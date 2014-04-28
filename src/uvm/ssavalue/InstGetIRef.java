package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.IRef;
import uvm.type.Type;

/**
 * Convert a Ref to an IRef
 */
public class InstGetIRef extends Instruction {
    /**
     * The type the operand referes to.
     */
    private Type referentType;
    
    /**
     * The type of this instruction.
     */
    private Type type;

    /**
     * The operand.
     */
    private UseBox opnd;

    public InstGetIRef() {
    }

    public InstGetIRef(Type referentType, Value opnd) {
        super();
        this.referentType = referentType;
        this.opnd = use(opnd);
        this.type = new IRef(referentType);
    }

    public Type getReferentType() {
        return referentType;
    }

    public void setReferentType(Type referentType) {
        this.referentType = referentType;
        this.type = new IRef(referentType);
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
        return OpCode.GETIREF;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitGetIRef(this);
    }
}
