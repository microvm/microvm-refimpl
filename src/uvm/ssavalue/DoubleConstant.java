package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

/**
 * Double (64-bit floating point number) constant.
 */
public class DoubleConstant extends FPConstant {
    private double value;

    public DoubleConstant() {
    }

    public DoubleConstant(Type type, double value) {
        super(type);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + getType() + " = " + value;
    }

    @Override
    public int opcode() {
        return OpCode.DOUBLE_IMM;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitDoubleConstant(this);
    }
}
