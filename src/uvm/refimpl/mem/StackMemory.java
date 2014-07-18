package uvm.refimpl.mem;

import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.mem.bumppointer.RewindableBumpPointerAllocator;

public class StackMemory extends RewindableBumpPointerAllocator {

    private long stackObjRef;

    public StackMemory(long stackObjRef, long extend, MicroVM microVM) {
        super(stackObjRef, extend, microVM);
        this.stackObjRef = stackObjRef;
    }

    public long getStackObjRef() {
        return stackObjRef;
    }

}
