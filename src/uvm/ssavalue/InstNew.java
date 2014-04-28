package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Ref;
import uvm.type.Type;

/**
 * Allocate scalar on the heap.
 */
public class InstNew extends Instruction {

    /**
     * The type to allocate.
     */
    private Type allocType;

    /**
     * The type of this instruction.
     */
    private Ref type;

    public InstNew() {
        super();
    }

    public InstNew(Type allocType) {
        super();
        this.allocType = allocType;
        this.type = new Ref(allocType);
    }

    public Type getAllocType() {
        return allocType;
    }

    public void setAllocType(Type allocType) {
        this.allocType = allocType;
        this.type = new Ref(allocType);
    }

    @Override
    public Ref getType() {
        return this.type;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitNew(this);
    }

    @Override
    public int opcode() {
        return OpCode.NEW;
    }

}
