package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Array;
import uvm.type.Type;

/**
 * Get the IRef to an element of an Array.
 */
public class InstGetElemIRef extends Instruction {
    /**
     * The type the operand referes to.
     */
    private Array referentType;
    
    /**
     * The index of the field.
     */
    private UseBox index;

    /**
     * The operand.
     */
    private UseBox opnd;

    public InstGetElemIRef() {
    }

    public InstGetElemIRef(Array referentType, Value index, Value opnd) {
        super();
        this.referentType = referentType;
        this.index = use(index);
        this.opnd = use(opnd);
    }

    public Array getReferentType() {
        return referentType;
    }

    public void setReferentType(Array referentType) {
        this.referentType = referentType;
    }

    public Value getIndex() {
        return index.getDst();
    }

    public void setIndex(Value index) {
        assertNotReset(this.index);
        this.index = use(index);
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
        return referentType.getElemType();
    }

    @Override
    public int opcode() {
        return OpCode.GETFIELDIREF;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitGetElemIRef(this);
    }
}
