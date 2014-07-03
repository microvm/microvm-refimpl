package uvm.refimpl.itpr;

import uvm.refimpl.mem.StackMemory;

public class InterpreterStack {
    private final int id;
    private final StackMemory stackMemory;

    private InterpreterFrame top;

    public InterpreterStack(int id, StackMemory stackMemory) {
        super();
        this.id = id;
        this.stackMemory = stackMemory;
    }

    public void kill() {
        // TODO Auto-generated method stub

    }

    // Getters and Setters

    public InterpreterFrame getTop() {
        return top;
    }

    public void setTop(InterpreterFrame top) {
        this.top = top;
    }

    public int getID() {
        return id;
    }

    public StackMemory getStackMemory() {
        return stackMemory;
    }

}
