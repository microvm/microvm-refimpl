package uvm.refimpl.mem;

public interface Allocator {

    long alloc(long size, long align, long headerSize);

}
