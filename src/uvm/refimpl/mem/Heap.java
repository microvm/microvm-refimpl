package uvm.refimpl.mem;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import uvm.util.ErrorUtils;

public class Heap {

    protected Space space;
    protected Lock lock;
    protected Condition gcCanStart;
    protected Condition gcFinished;
    protected int liveMutators;
    protected int mutatorsStopped;
    protected boolean isDoingGC;
    protected volatile boolean globalPauseFlag;

    public Heap() {
        super();
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

    public void mutatorWaitForGCEnd() {
        lock.lock();
        try {
            mutatorsStopped += 1;
            if (mutatorsStopped == liveMutators) {
                gcCanStart.signal();
            }

            while (isDoingGC) {
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

    private void triggerGC() {
        lock.lock();
        try {
            isDoingGC = true;
            globalPauseFlag = true;
        } finally {
            lock.unlock();
        }
    }

    public void untriggerGC() {
        lock.lock();
        try {
            isDoingGC = false;
            globalPauseFlag = false;
        } finally {
            lock.unlock();
        }
    }

    public boolean getGlobalPauseFlag() {
        return globalPauseFlag;
    }

    public void collectorWaitForGCStart() throws InterruptedException {
        lock.lock();
        while (liveMutators == 0 || mutatorsStopped != liveMutators) {
            gcCanStart.await();
        }
        lock.unlock();
    }

    public void setOutOfMemory() {
        ErrorUtils.uvmError("Not implemented");

    }

}