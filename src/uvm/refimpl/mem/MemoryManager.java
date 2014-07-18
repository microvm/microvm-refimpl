package uvm.refimpl.mem;

import java.util.ArrayList;
import java.util.List;

import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.itpr.MicroVMInternalTypes;
import uvm.refimpl.mem.simpleimmix.SimpleImmixHeap;

public class MemoryManager {
    private static final long MEMORY_BEGIN = 0x100000L; // Reserve 1MiB;

    private final long heapSize;
    private final long globalSize;
    private final long stackSize;

    private final SimpleImmixHeap heap;
    private final GlobalMemory globalMemory;
    private final List<StackMemory> stacks = new ArrayList<StackMemory>();

    private final Mutator internalMutator;

    private MicroVM microVM;

    public MemoryManager(long heapSize, long globalSize, long stackSize,
            MicroVM microVM) {
        this.heapSize = heapSize;
        this.globalSize = globalSize;
        this.stackSize = stackSize;
        this.microVM = microVM;

        heap = new SimpleImmixHeap(MEMORY_BEGIN, heapSize, microVM);
        globalMemory = new GlobalMemory(MEMORY_BEGIN + heapSize, globalSize,
                microVM);

        internalMutator = heap.makeMutator();
    }

    public Mutator makeMutator() {
        return heap.makeMutator();
    }

    public StackMemory makeStackMemory() {
        long objRef = internalMutator.newHybrid(
                MicroVMInternalTypes.BYTE_ARRAY_TYPE, stackSize);
        StackMemory stackMemory = new StackMemory(objRef, stackSize, microVM);
        return stackMemory;
    }

    // Getters and setters

    public SimpleImmixHeap getHeap() {
        return heap;
    }

    public GlobalMemory getGlobalMemory() {
        return globalMemory;
    }
}
