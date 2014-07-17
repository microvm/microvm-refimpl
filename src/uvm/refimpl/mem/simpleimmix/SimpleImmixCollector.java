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
import uvm.refimpl.mem.ObjectMarker;
import uvm.refimpl.mem.TypeSizes;
import uvm.refimpl.mem.los.LargeObjectSpace;
import uvm.type.Array;
import uvm.type.Hybrid;
import uvm.type.IRef;
import uvm.type.Ref;
import uvm.type.Struct;
import uvm.type.TagRef64;
import uvm.type.Type;
import uvm.type.WeakRef;
import uvm.util.ErrorUtils;

public class SimpleImmixCollector extends Collector implements Runnable {

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
        System.out.println("GC starts.");

        System.out.println("Marking....");
        AllScanner s1 = new AllScanner(new RefFieldHandler() {
            @Override
            public boolean handle(boolean fromClient, HasObjRef fromBox,
                    long fromObj, long fromIRef, long toObj) {
                return maybeMark(toObj);
            }
        });

        s1.scanAll();

        System.out.println("Marked. Collecting blocks....");

        collectBlocks();

        System.out.println("Blocks collected. Unmarking....");

        AllScanner s2 = new AllScanner(new RefFieldHandler() {
            @Override
            public boolean handle(boolean fromClient, HasObjRef fromBox,
                    long fromObj, long fromIRef, long toObj) {
                return clearMark(toObj);
            }
        });

        s2.scanAll();

        System.out.println("GC finished.");
        heap.untriggerGC();
    }

    private static final long MARK_MASK = 0x4000000000000000L;

    private boolean testAndSetMark(long objRef) {
        long headerAddr = objRef + TypeSizes.GC_HEADER_OFFSET_TAG;
        long oldHeader = MEMORY_SUPPORT.loadLong(headerAddr);
        System.out.format("GC header of %d is %x\n", objRef, oldHeader);
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
            System.out.format("Newly marked %d\n", addr);

            if (space.isInSpace(addr)) {
                space.markBlockByObjRef(addr);
            } else if (los.isInSpace(addr)) {
                los.markBlockByObjRef(addr);
            } else {
                ErrorUtils.uvmError(String.format(
                        "Object ref %d not in any space: \n", addr));
                return false; // Unreachable
            }

            return true;
        } else {
            return false;
        }
    }

    private void collectBlocks() {
        space.collectBlocks();
        los.collect();
    }

    private boolean clearMark(long objRef) {
        if (objRef == 0) {
            return false;
        }

        long headerAddr = objRef + TypeSizes.GC_HEADER_OFFSET_TAG;
        long oldHeader = MEMORY_SUPPORT.loadLong(headerAddr);
        System.out.format("GC header of %d is %x\n", objRef, oldHeader);
        long markBit = oldHeader & MARK_MASK;
        if (markBit != 0) {
            long newHeader = oldHeader & ~MARK_MASK;
            MEMORY_SUPPORT.storeLong(headerAddr, newHeader);
            return true;
        } else {
            return false;
        }
    }

    interface RefFieldHandler {
        public boolean handle(boolean fromClient, HasObjRef fromBox,
                long fromObj, long fromIRef, long toObj);
    }

    class AllScanner implements ObjectMarker {
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
            traceExternal();
            traceGlobal();
            traceStacks();
        }

        private void traceExternal() {
            microVM.clientMark(this);
        }

        @Override
        public void markObjRef(long objRef) {
            handleMaybeEnqueue(true, null, 0, 0, objRef);
        }

        private void traceGlobal() {
            // TODO Implement bump pointer space for global and stack
        }

        private void traceStacks() {
            Map<Integer, InterpreterStack> sr = microVM.getThreadStackManager()
                    .getStackRegistry();
            for (InterpreterStack sta : sr.values()) {
                for (InterpreterFrame fra = sta.getTop(); fra != null; fra = fra
                        .getPrevFrame()) {
                    for (ValueBox vb : fra.getValueDict().values()) {
                        if (vb instanceof HasObjRef) {
                            HasObjRef rvb = (HasObjRef) vb;
                            handleMaybeEnqueue(false, rvb, 0, 0,
                                    rvb.getObjRef());
                        }
                    }
                }

                // TODO Also trace alloca memory
            }
        }

        private void doTransitiveClosure() {
            while (!queue.isEmpty()) {
                long objRef = queue.pollFirst();
                long tagAddr = objRef + TypeSizes.GC_HEADER_OFFSET_TAG;
                long tag = MEMORY_SUPPORT.loadLong(tagAddr);
                int typeID = (int) (tag & 0x00000000ffffffffL);
                Type type = microVM.getGlobalBundle().getTypeNs()
                        .getByID(typeID);
                scanField(type, objRef, objRef);
            }
        }

        private void scanField(Type type, long objRef, long iRef) {
            if (type instanceof Ref || type instanceof IRef
                    || type instanceof WeakRef) {
                long toObj = MEMORY_SUPPORT.loadLong(iRef);
                handleMaybeEnqueue(false, null, objRef, iRef, toObj);
            } else if (type instanceof Struct) {
                Struct sTy = (Struct) type;
                long fieldAddr = iRef;
                for (Type fieldTy : sTy.getFieldTypes()) {
                    long fieldAlign = TypeSizes.alignOf(fieldTy);
                    fieldAddr = TypeSizes.alignUp(fieldAddr, fieldAlign);
                    scanField(fieldTy, objRef, fieldAddr);
                    fieldAddr += TypeSizes.sizeOf(fieldTy);
                }
            } else if (type instanceof Array) {
                Array aTy = (Array) type;
                Type elemTy = aTy.getElemType();
                long elemSize = TypeSizes.sizeOf(elemTy);
                long elemAlign = TypeSizes.alignOf(elemTy);
                long elemAddr = iRef;
                for (int i = 0; i < aTy.getLength(); i++) {
                    scanField(elemTy, objRef, elemAddr);
                    elemAddr = TypeSizes
                            .alignUp(elemAddr + elemSize, elemAlign);
                }
            } else if (type instanceof Hybrid) {
                Hybrid hTy = (Hybrid) type;
                Type fixedTy = hTy.getFixedPart();
                Type varTy = hTy.getVarPart();
                long fixedSize = TypeSizes.sizeOf(fixedTy);
                long fixedAlign = TypeSizes.alignOf(fixedTy);
                long varSize = TypeSizes.sizeOf(varTy);
                long varAlign = TypeSizes.alignOf(varTy);
                long curAddr = iRef;

                long varLength = MEMORY_SUPPORT.loadLong(iRef
                        + TypeSizes.GC_HEADER_OFFSET_HYBRID_LENGTH);

                scanField(fixedTy, objRef, curAddr);
                curAddr = TypeSizes.alignUp(curAddr + fixedSize, fixedAlign);

                for (long i = 0; i < varLength; i++) {
                    scanField(varTy, objRef, curAddr);
                    curAddr = TypeSizes.alignUp(curAddr + varSize, varAlign);
                }
            } else if (type instanceof TagRef64) {
                // TODO: Despite not implemented now, it should be traced only
                // if
                // its tag indicates it is a reference.
            }
        }

        private void handleMaybeEnqueue(boolean fromClient, HasObjRef fromBox,
                long fromObj, long fromIRef, long toObj) {
            boolean toEnqueue = handler.handle(fromClient, fromBox, fromObj,
                    fromIRef, toObj);
            if (toEnqueue) {
                queue.add(toObj);
            }
        }

    }

}
