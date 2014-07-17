package uvm.refimpl.mem;

public abstract class Collector implements Runnable {

    public Collector() {
        super();
    }

    @Override
    public void run() {
        try {
            while (true) {
                park();
                collect();
            }
        } catch (Exception e) {
            System.err.println("Error thrown from collection thread.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void park() {
        getHeap().collectorWaitForGCStart();
    }

    protected abstract Heap getHeap();

    protected abstract void collect();

}