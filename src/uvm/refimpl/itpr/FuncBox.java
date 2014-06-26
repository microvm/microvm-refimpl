package uvm.refimpl.itpr;

import uvm.Function;

public class FuncBox extends WrapperBox<Function> {
    public Function getFunc() {
        return object;
    }

    public void setFunc(Function func) {
        this.object = func;
    }

}
