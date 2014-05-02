package uvm.intrinsicfunc;

import uvm.type.Type;

public class GenericIntrinsicFunction implements IntrinsicFunction {
    private Type type;

    public GenericIntrinsicFunction(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }
}
