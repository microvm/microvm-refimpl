package uvm.refimpl.itpr;

public class InterpreterStack {
    private int id;

    public InterpreterStack(int id) {
        super();
        this.id = id;
    }

    private InterpreterFrame top;

    public InterpreterFrame getTop() {
        return top;
    }

    public void setTop(InterpreterFrame top) {
        this.top = top;
    }

    public int getID() {
        return id;
    }

}
