package uvm.refimpl.itpr;

import java.util.HashMap;

import uvm.Function;
import uvm.ssavalue.Value;

public class InterpreterFrame {
    private Function func;
    private HashMap<Value, ValueBox> valueDict = new HashMap<Value, ValueBox>();

    public InterpreterFrame(Function func) {
        this.func = func;
    }
}
