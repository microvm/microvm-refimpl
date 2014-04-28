package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.IRef;
import uvm.type.Type;

/**
 * Allocate scalar on the stack.
 */
public class InstAlloca extends Instruction {

    /**
     * The type to allocate.
     */
    private Type allocType;

    /**
     * The type of this instruction.
     */
    private IRef type;

    public InstAlloca() {
        super();
    }

    public InstAlloca(Type allocType) {
        super();
        this.allocType = allocType;
        this.type = new IRef(allocType);
    }

    public Type getAllocType() {
        return allocType;
    }

    public void setAllocType(Type allocType) {
        this.allocType = allocType;
        this.type = new IRef(allocType);
    }

    @Override
    public IRef getType() {
        return this.type;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitAlloca(this);
    }

    @Override
    public int opcode() {
        return OpCode.ALLOCA;
    }

}
