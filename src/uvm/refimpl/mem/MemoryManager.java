package uvm.refimpl.mem;

import java.util.ArrayList;
import java.util.List;

import uvm.refimpl.mem.simpleimmix.SimpleImmixHeap;

public class MemoryManager {
    private static final long MEMORY_BEGIN = 0x100000L; // Reserve 1MiB;

    private final long heapSize;
    private final long globalSize;
    private final long stackSize;

    private final SimpleImmixHeap heap;
    private final GlobalMemory global;
    private final List<StackMemory> stacks = new ArrayList<StackMemory>();

    private long stackBegin;

    public MemoryManager(long heapSize, long globalSize, long stackSize) {
        this.heapSize = heapSize;
        this.globalSize = globalSize;
        this.stackSize = stackSize;

        heap = new SimpleImmixHeap(MEMORY_BEGIN, heapSize);
        global = new GlobalMemory(MEMORY_BEGIN + heapSize, globalSize);
        stackBegin = MEMORY_BEGIN + heapSize + globalSize;
    }

    public Mutator makeMutator() {
        return heap.makeMutator();
    }

    public StackMemory makeStackMemory() {
        long myStackBegin;
        synchronized (this) {
            myStackBegin = stackBegin;
            stackBegin += stackSize;
        }

        StackMemory stackMemory = new StackMemory(myStackBegin, stackSize);
        return stackMemory;
    }
}
