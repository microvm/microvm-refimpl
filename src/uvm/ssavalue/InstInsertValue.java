package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Struct;
import uvm.type.Type;

/**
 * Make a new Struct typed SSA Value with only one field different.
 */
public class InstInsertValue extends Instruction {
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

    /**
     * The new value of the field.
     */
    private UseBox newVal;

    public InstInsertValue() {
        super();
    }

    public InstInsertValue(Struct structType, int index, Value opnd,
            Value newVal) {
        super();
        this.structType = structType;
        this.index = index;
        this.opnd = use(opnd);
        this.newVal = use(newVal);
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

    public Value getNewVal() {
        return newVal.getDst();
    }

    public void setNewVal(Value newVal) {
        assertNotReset(this.newVal);
        this.newVal = use(newVal);
    }

    @Override
    public int opcode() {
        return OpCode.INSERTVALUE;
    }

    @Override
    public Type getType() {
        return structType;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitInsertValue(this);
    }

    /**
     * @return The type of the inserted field.
     */
    public Type getFieldType() {
        return structType.getFieldTypes().get(index);
    }

}
