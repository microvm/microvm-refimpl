package uvm.refimpl.mem.bumppointer;

import uvm.refimpl.mem.Space;

public class RewindableBumpPointerSpace extends Space {

    public RewindableBumpPointerSpace(String name, long begin, long extend) {
        super(name, begin, extend);
    }

}
