package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Ref;
import uvm.type.Type;

/**
 * Used to receive a thrown exception.
 */
public class InstLandingPad extends Instruction {
    public InstLandingPad() {
    }
    
    private static Type REFVOID = new Ref(new uvm.type.Void());

    @Override
    public Type getType() {
        return REFVOID;
    }

    @Override
    public int opcode() {
        return OpCode.RETVOID;
    }
    
    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitLandingPad(this);
    }
}
