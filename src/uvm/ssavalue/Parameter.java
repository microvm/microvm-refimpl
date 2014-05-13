package uvm.ssavalue;

import uvm.FunctionSignature;
import uvm.OpCode;
import uvm.type.Type;

/**
 * A parameter to a function. It is not an instruction, but implemented as a
 * subclass of Instruction so that in later control-flow analysis it can be
 * added into the entry block for the ease of analysis.
 */
public class Parameter extends Instruction {
    /**
     * The signature of the function it is in.
     */
    private FunctionSignature sig;
    
    /**
     * The index this parameter is in the parameter list.
     */
    private int paramIndex;

    public Parameter() {
    }

    public Parameter(FunctionSignature sig, int paramIndex) {
        super();
        this.sig = sig;
        this.paramIndex = paramIndex;
    }

    public FunctionSignature getSig() {
        return sig;
    }

    public void setSig(FunctionSignature sig) {
        this.sig = sig;
    }

    public int getParamIndex() {
        return paramIndex;
    }

    public void setParamIndex(int paramIndex) {
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
