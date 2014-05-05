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
     * The operand
     */
    private UseBox opnd;

    public InstConversion() {
    }

    public InstConversion(Type fromType, Type toType, ConvOptr optr, Value opnd) {
        this.fromType = fromType;
        this.toType = toType;
        this.optr = optr;
        this.opnd = use(opnd);
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

    public Value getOpnd() {
        return opnd.getDst();
    }

    public void setOpnd(Value opnd) {
        assertNotReset(this.opnd);
        this.opnd = use(opnd);
    }

    @Override
    public Type getType() {
        return toType;
    }

    @Override
    public int opcode() {
        return optr.getOpCode();
    }

    @Override
    public String toString() {
        return String.format("%s%s %s %s %s", getClass().getSimpleName(),
                IdentifiedHelper.repr(this), optr.toString(),
                IdentifiedHelper.repr(getOpnd()));
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitConversion(this);
    }
}
