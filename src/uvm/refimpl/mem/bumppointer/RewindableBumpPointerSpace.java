package uvm.refimpl.mem.bumppointer;


public class RewindableBumpPointerSpace {

    public String name;
    public long begin;
    public long extend;

    public RewindableBumpPointerSpace(String name, long begin, long extend) {
        this.name = name;
        this.begin = begin;
        this.extend = extend;
    }

}
