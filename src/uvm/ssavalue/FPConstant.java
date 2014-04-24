package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

public class FPConstant extends Constant {
    private Type type;
    private double value;

    public FPConstant() {
    }

    public FPConstant(Type type, double value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + type + " = " + value;
    }

    @Override
    public int opcode() {
        return OpCode.FP_IMM;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitFPConstant(this);
    }
}
