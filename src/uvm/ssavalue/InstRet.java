package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

/**
 * The Ret instruction returns from the current function, carrying a value.
 */
public class InstRet extends Instruction {
    /**
     * The return type
     */
    private Type retType;

    /**
     * The return value.
     */
    private UseBox retVal;

    public InstRet() {
    }

    public InstRet(Type retType, Value retVal) {
        this.retType = retType;
        this.retVal = use(retVal);
    }

    public Type getRetType() {
        return this.retType;
    }

    public void setRetType(Type retType) {
        this.retType = retType;
    }

    public Value getRetVal() {
        return retVal.getDst();
    }

    public void setRetVal(Value retVal) {
        assertNotReset(this.retVal);
        this.retVal = use(retVal);
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public int opcode() {
        return OpCode.RET;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitRet(this);
    }
}
