package uvm.refimpl.mem;

import uvm.refimpl.mem.bumppointer.RewindableBumpPointerSpace;

public class StackMemory {

    private long begin;
    private long size;

    private RewindableBumpPointerSpace space;

    public StackMemory(long begin, long size) {
        this.begin = begin;
        this.size = size;

        this.space = new RewindableBumpPointerSpace("GlobalSpace", begin, size);
    }

}
