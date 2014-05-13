package uvm.ssavalue;

import uvm.type.Type;

/**
 * Super class of floating point number constant, i.e. FloatConstant and
 * DoubleConstant.
 */
public abstract class FPConstant extends Constant {
    private Type type;

    public FPConstant() {
    }

    public FPConstant(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}
