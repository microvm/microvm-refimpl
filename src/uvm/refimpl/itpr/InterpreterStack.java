package uvm.refimpl.itpr;

public class InterpreterStack {
    private InterpreterFrame top;

    public InterpreterFrame getTop() {
        return top;
    }

    public void setTop(InterpreterFrame top) {
        this.top = top;
    }
    
}
