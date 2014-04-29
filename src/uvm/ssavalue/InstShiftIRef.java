package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.IRef;
import uvm.type.Type;

/**
 * Moves an IRef forward by the offset of a given number of instances.
 */
public class InstShiftIRef extends Instruction {
    /**
     * The type the operand referes to.
     */
    private Type referentType;

    /**
     * The type of this instruction
     */
    private Type type;

    /**
     * The index of the field.
     */
    private UseBox offset;

    /**
     * The operand.
     */
    private UseBox opnd;

    public InstShiftIRef() {
    }

    public InstShiftIRef(Type referentType, Value index, Value opnd) {
        super();
        this.referentType = referentType;
        this.type = new IRef(referentType);
        this.offset = use(index);
        this.opnd = use(opnd);
    }

    public Type getReferentType() {
        return referentType;
    }

    public void setReferentType(Type referentType) {
        this.referentType = referentType;
        this.type = new IRef(referentType);
    }

    public Value getOffset() {
        return offset.getDst();
    }

    public void setOffset(Value offset) {
        assertNotReset(this.offset);
        this.offset = use(offset);
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
        return OpCode.SHIFTIREF;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitShiftIRef(this);
    }
}
