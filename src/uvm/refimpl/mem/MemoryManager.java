package uvm.refimpl.mem;

import java.util.ArrayList;
import java.util.List;

import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.mem.simpleimmix.SimpleImmixHeap;

public class MemoryManager {
    private static final long MEMORY_BEGIN = 0x100000L; // Reserve 1MiB;

    private final long heapSize;
    private final long globalSize;
    private final long stackSize;

    private final SimpleImmixHeap heap;
    private final GlobalMemory global;
    private final List<StackMemory> stacks = new ArrayList<StackMemory>();

    public MemoryManager(long heapSize, long globalSize, long stackSize,
            MicroVM microVM) {
        this.heapSize = heapSize;
        this.globalSize = globalSize;
        this.stackSize = stackSize;

        heap = new SimpleImmixHeap(MEMORY_BEGIN, heapSize, microVM);
        global = new GlobalMemory(MEMORY_BEGIN + heapSize, globalSize);
    }

    public Mutator makeMutator() {
        return heap.makeMutator();
    }

    public StackMemory makeStackMemory() {
        // TODO: Implement it.
        return null;
    }

    // Getters and setters

    public SimpleImmixHeap getHeap() {
        return heap;
    }

    public GlobalMemory getGlobal() {
        return global;
    }
}
