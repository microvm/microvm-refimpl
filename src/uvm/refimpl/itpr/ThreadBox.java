package uvm.refimpl.itpr;

public class ThreadBox extends ValueBox {
    private InterpreterThread thread;

    public InterpreterThread getThread() {
        return thread;
    }

    public void setThread(InterpreterThread thread) {
        this.thread = thread;
    }

}
