package uvm.refimpl.itpr;

public class ThreadBox extends WrapperBox<InterpreterThread> {

    public InterpreterThread getThread() {
        return object;
    }

    public void setThread(InterpreterThread thread) {
        this.object = thread;
    }

}
