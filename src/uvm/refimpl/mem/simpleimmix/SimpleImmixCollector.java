package uvm.refimpl.mem.simpleimmix;

import uvm.util.ErrorUtils;

public class SimpleImmixCollector implements Runnable {

    private SimpleImmixHeap heap;

    public SimpleImmixCollector(SimpleImmixHeap heap) {
        this.heap = heap;
    }

    @Override
    public void run() {
        while (true) {
            park();
            collect();
        }

    }

    private void park() {
        heap.lock.lock();
        while (heap.liveMutators == 0
                || heap.mutatorsStopped != heap.liveMutators) {
            try {
                heap.gcCanStart.await();
            } catch (InterruptedException e) {
                break;
            }
        }
        heap.lock.unlock();
    }

    private void collect() {
        ErrorUtils.uvmError("Collection not implemented.");
    }

}
