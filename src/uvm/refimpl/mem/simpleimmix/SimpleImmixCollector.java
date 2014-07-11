package uvm.refimpl.mem.simpleimmix;

import uvm.refimpl.mem.Collector;
import uvm.refimpl.mem.Heap;
import uvm.util.ErrorUtils;

public class SimpleImmixCollector extends Collector implements Runnable {

    public SimpleImmixCollector(Heap heap) {
        this.heap = heap;
    }

    @Override
    protected void collect() {
        ErrorUtils.uvmError("Out of memory");
        System.exit(0);
        heap.untriggerGC();
    }
}
