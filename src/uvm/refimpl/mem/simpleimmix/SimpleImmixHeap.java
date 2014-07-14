package uvm.refimpl.mem.simpleimmix;

import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.mem.Heap;

/**
 * A heap which uses the simplified Immix GC algorithm.
 */
public class SimpleImmixHeap extends Heap {
    private SimpleImmixSpace space;
    private SimpleImmixCollector collector;
    private Thread collectorThread;
    private MicroVM microVM;

    public SimpleImmixHeap(long begin, long size, MicroVM microVM) {
        super();
        this.microVM = microVM;

        space = new SimpleImmixSpace(this, "SimpleImmixSpace", begin, size);

        collector = new SimpleImmixCollector(this, space, microVM);
        collectorThread = new Thread(collector);
        collectorThread.setDaemon(true);
        collectorThread.start();
    }

    @Override
    public SimpleImmixMutator makeMutator() {
        SimpleImmixMutator mutator = new SimpleImmixMutator(this, space);
        return mutator;
    }

    public long getBlock() {
        while (true) {
            long addr = space.tryGetBlock();

            if (addr != 0L) {
                return addr;
            }

            mutatorTriggerAndWaitForGCEnd();
        }
    }

}
