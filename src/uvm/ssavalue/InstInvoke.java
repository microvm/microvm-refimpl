package uvm.ssavalue;

import java.util.List;

import uvm.BasicBlock;
import uvm.FunctionSignature;
import uvm.OpCode;

/**
 * A call anticipating exceptions.
 */
public class InstInvoke extends NonTailCall implements HandlesException {

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
        this.nor = nor;
        this.exc = exc;
    }

    @Override
    public BasicBlock getNor() {
        return nor;
    }

    @Override
    public void setNor(BasicBlock nor) {
        this.nor = nor;
    }

    @Override
    public BasicBlock getExc() {
        return exc;
    }

    @Override
    public void setExc(BasicBlock exc) {
        this.exc = exc;
    }

    @Override
    public int opcode() {
        return OpCode.INVOKE;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitInvoke(this);
    }

}
