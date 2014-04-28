package uvm.ssavalue;

import java.util.List;

import uvm.BasicBlock;
import uvm.FunctionSignature;
import uvm.OpCode;

/**
 * A call anticipating exceptions.
 */
public class InstInvoke extends NonTailCall {

    /**
     * The normal continuation.
     */
    private BasicBlock nor;

    /**
     * The exceptional continuation.
     */
    private BasicBlock exc;

    public InstInvoke() {
    }

    public InstInvoke(FunctionSignature sig, Value func, List<Value> args,
            List<Value> keepAlives, BasicBlock nor, BasicBlock exc) {
        super(sig, func, args, keepAlives);
    }

    public BasicBlock getNor() {
        return nor;
    }

    public void setNor(BasicBlock nor) {
        this.nor = nor;
    }

    public BasicBlock getExc() {
        return exc;
    }

    public void setExc(BasicBlock exc) {
        this.exc = exc;
    }

    @Override
    public int opcode() {
        return OpCode.CALL;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitInvoke(this);
    }

}
