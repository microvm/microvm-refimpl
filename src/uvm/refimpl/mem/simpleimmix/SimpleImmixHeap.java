package uvm.refimpl.mem.simpleimmix;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import uvm.refimpl.mem.MemorySupport;
import uvm.refimpl.mem.UnsafeMemorySupport;

/**
 * A heap which uses the simplified Immix GC algorithm.
 * <p>
 * 
 */
public class SimpleImmixHeap {
    public static final long HEAP_ADDR_BEGIN = 0x100000L;
    public static final long GC_HEADER_SIZE_BITS = 64;
    public static final long GC_HEADER_SIZE_BYTES = 8;
    public static final long GC_HEADER_SIZE_LOG = 6;

    public MemorySupport memorySupport = new UnsafeMemorySupport();

    public SimpleImmixSpace space;

    public int liveMutators;
    public int mutatorsStopped;

    public Lock lock; // For almost everything.
    public Condition gcCanStart;
    public Condition gcFinished;

    private boolean globalPauseFlag;
    
    public SimpleImmixCollector collector;
    public Thread collectorThread;

    public SimpleImmixHeap(long size) {
        super();

        space = new SimpleImmixSpace(this, "SimpleImmixSpace", HEAP_ADDR_BEGIN,
                size);

        liveMutators = 0;
        mutatorsStopped = 0;

        lock = new ReentrantLock();
        gcCanStart = lock.newCondition();
        gcFinished = lock.newCondition();

        globalPauseFlag = false;
        
        collector = new SimpleImmixCollector(this);
        collectorThread = new Thread(collector);
        collectorThread.start();
    }

    public void triggerAndWaitForGC() {
        try {
            globalPauseFlag = true;
            mutatorsStopped += 1;
            if (mutatorsStopped == liveMutators) {
                gcCanStart.signal();
            }

            while (globalPauseFlag) {
                try {
                    gcFinished.await();
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }

            mutatorsStopped -= 1;
        } finally {
            lock.unlock();
        }

    }

    public boolean getGlobalPauseFlag() {
        boolean rv;

        lock.lock();
        try {
            rv = globalPauseFlag;
        } finally {
            lock.unlock();
        }

        return rv;
    }

    public SimpleImmixMutator makeMutator() {
        lock.lock();
        SimpleImmixMutator mutator;
        try {
            mutator = new SimpleImmixMutator(this, space);
            liveMutators++;
        } finally {
            lock.unlock();
        }
        return mutator;
    }

}
