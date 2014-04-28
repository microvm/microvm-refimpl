package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

/**
 * Conditional moving.
 */
public class InstSelect extends Instruction {
    /**
     * The type of this instruction.
     */
    private Type type;

    /**
     * The condition.
     */
    private UseBox cond;

    /**
     * The result when cond is 1.
     */
    private UseBox ifTrue;

    /**
     * The result when cond is 0.s
     */
    private UseBox ifFalse;

    public InstSelect() {
    }

    public InstSelect(Type type, Value cond, Value ifTrue, Value ifFalse) {
        super();
        this.type = type;
        this.cond = use(cond);
        this.ifTrue = use(ifTrue);
        this.ifFalse = use(ifFalse);
    }

    @Override
    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Value getCond() {
        return this.cond.getDst();
    }

    public void setCond(Value cond) {
        assertNotReset(this.cond);
        this.cond = use(cond);
    }

    public Value getIfTrue() {
        return ifTrue.getDst();
    }

    public void setIfTrue(Value ifTrue) {
        assertNotReset(this.ifTrue);
        this.ifTrue = use(ifTrue);
    }

    public Value getIfFalse() {
        return ifFalse.getDst();
    }

    public void setIfFalse(Value ifFalse) {
        assertNotReset(this.ifFalse);
        this.ifFalse = use(ifFalse);
    }

    @Override
    public int opcode() {
        return OpCode.SELECT;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitSelect(this);
    }

}
