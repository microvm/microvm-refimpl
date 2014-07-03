package uvm.refimpl.mem;

import uvm.refimpl.mem.bumppointer.RewindableBumpPointerSpace;
import uvm.type.Type;

public class StackMemory {

    private long begin;
    private long size;

    private RewindableBumpPointerSpace space;

    public StackMemory(long begin, long size) {
        this.begin = begin;
        this.size = size;

        this.space = new RewindableBumpPointerSpace("StackSpace", begin, size);
    }

    public long allocaScalar(Type type) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long allocaHybrid(Type type, long len) {
        // TODO Auto-generated method stub
        return 0;
    }

}
