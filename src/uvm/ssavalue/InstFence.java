package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

/**
 * An explicit memory fence.
 */
public class InstFence extends Instruction {

    /**
     * Requirement of memory ordering.
     */
    private AtomicOrdering ordering;

    public InstFence() {
        super();
    }

    public InstFence(AtomicOrdering ordering) {
        super();
        this.ordering = ordering;
    }

    public AtomicOrdering getOrdering() {
        return ordering;
    }

    public void setOrdering(AtomicOrdering ordering) {
        this.ordering = ordering;
    }

    @Override
    public int opcode() {
        return OpCode.FENCE;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitFence(this);
    }

}
