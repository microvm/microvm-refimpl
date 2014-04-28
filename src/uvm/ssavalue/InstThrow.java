package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

/**
 * Throw an exception.
 */
public class InstThrow extends Instruction {
    
    /**
     * The exception object.
     */
    private UseBox exception;

    public InstThrow() {
        super();
    }

    public InstThrow(Value exception) {
        super();
        this.exception = use(exception);
    }

    public Value getException() {
        return exception.getDst();
    }

    public void setException(Value exception) {
        assertNotReset(this.exception);
        this.exception = use(exception);
    }

    @Override
    public int opcode() {
        return OpCode.THROW;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitThrow(this);
    }

}
