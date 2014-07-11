package uvm.refimpl.mem.simpleimmix;

import uvm.refimpl.mem.Heap;

/**
 * A heap which uses the simplified Immix GC algorithm.
 * <p>
 * 
 */
public class SimpleImmixHeap extends Heap {
    private SimpleImmixSpace space;
    private SimpleImmixCollector collector;
    private Thread collectorThread;

    public SimpleImmixHeap(long begin, long size) {
        super();

        space = new SimpleImmixSpace(this, "SimpleImmixSpace", begin, size);

        collector = new SimpleImmixCollector(this);
        collectorThread = new Thread(collector);
        collectorThread.setDaemon(true);
        collectorThread.start();
    }

    @Override
    public SimpleImmixMutator makeMutator() {
        SimpleImmixMutator mutator;
        lock.lock();
        try {
            mutator = new SimpleImmixMutator(this, space);
        } finally {
            lock.unlock();
        }
        return mutator;
    }

    public long getBlock() {
        lock.lock();
        try {
            while (true) {
                long addr = space.tryGetBlock();

                if (addr != 0L) {
                    return addr;
                }

                mutatorTriggerAndWaitForGCEnd();
            }
        } finally {
            lock.unlock();
        }
    }

}
