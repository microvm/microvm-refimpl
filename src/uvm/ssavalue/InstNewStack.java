package uvm.ssavalue;

import java.util.ArrayList;
import java.util.List;

import uvm.FunctionSignature;
import uvm.OpCode;
import uvm.type.Stack;
import uvm.type.Type;

/**
 * Create a new stack with a suspended function activation at the bottom.
 */
public class InstNewStack extends Instruction {

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

    protected InstNewStack() {
    }

    public InstNewStack(FunctionSignature sig, Value func, List<Value> args) {
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

    private static Stack STACK_TYPE;

    @Override
    public Type getType() {
        return STACK_TYPE;
    }

    @Override
    public int opcode() {
        return OpCode.NEWSTACK;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitNewStack(this);
    }
}
