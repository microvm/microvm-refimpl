package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.UseBox;
import uvm.Value;

/**
 * The Ret instruction returns from the current function, carrying a value.
 */
public class InstRet extends Instruction {
    /**
     * The return type
     */
    private Type type;

    /**
     * The return value.
     */
    private UseBox retVal;

    public InstRet() {
    }

    public InstRet(Type type, Value retVal) {
        this.type = type;
        this.retVal = use(retVal);
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
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    
    @Override
    public int opcode() {
        return OpCode.RET;
    }
}
