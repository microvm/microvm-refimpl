package uvm.inst;

import uvm.BasicBlock;
import uvm.IdentifiedHelper;
import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.UseBox;
import uvm.Value;
import uvm.ValueVisitor;

/**
 * A binary conditional branch. Branch to ifTrue if cond is true, or branch to
 * ifFalse otherwise.
 */
public class InstBranch2 extends Instruction {
    /**
     * The condition which decides the destination.
     */
    private UseBox cond;
    /**
     * The BasicBlock to branch to if cond is 1.
     */
    private BasicBlock ifTrue;
    /**
     * The BasicBlock to branch to if cond is 0.
     */
    private BasicBlock ifFalse;

    public InstBranch2() {
    }

    public InstBranch2(Value cond, BasicBlock ifTrue, BasicBlock ifFalse) {
        this.cond = use(cond);
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    public Value getCond() {
        return cond.getDst();
    }

    public void setCond(Value cond) {
        assertNotReset(this.cond);
        this.cond = use(cond);
    }

    public BasicBlock getIfTrue() {
        return ifTrue;
    }

    public void setIfTrue(BasicBlock ifTrue) {
        this.ifTrue = ifTrue;
    }

    public BasicBlock getIfFalse() {
        return ifFalse;
    }

    public void setIfFalse(BasicBlock ifFalse) {
        this.ifFalse = ifFalse;
    }

    @Override
    public Type getType() {
        return null;
    }
    
    @Override
    public int opcode() {
        return OpCode.BRANCH2;
    }

    @Override
    public String toString() {
        return String.format("%s%s %s %s %s", getClass().getSimpleName(),
                IdentifiedHelper.repr(this),
                IdentifiedHelper.repr(getCond()),
                IdentifiedHelper.repr(getIfTrue()),
                IdentifiedHelper.repr(getIfFalse()));
    }
    
    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitBranch2(this);
    }
}
