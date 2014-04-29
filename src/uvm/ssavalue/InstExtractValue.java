package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Struct;
import uvm.type.Type;

/**
 * Extract a field from a Struct typed SSA Value.
 */
public class InstExtractValue extends Instruction {
    /**
     * The type of the operand.
     */
    private Struct structType;

    /**
     * The index of the field to extract.
     */
    private int index;

    /**
     * The operand.
     */
    private UseBox opnd;

    public InstExtractValue() {
        super();
    }

    public InstExtractValue(Struct structType, int index, Value opnd) {
        super();
        this.structType = structType;
        this.index = index;
        this.opnd = use(opnd);
    }

    public Struct getStructType() {
        return structType;
    }

    public void setStructType(Struct structType) {
        this.structType = structType;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Value getOpnd() {
        return opnd.getDst();
    }

    public void setOpnd(Value opnd) {
        assertNotReset(this.opnd);
        this.opnd = use(opnd);
    }

    @Override
    public int opcode() {
        return OpCode.EXTRACTVALUE;
    }

    @Override
    public Type getType() {
        return structType.getFieldTypes().get(index);
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitExtractValue(this);
    }

}
