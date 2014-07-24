package uvm.refimpl.mem.simpleimmix;

import static uvm.util.ErrorUtils.*;

import java.util.Map;

import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.itpr.HasObjRef;
import uvm.refimpl.itpr.InterpreterFrame;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.ValueBox;
import uvm.refimpl.mem.AddressQueue;
import uvm.refimpl.mem.Collector;
import uvm.refimpl.mem.HeaderUtils;
import uvm.refimpl.mem.MemConstants;
import uvm.refimpl.mem.MemUtils;
import uvm.refimpl.mem.StackMemory;
import uvm.refimpl.mem.TypeSizes;
import uvm.refimpl.mem.los.LargeObjectSpace;
import uvm.refimpl.mem.scanning.MemoryDataScanner;
import uvm.refimpl.mem.scanning.ObjectMarker;
import uvm.refimpl.mem.scanning.RefFieldHandler;
import uvm.type.Hybrid;
import uvm.type.Type;
import uvm.util.LogUtil;
import uvm.util.Logger;
import static uvm.platformsupport.Config.MEMORY_SUPPORT;

public class SimpleImmixCollector extends Collector implements Runnable {
    private static final Logger logger = LogUtil.getLogger("SIC");

    private SimpleImmixHeap heap;
    private SimpleImmixSpace space;
    private LargeObjectSpace los;
    private MicroVM microVM;

    private SimpleImmixDefragMutator defragMutator;
    private boolean canDefrag;

    private long threshold;

    private int gcCount;

    public SimpleImmixCollector(SimpleImmixHeap heap, SimpleImmixSpace space,
            LargeObjectSpace los, MicroVM microVM) {
        this.heap = heap;
        this.space = space;
        this.los = los;
        this.microVM = microVM;

        gcCount = 0;
    }

    @Override
    public SimpleImmixHeap getHeap() {
        return heap;
    }

    @Override
    protected void collect() {
        gcCount++;
        logger.format("GC starts. gcCount=%d", gcCount);

        logger.format("Clearing stats...");

        space.clearStats();

        logger.format("Marking and getting statistics....");
        AllScanner s1 = new AllScanner(new RefFieldHandler() {
            @Override
            public boolean handle(boolean fromClient, HasObjRef fromBox,
                    long fromObj, long fromIRef, long toObj) {
                return maybeMarkAndStat(toObj);
            }
        });

        s1.scanAll();

        logger.format("Stat finished. Unmarking....");

        AllScanner s2 = new AllScanner(new RefFieldHandler() {
            @Override
            public boolean handle(boolean fromClient, HasObjRef fromBox,
                    long fromObj, long fromIRef, long toObj) {
                return clearMark(toObj);
            }
        });

        s2.scanAll();

        long resvSpace = space.getTotalReserveSpace();
        threshold = space.findThreshold(resvSpace);

        logger.format("Making defrag mutator...");
        defragMutator = new SimpleImmixDefragMutator(heap, space);
        canDefrag = true;

        logger.format("Mark again, maybe move objects....");
        AllScanner s3 = new AllScanner(markMover);

        s3.scanAll();

        defragMutator.close();

        logger.format("Marked. Collecting blocks....");

        boolean anyMemoryRecycled = collectBlocks();

        if (!anyMemoryRecycled && heap.getMustFreeSpace()) {
            uvmError("Out of memory because the GC failed to recycle any memory.");
            System.exit(1);
        }

        logger.format("Blocks collected. Unmarking....");

        AllScanner s4 = new AllScanner(new RefFieldHandler() {
            @Override
            public boolean handle(boolean fromClient, HasObjRef fromBox,
                    long fromObj, long fromIRef, long toObj) {
                return clearMark(toObj);
            }
        });

        s4.scanAll();

        logger.format("GC finished.");
        heap.untriggerGC();
    }

    private static final long MARK_MASK = 0x4000000000000000L;
    private static final long MOVE_MASK = 0x8000000000000000L;

    private boolean testAndSetMark(long objRef) {
        long oldHeader = HeaderUtils.getTag(objRef);
        logger.format("GC header of %d is %x", objRef, oldHeader);
        long markBit = oldHeader & MARK_MASK;
        if (markBit == 0) {
            long newHeader = oldHeader | MARK_MASK;
            HeaderUtils.setTag(objRef, newHeader);
            return false;
        } else {
            return true;
        }
    }

    private boolean maybeMarkAndStat(long addr) {
        if (addr == 0) {
            return false;
        }

        boolean wasMarked = testAndSetMark(addr);

        if (!wasMarked) {
            logger.format("Newly marked %d", addr);

            if (space.isInSpace(addr)) {
                space.markBlockByObjRef(addr);

                long tag = HeaderUtils.getTag(addr);
                Type type = HeaderUtils.getType(microVM, tag);
                long used;
                if (type instanceof Hybrid) {
                    long varSize = HeaderUtils.getVarLength(addr);
                    used = TypeSizes.hybridSizeOf((Hybrid) type, varSize)
                            + TypeSizes.GC_HEADER_SIZE_HYBRID;
                } else {
                    used = TypeSizes.sizeOf(type)
                            + TypeSizes.GC_HEADER_SIZE_SCALAR;
                }

                int blockNum = space.objRefToBlockIndex(addr);
                space.incStat(blockNum, used);
            } else if (los.isInSpace(addr)) {
                los.markBlockByObjRef(addr);
            } else {
                uvmError(String.format("Object ref %d not in any space", addr));
                return false; // Unreachable
            }

            return true;
        } else {
            return false;
        }
    }

    private class MarkMover implements RefFieldHandler {
        @Override
        public boolean handle(boolean fromClient, HasObjRef fromBox,
                long fromObj, long fromIRef, long toObj) {

            if (toObj == 0) {
                return false;
            }

            long oldHeader = HeaderUtils.getTag(toObj);
            logger.format("GC header of %d is %x", toObj, oldHeader);
            long markBit = oldHeader & MARK_MASK;
            long moveBit = oldHeader & MOVE_MASK;
            boolean wasMarked = markBit != 0;
            boolean wasMoved = moveBit != 0;

            if (wasMoved) {
                long dest = HeaderUtils.getForwardedDest(oldHeader);
                updateSrcRef(fromClient, fromBox, fromIRef, dest);
                return false;
            } else {
                if (wasMarked) {
                    return false;
                } else {
                    boolean isMovable;
                    if (fromClient) {
                        isMovable = false;
                    } else {
                        boolean isInSmallObjectSpace = space.isInSpace(toObj);
                        if (isInSmallObjectSpace) {
                            int pageNum = space.objRefToBlockIndex(toObj);
                            long stat = space.getStat(pageNum);
                            if (stat < threshold) {
                                isMovable = true;
                            } else {
                                isMovable = false;
                            }
                        } else {
                            isMovable = false;
                        }
                    }
                    long actualObj;
                    if (isMovable) {
                        actualObj = evacuate(toObj);
                        if (actualObj != toObj) {
                            updateSrcRef(fromClient, fromBox, fromIRef,
                                    actualObj);
                        }
                    } else {
                        actualObj = toObj;
                    }

                    long newHeader = oldHeader | MARK_MASK;
                    HeaderUtils.setTag(actualObj, newHeader);
                    logger.format("Newly marked %d", actualObj);

                    if (space.isInSpace(actualObj)) {
                        space.markBlockByObjRef(actualObj);
                    } else if (los.isInSpace(actualObj)) {
                        los.markBlockByObjRef(actualObj);
                    } else {
                        uvmError(String.format(
                                "Object ref %d not in any space", actualObj));
                        return false; // Unreachable
                    }

                    return true;
                }
            }
        }

        private long evacuate(long oldObjRef) {
            logger.format("Evacuating object %d", oldObjRef);
            if (!canDefrag) {
                logger.format("No more reserved blocks.");
                return oldObjRef;
            } else {
                long tag = HeaderUtils.getTag(oldObjRef);
                Type type = HeaderUtils.getType(microVM, tag);
                long newObjRef;
                long oldSize;
                if (type instanceof Hybrid) {
                    long len = HeaderUtils.getVarLength(oldObjRef);
                    Hybrid htype = (Hybrid) type;
                    newObjRef = defragMutator.newHybrid(htype, len);
                    oldSize = TypeSizes.hybridSizeOf(htype, len);
                } else {
                    newObjRef = defragMutator.newScalar(type);
                    oldSize = TypeSizes.sizeOf(type);
                }

                if (newObjRef == 0) {
                    canDefrag = false;
                    logger.format("No more reserved blocks and thus no more moving.");
                    return oldObjRef;
                } else {
                    oldSize = TypeSizes.alignUp(oldSize,
                            MemConstants.WORD_SIZE_BYTES);
                    logger.format("Copying old object %d to %d, %d bytes.",
                            oldObjRef, newObjRef, oldSize);
                    MemUtils.memcpy(oldObjRef, newObjRef, oldSize);

                    // Set up tombstone
                    long newTag = newObjRef | MOVE_MASK;
                    HeaderUtils.setTag(oldObjRef, newTag);

                    return newObjRef;
                }
            }
        }

        private void updateSrcRef(boolean fromClient, HasObjRef fromBox,
                long fromIRef, long dest) {
            if (fromClient) {
                return;
            } else if (fromBox != null) {
                fromBox.setObjRef(dest);
            } else {
                MEMORY_SUPPORT.storeLong(fromIRef, dest);
            }
        }
    }

    private MarkMover markMover = new MarkMover();

    private boolean collectBlocks() {
        boolean anyMemoryRecycled = false;
        anyMemoryRecycled = space.collectBlocks() || anyMemoryRecycled;
        anyMemoryRecycled = los.collect() || anyMemoryRecycled;
        return anyMemoryRecycled;
    }

    private boolean clearMark(long objRef) {
        if (objRef == 0) {
            return false;
        }

        long oldHeader = HeaderUtils.getTag(objRef);
        logger.format("GC header of %d is %x", objRef, oldHeader);
        long markBit = oldHeader & MARK_MASK;
        if (markBit != 0) {
            long newHeader = oldHeader & ~(MARK_MASK | MOVE_MASK);
            HeaderUtils.setTag(objRef, newHeader);
            return true;
        } else {
            return false;
        }
    }

    class AllScanner implements RefFieldHandler {
        private RefFieldHandler handler;

        private AddressQueue queue = new AddressQueue();

        public AllScanner(RefFieldHandler handler) {
            this.handler = handler;
        }

        public void scanAll() {
            traceRoots();
            doTransitiveClosure();
        }

        private void traceRoots() {
            logger.format("Tracing external roots...");
            traceExternal();
            logger.format("Tracing globals...");
            traceGlobal();
            logger.format("Tracing stacks...");
            traceStacks();
        }

        private void traceExternal() {
            microVM.clientMark(externalMarker);
        }

        private ObjectMarker externalMarker = new ObjectMarker() {

            @Override
            public void markObjRef(long objRef) {
                handle(true, null, 0, 0, objRef);
            }
        };

        private void traceGlobal() {
            microVM.getMemoryManager().getGlobalMemory().getAllocator()
                    .traverseFields(this);
        }

        private void traceStacks() {
            Map<Integer, InterpreterStack> sr = microVM.getThreadStackManager()
                    .getStackRegistry();
            for (InterpreterStack sta : sr.values()) {
                if (sta.getState() != InterpreterStack.DEAD) {
                    logger.format("Tracing stack %d for registers...",
                            sta.getID());
                    for (InterpreterFrame fra = sta.getTop(); fra != null; fra = fra
                            .getPrevFrame()) {
                        for (ValueBox vb : fra.getValueDict().values()) {
                            if (vb instanceof HasObjRef) {
                                HasObjRef rvb = (HasObjRef) vb;
                                handle(false, rvb, 0, 0, rvb.getObjRef());
                            }
                        }
                    }

                    logger.format("Tracing stack %d memory chunk in LOS...",
                            sta.getID());

                    StackMemory stackMemory = sta.getStackMemory();
                    long stackMemObjAddr = stackMemory.getStackObjRef();
                    handle(false, null, 0, 0, stackMemObjAddr);
                    logger.format("Tracing stack %d for allocas...",
                            sta.getID());

                    stackMemory.traverseFields(this);
                }
            }
        }

        private void doTransitiveClosure() {
            while (!queue.isEmpty()) {
                long objRef = queue.pollFirst();
                MemoryDataScanner.scanMemoryData(objRef, objRef, microVM, this);
            }
        }

        @Override
        public boolean handle(boolean fromClient, HasObjRef fromBox,
                long fromObj, long fromIRef, long toObj) {
            boolean toEnqueue = handler.handle(fromClient, fromBox, fromObj,
                    fromIRef, toObj);
            if (toEnqueue) {
                queue.add(toObj);
            }
            return toEnqueue;
        }

    }

}
