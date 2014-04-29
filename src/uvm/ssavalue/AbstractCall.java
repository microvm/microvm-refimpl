package uvm.ssavalue;

import java.util.ArrayList;
import java.util.List;

import uvm.FunctionSignature;
import uvm.type.Type;

/**
 * The parent class of all calls to MicroVM functions. These include CALL,
 * INVOKE and TAILCALL.
 */
public abstract class AbstractCall extends Instruction {

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

    protected AbstractCall() {
    }

    public AbstractCall(FunctionSignature sig, Value func, List<Value> args) {
        super();
        this.sig = sig;
        this.func = use(func);
        for (Value arg : args) {
            this.args.add(use(arg));
        }
    }

    public FunctionSignature getSig() {
        return sig;
    }

    public void setSig(FunctionSignature sig) {
        this.sig = sig;
    }

    public Value getFunc() {
        return func.getDst();
    }

    public void setFunc(Value func) {
        assertNotReset(this.func);
        this.func = use(func);
    }

    public List<UseBox> getArgs() {
        return args;
    }

    public void addArg(Value arg) {
        this.args.add(use(arg));
    }

    @Override
    public Type getType() {
        return this.sig.getReturnType();
    }
}