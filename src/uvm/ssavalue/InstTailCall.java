package uvm.ssavalue;

import java.util.List;

import uvm.FunctionSignature;
import uvm.OpCode;
import uvm.type.Type;

/**
 * A tail call.
 */
public class InstTailCall extends AbstractCall {
    public InstTailCall() {
    }

    public InstTailCall(FunctionSignature sig, Value func, List<Value> args) {
        super(sig, func, args);
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public int opcode() {
        return OpCode.TAILCALL;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitTailCall(this);
    }

}
