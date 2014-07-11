package uvm.refimpl.mem;


public abstract class Collector implements Runnable {

    protected Heap heap;

    public Collector() {
        super();
    }

    @Override
    public void run() {
        while (true) {
            park();
            collect();
        }
    }

    private void park() {
        heap.collectorWaitForGCStart();
    }

    protected abstract void collect();

}