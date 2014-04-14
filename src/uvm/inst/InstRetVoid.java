package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.ValueVisitor;

/**
 * The RetVoid instruction returns from the current function of void return
 * type.
 */
public class InstRetVoid extends Instruction {
    public InstRetVoid() {
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public int opcode() {
        return OpCode.RETVOID;
    }
    
    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitRetVoid(this);
    }
}
