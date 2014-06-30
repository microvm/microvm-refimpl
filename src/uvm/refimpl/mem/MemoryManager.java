package uvm.refimpl.mem;

import java.util.List;

import uvm.refimpl.mem.simpleimmix.SimpleImmixHeap;
import uvm.type.Type;

public class MemoryManager {
    private static final long MEMORY_BEGIN = 0x100000L; // Reserve 1MiB;

    private long heapSize;
    private long globalSize;
    private long stackSize;

    private SimpleImmixHeap heap;
    private GlobalMemory global;
    private List<StackMemory> stacks;

    private long stackBegin;

    public MemoryManager(long heapSize, long globalSize, long stackSize) {
        this.heapSize = heapSize;
        this.globalSize = globalSize;
        this.stackSize = stackSize;

        heap = new SimpleImmixHeap(MEMORY_BEGIN, heapSize);
        global = new GlobalMemory(MEMORY_BEGIN + heapSize, globalSize);
        stackBegin = MEMORY_BEGIN + heapSize + globalSize;
    }

    public long newScalar(Type type) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long newHybrid(Type type, long len) {
        // TODO Auto-generated method stub
        return 0;
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
