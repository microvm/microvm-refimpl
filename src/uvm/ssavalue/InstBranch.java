package uvm.ssavalue;

import uvm.BasicBlock;
import uvm.IdentifiedHelper;
import uvm.OpCode;
import uvm.type.Type;

/**
 * An unconditional branching.
 */
public class InstBranch extends Instruction {

    /**
     * The destination.
     */
    private BasicBlock dest;

    public InstBranch() {
    }

    public InstBranch(BasicBlock dest) {
        this.dest = dest;
    }

    public BasicBlock getDest() {
        return dest;
    }

    public void setDest(BasicBlock dest) {
        this.dest = dest;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public int opcode() {
        return OpCode.BRANCH;
    }

    @Override
    public String toString() {
        return String.format("%s%s %s", getClass().getSimpleName(),
                IdentifiedHelper.repr(getDest()));
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitBranch(this);
    }

}
