package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Hybrid;
import uvm.type.Ref;

/**
 * Allocate Hybrid on the heap.
 */
public class InstNewHybrid extends Instruction {

    /**
     * The type to allocate.
     */
    private Hybrid allocType;

    /**
     * The length of the variable part of the Hybrid.
     */
    private int length;

    /**
     * The type of this instruction.
     */
    private Ref type;

    public InstNewHybrid() {
        super();
    }

    public InstNewHybrid(Hybrid allocType, int length) {
        super();
        this.allocType = allocType;
        this.type = new Ref(allocType);
        this.length = length;
    }

    public Hybrid getAllocType() {
        return allocType;
    }

    public void setAllocType(Hybrid allocType) {
        this.allocType = allocType;
        this.type = new Ref(allocType);
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public Ref getType() {
        return this.type;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitNewHybrid(this);
    }

    @Override
    public int opcode() {
        return OpCode.NEWHYBRID;
    }

}
