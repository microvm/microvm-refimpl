package uvm.ssavalue;

import uvm.FunctionSignature;
import uvm.OpCode;
import uvm.type.Type;

/**
 * A parameter to a function. It is a subclass of Instruction because currently
 * we write it in the entry basic block.
 */
public class Parameter extends Instruction {
    private FunctionSignature sig;
    private int paramIndex;

    public Parameter(FunctionSignature sig, int paramIndex) {
        super();
        this.sig = sig;
        this.paramIndex = paramIndex;
    }

    @Override
    public Type getType() {
        return sig.getParamTypes().get(paramIndex);
    }
    
    @Override
    public int opcode() {
        return OpCode.PARAM;
    }
    
    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitParameter(this);
    }
}
