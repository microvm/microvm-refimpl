package uvm.refimpl.mem.simpleimmix;

import uvm.refimpl.mem.Heap;
import uvm.util.ErrorUtils;

public class SimpleImmixCollector implements Runnable {

    private Heap heap;

    public SimpleImmixCollector(Heap heap) {
        this.heap = heap;
    }

    @Override
    public void run() {
        while (true) {
            try {
                park();
            } catch (InterruptedException e) {
                break;
            }
            collect();
        }
        System.out.println("GC loop quitted upon interruption");
    }

    private void park() throws InterruptedException {
        heap.collectorWaitForGCStart();
    }

    private void collect() {
        heap.untriggerGC();
        ErrorUtils.uvmError("Collection not implemented.");
    }

}
