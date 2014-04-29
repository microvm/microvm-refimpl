package uvm.ssavalue;

import uvm.IdentifiedHelper;
import uvm.type.Int;
import uvm.type.Type;

/**
 * Binary comparison.
 */
public class InstCmp extends Instruction {

    /**
     * The type of the operands.
     */
    private Type opndType;

    /**
     * The comparing operator
     */
    private CmpOptr optr;

    /**
     * The first operand
     */
    private UseBox op1;

    /**
     * The second operand
     */
    private UseBox op2;

    public InstCmp() {
    }

    public InstCmp(Type opndType, CmpOptr optr, Value op1, Value op2) {
        this.opndType = opndType;
        this.optr = optr;
        this.op1 = use(op1);
        this.op2 = use(op2);
    }

    private static Int INT1 = new Int(1);

    public Type getOpndType() {
        return opndType;
    }

    public void setOpndType(Type opndType) {
        this.opndType = opndType;
    }

    public CmpOptr getOptr() {
        return optr;
    }

    public void setOptr(CmpOptr optr) {
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
    public Type getType() {
        return INT1;
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
        return visitor.visitCmp(this);
    }
}
