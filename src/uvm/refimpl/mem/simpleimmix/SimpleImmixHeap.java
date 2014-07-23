package uvm.refimpl.mem.simpleimmix;

import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.mem.Heap;
import uvm.refimpl.mem.los.LargeObjectSpace;

/**
 * A heap which uses the simplified Immix GC algorithm.
 */
public class SimpleImmixHeap extends Heap {
    private SimpleImmixSpace space;
    private SimpleImmixCollector collector;
    private Thread collectorThread;
    private MicroVM microVM;
    private LargeObjectSpace los;

    public SimpleImmixHeap(long begin, long size, MicroVM microVM) {
        super();
        this.microVM = microVM;

        long mid = begin + size / 2;

        space = new SimpleImmixSpace(this, "SimpleImmixSpace", begin, size / 2);

        los = new LargeObjectSpace(this, "Large object space", mid, size / 2);

        collector = new SimpleImmixCollector(this, space, los, microVM);
        collectorThread = new Thread(collector);
        collectorThread.setDaemon(true);
        collectorThread.start();
    }

    @Override
    public SimpleImmixMutator makeMutator() {
        SimpleImmixMutator mutator = new SimpleImmixMutator(this, space);
        return mutator;
    }

    public long getBlock(long oldBlockAddr) {
        while (true) {
            long addr = space.tryGetBlock(oldBlockAddr);

            if (addr != 0L) {
                return addr;
            }

            mutatorTriggerAndWaitForGCEnd(true);
        }
    }

    public void returnBlock(long blockAddr) {
        space.returnBlock(blockAddr);
    }

    public long allocLargeObject(long size, long align, long headerSize) {
        return los.alloc(size, align, headerSize);
    }

}
