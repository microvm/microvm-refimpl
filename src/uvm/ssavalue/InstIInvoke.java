package uvm.ssavalue;

import java.util.List;

import uvm.BasicBlock;
import uvm.OpCode;
import uvm.intrinsicfunc.IntrinsicFunction;

/**
 * Call an intrinsic function.
 */
public class InstIInvoke extends AbstractIntrinsicCall {

    /**
     * The normal continuation.
     */
    private BasicBlock nor;

    /**
     * The exceptional continuation.
     */
    private BasicBlock exc;

    public InstIInvoke() {
    }

    public InstIInvoke(IntrinsicFunction intrinsicFunction, List<Value> args,
            BasicBlock nor, BasicBlock exc, List<Value> keepAlives) {
        super(intrinsicFunction, args, keepAlives);
        this.nor = nor;
        this.exc = exc;
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
        return OpCode.IINVOKE;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitIInvoke(this);
    }
}
