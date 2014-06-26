package uvm.refimpl.itpr;

public class StackBox extends WrapperBox<InterpreterStack> {

    public InterpreterStack getStack() {
        return object;
    }

    public void setStack(InterpreterStack stack) {
        this.object = stack;
    }

}
