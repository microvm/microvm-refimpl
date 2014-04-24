package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

public class NullConstant extends Constant {
    private Type type;

    public NullConstant() {
    }

    public NullConstant(Type type, long value) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + type + " = NULL";
    }

    @Override
    public int opcode() {
        return OpCode.NULL_IMM;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitNullConstant(this);
    }
}
