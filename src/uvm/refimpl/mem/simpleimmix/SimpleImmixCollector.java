package uvm.refimpl.mem.simpleimmix;

import static uvm.platformsupport.Config.*;

import java.util.Map;

import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.itpr.HasObjRef;
import uvm.refimpl.itpr.InterpreterFrame;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.ValueBox;
import uvm.refimpl.mem.AddressQueue;
import uvm.refimpl.mem.Collector;
import uvm.refimpl.mem.StackMemory;
import uvm.refimpl.mem.TypeSizes;
import uvm.refimpl.mem.los.LargeObjectSpace;
import uvm.refimpl.mem.scanning.MemoryDataScanner;
import uvm.refimpl.mem.scanning.ObjectMarker;
import uvm.refimpl.mem.scanning.RefFieldHandler;
import uvm.util.ErrorUtils;
import uvm.util.LogUtil;
import uvm.util.Logger;

public class SimpleImmixCollector extends Collector implements Runnable {
    private static final Logger logger = LogUtil.getLogger("SIC");

    private SimpleImmixHeap heap;
    private SimpleImmixSpace space;
    private LargeObjectSpace los;
    private MicroVM microVM;

    public SimpleImmixCollector(SimpleImmixHeap heap, SimpleImmixSpace space,
            LargeObjectSpace los, MicroVM microVM) {
        this.heap = heap;
        this.space = space;
        this.los = los;
        this.microVM = microVM;
    }

    @Override
    public SimpleImmixHeap getHeap() {
        return heap;
    }

    @Override
    protected void collect() {
        logger.format("GC starts.");

        logger.format("Marking....");
        AllScanner s1 = new AllScanner(new RefFieldHandler() {
            @Override
            public boolean handle(boolean fromClient, HasObjRef fromBox,
                    long fromObj, long fromIRef, long toObj) {
                return maybeMark(toObj);
            }
        });

        s1.scanAll();

        logger.format("Marked. Collecting blocks....");

        boolean anyMemoryRecycled = collectBlocks();

        if (!anyMemoryRecycled && heap.getMustFreeSpace()) {
            ErrorUtils
                    .uvmError("Out of memory because the GC failed to recycle any memory.");
            System.exit(1);
        }

        logger.format("Blocks collected. Unmarking....");

        AllScanner s2 = new AllScanner(new RefFieldHandler() {
            @Override
            public boolean handle(boolean fromClient, HasObjRef fromBox,
                    long fromObj, long fromIRef, long toObj) {
                return clearMark(toObj);
            }
        });

        s2.scanAll();

        logger.format("GC finished.");
        heap.untriggerGC();
    }

    private static final long MARK_MASK = 0x4000000000000000L;

    private boolean testAndSetMark(long objRef) {
        long headerAddr = objRef + TypeSizes.GC_HEADER_OFFSET_TAG;
        long oldHeader = MEMORY_SUPPORT.loadLong(headerAddr);
        logger.format("GC header of %d is %x", objRef, oldHeader);
        long markBit = oldHeader & MARK_MASK;
        if (markBit == 0) {
            long newHeader = oldHeader | MARK_MASK;
            MEMORY_SUPPORT.storeLong(headerAddr, newHeader);
            return false;
        } else {
            return true;
        }
    }

    private boolean maybeMark(long addr) {
        if (addr == 0) {
            return false;
        }

        boolean wasMarked = testAndSetMark(addr);

        if (!wasMarked) {
            logger.format("Newly marked %d", addr);

            if (space.isInSpace(addr)) {
                space.markBlockByObjRef(addr);
            } else if (los.isInSpace(addr)) {
                los.markBlockByObjRef(addr);
            } else {
                ErrorUtils.uvmError(String.format(
                        "Object ref %d not in any space", addr));
                return false; // Unreachable
            }

            return true;
        } else {
            return false;
        }
    }

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

        long headerAddr = objRef + TypeSizes.GC_HEADER_OFFSET_TAG;
        long oldHeader = MEMORY_SUPPORT.loadLong(headerAddr);
        logger.format("GC header of %d is %x", objRef, oldHeader);
        long markBit = oldHeader & MARK_MASK;
        if (markBit != 0) {
            long newHeader = oldHeader & ~MARK_MASK;
            MEMORY_SUPPORT.storeLong(headerAddr, newHeader);
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
