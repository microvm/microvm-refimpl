package uvm.ssavalue;

import java.util.ArrayList;
import java.util.List;

import uvm.FunctionSignature;
import uvm.OpCode;
import uvm.type.Type;

/**
 * Call a C function.
 */
public class InstCCall extends Instruction implements CallLike {

    /**
     * The calling convention.
     */
    private CallConv callConv;

    /**
     * The signature of the callee.
     */
    private FunctionSignature sig;

    /**
     * The callee.
     */
    private UseBox func;

    /**
     * Arguments
     */
    private List<UseBox> args = new ArrayList<UseBox>();

    public InstCCall() {
    }

    public InstCCall(CallConv callConv, FunctionSignature sig, Value func,
            List<Value> args) {
        super();
        this.callConv = callConv;
        this.sig = sig;
        this.func = use(func);
        for (Value arg : args) {
            this.args.add(use(arg));
        }
    }

    @Override
    public Type getType() {
        return this.sig.getReturnType();
    }

    public CallConv getCallConv() {
        return callConv;
    }

    public void setCallConv(CallConv callConv) {
        this.callConv = callConv;
    }

    @Override
    public FunctionSignature getSig() {
        return sig;
    }

    @Override
    public void setSig(FunctionSignature sig) {
        this.sig = sig;
    }

    @Override
    public Value getFunc() {
        return func.getDst();
    }

    @Override
    public void setFunc(Value func) {
        assertNotReset(this.func);
        this.func = use(func);
    }

    @Override
    public List<UseBox> getArgs() {
        return args;
    }

    @Override
    public void addArg(Value arg) {
        this.args.add(use(arg));
    }

    @Override
    public int opcode() {
        return OpCode.CCALL;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitCCall(this);
    }
}
