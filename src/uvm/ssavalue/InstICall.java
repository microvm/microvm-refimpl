package uvm.ssavalue;

import java.util.List;

import uvm.OpCode;
import uvm.intrinsicfunc.IntrinsicFunction;

/**
 * Call an intrinsic function.
 */
public class InstICall extends AbstractIntrinsicCall {

    public InstICall() {
    }

    public InstICall(IntrinsicFunction intrinsicFunction, List<Value> args,
            List<Value> keepAlives) {
        super(intrinsicFunction, args, keepAlives);
    }

    @Override
    public int opcode() {
        return OpCode.ICALL;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitICall(this);
    }
}
