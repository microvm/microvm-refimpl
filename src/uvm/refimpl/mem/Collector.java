package uvm.refimpl.mem;

public abstract class Collector implements Runnable {

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
        getHeap().collectorWaitForGCStart();
    }

    protected abstract Heap getHeap();

    protected abstract void collect();

}