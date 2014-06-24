package uvm.refimpl.itpr;

public class StackBox extends ValueBox {
    private InterpreterStack stack;

    public InterpreterStack getStack() {
        return stack;
    }

    public void setStack(InterpreterStack stack) {
        this.stack = stack;
    }
    
}
