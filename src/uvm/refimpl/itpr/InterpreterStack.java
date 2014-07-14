package uvm.refimpl.itpr;

import uvm.refimpl.mem.StackMemory;

public class InterpreterStack {
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int DEAD = 3;

    private final int id;
    private final StackMemory stackMemory;

    private InterpreterFrame top;

    private int state;

    public InterpreterStack(int id, StackMemory stackMemory) {
        super();
        this.id = id;
        this.stackMemory = stackMemory;
        this.state = READY;
    }

    public void swapTo() {
        state = RUNNING;
    }

    public void swapFrom() {
        state = READY;
    }

    public void kill() {
        state = DEAD;
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

    public int getState() {
        return state;
    }

}
