package uvm.type;

import uvm.FunctionSignature;

public class Func extends Type {
    private FunctionSignature sig;

    public Func() {
        super();
    }

    public Func(FunctionSignature sig) {
        super();
        this.sig = sig;
    }

    public FunctionSignature getSig() {
        return sig;
    }

    public void setSig(FunctionSignature sig) {
        this.sig = sig;
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitFunc(this);
    }
}
