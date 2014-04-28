package uvm.ssavalue;

import java.util.List;

import uvm.FunctionSignature;
import uvm.OpCode;

/**
 * A normal call.
 */
public class InstCall extends NonTailCall {
    public InstCall() {
    }

    public InstCall(FunctionSignature sig, Value func, List<Value> args,
            List<Value> keepAlives) {
        super(sig, func, args, keepAlives);
    }

    @Override
    public int opcode() {
        return OpCode.CALL;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitCall(this);
    }

}
