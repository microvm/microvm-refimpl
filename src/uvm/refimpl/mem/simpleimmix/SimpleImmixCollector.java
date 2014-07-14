package uvm.refimpl.mem.simpleimmix;

import static uvm.platformsupport.Config.*;

import java.util.List;
import java.util.Map;

import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.itpr.IRefBox;
import uvm.refimpl.itpr.InterpreterFrame;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.RefBox;
import uvm.refimpl.itpr.ValueBox;
import uvm.refimpl.mem.AddressQueue;
import uvm.refimpl.mem.Collector;
import uvm.refimpl.mem.TypeSizes;
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
    private MicroVM microVM;

    private AddressQueue queue = new AddressQueue();

    public SimpleImmixCollector(SimpleImmixHeap heap, SimpleImmixSpace space,
            MicroVM microVM) {
        this.heap = heap;
        this.space = space;
        this.microVM = microVM;
    }

    @Override
    public SimpleImmixHeap getHeap() {
        return heap;
    }

    @Override
    protected void collect() {
        ErrorUtils.uvmAssert(queue.isEmpty(),
                "Queue is not empty when started GC");

        traceRoots();

        for (long addr : queue) {
            System.out.println("queued: " + addr);
        }

        doTransitiveClosure();

        ErrorUtils.uvmError("Out of memory");
        System.exit(0);
        heap.untriggerGC();
    }

    private void traceRoots() {
        traceExternal();
        traceGlobal();
        traceStacks();
    }

    private void traceExternal() {
        List<Long> er = microVM.extraRoots();
        for (long a : er) {
            maybeMark(a);
        }
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
                    if (vb instanceof RefBox) {
                        maybeMark(((RefBox) vb).getAddr());
                    } else if (vb instanceof IRefBox) {
                        maybeMark(((IRefBox) vb).getBase());
                    }
                }
            }

            // TODO Also trace alloca memory
        }
    }

    private void maybeMark(long addr) {
        if (addr == 0) {
            return;
        }

        boolean wasMarked = testAndSetMark(addr);

        if (!wasMarked) {
            space.markBlockByObjRef(addr);

            queue.add(addr);
        }
    }

    private static final long MARK_MASK = 0x4000000000000000L;

    private boolean testAndSetMark(long objRef) {
        long headerAddr = objRef + TypeSizes.GC_HEADER_OFFSET_TAG;
        long oldHeader = MEMORY_SUPPORT.loadLong(headerAddr);
        long markBit = oldHeader & MARK_MASK;
        if (markBit == 0) {
            long newHeader = oldHeader | MARK_MASK;
            MEMORY_SUPPORT.storeLong(headerAddr, newHeader);
            return false;
        } else {
            return true;
        }
    }

    private void doTransitiveClosure() {
        while (!queue.isEmpty()) {
            long objRef = queue.pollFirst();
            long tagAddr = objRef + TypeSizes.GC_HEADER_OFFSET_TAG;
            long tag = MEMORY_SUPPORT.loadLong(tagAddr);
            int typeID = (int) (tag & 0x00000000ffffffffL);
            Type type = microVM.getGlobalBundle().getTypeNs().getByID(typeID);
            scanObject(type, objRef);
        }
    }

    private void scanObject(Type type, long addr) {
        if (type instanceof Ref || type instanceof IRef
                || type instanceof WeakRef) {
            maybeMark(addr);
        } else if (type instanceof Struct) {
            // TODO: scan each field
        } else if (type instanceof Array) {
            // TODO: scan each element
        } else if (type instanceof Hybrid) {
            // TODO: Get the var-part length, scan the fixed part and repeat for
            // each var part.
        } else if (type instanceof TagRef64) {
            // TODO: Despite not implemented now, it should be traced only if
            // its tag indicates it is a reference.
        }
    }
}
