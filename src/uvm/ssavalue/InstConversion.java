package uvm.ssavalue;

import uvm.IdentifiedHelper;
import uvm.type.Type;

/**
 * Type conversion. Convert from one type to another.
 */
public class InstConversion extends Instruction {

    /**
     * The type of the operand
     */
    private Type fromType;

    /**
     * The type to convert to
     */
    private Type toType;

    /**
     * The conversion operator
     */
    private ConvOptr optr;
    /**
     * The first operand
     */
    private UseBox op1;
    /**
     * The second operand
     */
    private UseBox op2;

    public InstConversion() {
    }

    public InstConversion(Type fromType, Type toType, ConvOptr optr, Value op1,
            Value op2) {
        this.fromType = fromType;
        this.toType = toType;
        this.optr = optr;
        this.op1 = use(op1);
        this.op2 = use(op2);
    }

    @Override
    public Type getType() {
        return toType;
    }

    public Type getFromType() {
        return fromType;
    }

    public void setFromType(Type fromType) {
        this.fromType = fromType;
    }

    public Type getToType() {
        return toType;
    }

    public void setToType(Type toType) {
        this.toType = toType;
    }

    public ConvOptr getOptr() {
        return optr;
    }

    public void setOptr(ConvOptr optr) {
        this.optr = optr;
    }

    public Value getOp1() {
        return op1.getDst();
    }

    public void setOp1(Value op1) {
        assertNotReset(this.op1);
        this.op1 = use(op1);
    }

    public Value getOp2() {
        return op2.getDst();
    }

    public void setOp2(Value op2) {
        assertNotReset(this.op2);
        this.op2 = use(op2);
    }

    @Override
    public int opcode() {
        return optr.getOpCode();
    }

    @Override
    public String toString() {
        return String.format("%s%s %s %s %s", getClass().getSimpleName(),
                IdentifiedHelper.repr(this), optr.toString(),
                IdentifiedHelper.repr(getOp1()),
                IdentifiedHelper.repr(getOp2()));
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitConversion(this);
    }
}
