package uvm.refimpl.itpr;

import uvm.Function;

public class FuncBox extends ValueBox {
    private Function func;

    public Function getFunc() {
        return func;
    }

    public void setFunc(Function func) {
        this.func = func;
    }

}
