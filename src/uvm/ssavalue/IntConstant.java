package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Int;

/**
 * Integer constant.
 */
public class IntConstant extends Constant {
    /**
     * The concrete integer type.
     */
    private Int type;
    private long value;

    public IntConstant() {
    }

    public IntConstant(Int type, long value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public Int getType() {
        return this.type;
    }

    public void setType(Int type) {
        this.type = type;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + type + " = " + value;
    }

    @Override
    public int opcode() {
        return OpCode.INT_IMM;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitIntConstant(this);
    }
}
