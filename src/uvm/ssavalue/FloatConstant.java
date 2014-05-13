package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

/**
 * Float (32-bit floating point number) constant.
 */
public class FloatConstant extends FPConstant {
    private float value;

    public FloatConstant() {
    }

    public FloatConstant(Type type, float value) {
        super(type);
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + getType() + " = " + value;
    }

    @Override
    public int opcode() {
        return OpCode.FLOAT_IMM;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitFloatConstant(this);
    }
}
