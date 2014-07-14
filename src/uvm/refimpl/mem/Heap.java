package uvm.refimpl.mem;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import uvm.util.ErrorUtils;

public abstract class Heap {
    protected Lock lock; // Used to communicate between the mutator and the collector.
    protected Condition gcCanStart;
    protected Condition gcFinished;

    protected static final int MUTATOR_RUNNING = 0;
    protected static final int DOING_GC = 1;

    protected int gcState;

    public Heap() {
        super();

        lock = new ReentrantLock();
        gcCanStart = lock.newCondition();
        gcFinished = lock.newCondition();

        gcState = MUTATOR_RUNNING;
    }

    public void mutatorTriggerAndWaitForGCEnd() {
        lock.lock();
        try {
            triggerGC();
            mutatorWaitForGCEnd();
        } finally {
            lock.unlock();
        }

    }

    private void triggerGC() {
        lock.lock();
        try {
            assert (gcState == MUTATOR_RUNNING);
            gcState = DOING_GC;
            gcCanStart.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void mutatorWaitForGCEnd() {
        lock.lock();
        try {
            while (gcState != MUTATOR_RUNNING) {
                try {
                    gcFinished.await();
                } catch (InterruptedException e) {
                    ErrorUtils
                            .uvmError("Interrupted while waiting for GC. Stop.");
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void untriggerGC() {
        lock.lock();
        try {
            assert (gcState == DOING_GC);
            gcState = MUTATOR_RUNNING;
            gcFinished.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void collectorWaitForGCStart() {
        lock.lock();
        while (gcState != DOING_GC) {
            try {
                gcCanStart.await();
            } catch (InterruptedException e) {
                ErrorUtils.uvmError("GC thread is interrupted.");
            }
        }
        lock.unlock();
    }

    public abstract Mutator makeMutator();

}