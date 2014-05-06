package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Hybrid;
import uvm.type.IRef;
import uvm.type.Type;

/**
 * Get the IRef to the first element of the variable part of a Hybrid.
 */
public class InstGetVarPartIRef extends Instruction {
    /**
     * The type the operand referes to.
     */
    private Hybrid referentType;

    /**
     * The type of this instruction
     */
    private Type type;

    /**
     * The operand.
     */
    private UseBox opnd;

    public InstGetVarPartIRef() {
    }

    public InstGetVarPartIRef(Hybrid referentType, Value opnd) {
        super();
        this.referentType = referentType;
        this.type = new IRef(referentType.getVarPart());
        this.opnd = use(opnd);
    }

    public Hybrid getReferentType() {
        return referentType;
    }

    public void setReferentType(Hybrid referentType) {
        this.referentType = referentType;
        this.type = new IRef(referentType.getVarPart());
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
        return type;
    }

    @Override
    public int opcode() {
        return OpCode.GETVARPARTIREF;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitGetVarPartIRef(this);
    }
}
