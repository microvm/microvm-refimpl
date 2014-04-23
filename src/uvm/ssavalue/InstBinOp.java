package uvm.ssavalue;

import uvm.IdentifiedHelper;
import uvm.Instruction;
import uvm.type.Type;

/**
 * A binary operation.
 */
public class InstBinOp extends Instruction {
    /**
     * The expected type of the operands.
     */
    private Type type;
    /**
     * The operator.
     */
    private BinOptr optr;
    /**
     * The first operand.
     */
    private UseBox op1;
    /**
     * The second operand.
     */
    private UseBox op2;

    /**
     * Empty constructor. Conveinent for two-step JavaBean-style constructing.
     */
    public InstBinOp() {
    }

    public InstBinOp(Type type, BinOptr optr, Value op1, Value op2) {
        this.type = type;
        this.optr = optr;
        this.op1 = use(op1);
        this.op2 = use(op2);
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public BinOptr getOptr() {
        return optr;
    }

    public void setOptr(BinOptr optr) {
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
        return visitor.visitBinOp(this);
    }
}
