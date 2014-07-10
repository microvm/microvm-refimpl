package uvm.refimpl.mem.simpleimmix;

import java.util.concurrent.locks.ReentrantLock;

import uvm.refimpl.mem.Heap;
import uvm.refimpl.mem.Mutator;

/**
 * A heap which uses the simplified Immix GC algorithm.
 * <p>
 * 
 */
public class SimpleImmixHeap extends Heap {
    private SimpleImmixCollector collector;
    private Thread collectorThread;

    public SimpleImmixHeap(long begin, long size) {
        super();

        space = new SimpleImmixSpace(this, "SimpleImmixSpace", begin, size);

        lock = new ReentrantLock();
        gcCanStart = lock.newCondition();
        gcFinished = lock.newCondition();

        liveMutators = 0;
        mutatorsStopped = 0;
        isDoingGC = false;

        globalPauseFlag = false;

        collector = new SimpleImmixCollector(this);
        collectorThread = new Thread(collector);
        collectorThread.setDaemon(true);
        collectorThread.start();
    }

    public Mutator makeMutator() {
        Mutator mutator;
        lock.lock();
        try {
            liveMutators++;
            mutator = new SimpleImmixMutator(this, (SimpleImmixSpace) space);
        } finally {
            lock.unlock();
        }
        return mutator;
    }

}
