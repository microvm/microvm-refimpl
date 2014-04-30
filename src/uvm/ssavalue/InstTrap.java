package uvm.ssavalue;

import uvm.OpCode;

/**
 * Unconditionally transfer the control to the client.
 */
public class InstTrap extends AbstractTrap {

    @Override
    public int opcode() {
        return OpCode.TRAP;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitTrap(this);
    }

}
