package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.IRef;
import uvm.type.Struct;
import uvm.type.Type;

/**
 * Get the IRef to a field of a Struct.
 */
public class InstGetFieldIRef extends Instruction {
    /**
     * The type the operand referes to.
     */
    private Struct referentType;

    /**
     * The index of the field.
     */
    private int index = -1;

    /**
     * The operand.
     */
    private UseBox opnd;

    /**
     * The type of this instruction
     */
    private Type type;

    public InstGetFieldIRef() {
    }

    public InstGetFieldIRef(Struct referentType, int index, Value opnd) {
        super();
        this.referentType = referentType;
        this.index = index;
        this.opnd = use(opnd);
        tryInitialiseType();
    }

    /**
     * Initialise the type field if both the referent type and the index are
     * set.
     */
    private void tryInitialiseType() {
        if (this.referentType != null && this.index != -1) {
            this.type = new IRef(referentType.getFieldTypes().get(index));
        }
    }

    public Struct getReferentType() {
        return referentType;
    }

    public void setReferentType(Struct referentType) {
        this.referentType = referentType;
        tryInitialiseType();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        tryInitialiseType();
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
        return this.type;
    }

    @Override
    public int opcode() {
        return OpCode.GETFIELDIREF;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitGetFieldIRef(this);
    }
}
