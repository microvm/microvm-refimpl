package uvm.refimpl.mem;

import uvm.refimpl.mem.bumppointer.RewindableBumpPointerSpace;

public class GlobalMemory {

    private long begin;
    private long size;

    private RewindableBumpPointerSpace space;

    public GlobalMemory(long begin, long size) {
        this.begin = begin;
        this.size = size;

        this.space = new RewindableBumpPointerSpace("GlobalSpace", begin, size);
    }

}
