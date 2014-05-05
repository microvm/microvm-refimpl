package uvm.ssavalue;

import java.util.ArrayList;
import java.util.List;

import uvm.intrinsicfunc.IntrinsicFunction;
import uvm.type.Type;

public abstract class AbstractIntrinsicCall extends Instruction implements
        HasArgs, HasKeepAlives {

    /**
     * The intrinsic function object.
     */
    protected IntrinsicFunction intrinsicFunction;

    /**
     * Arguments
     */
    protected List<UseBox> args = new ArrayList<UseBox>();

    /**
     * Registers to be retained on the stack frame.
     */
    private List<UseBox> keepAlives = new ArrayList<UseBox>();

    protected AbstractIntrinsicCall() {
        super();
    }

    protected AbstractIntrinsicCall(IntrinsicFunction intrinsicFunction,
            List<Value> args, List<Value> keepAlives) {
        super();
        this.intrinsicFunction = intrinsicFunction;
        for (Value arg : args) {
            this.args.add(use(arg));
        }
        for (Value ka : keepAlives) {
            this.keepAlives.add(use(ka));
        }
    }

    public IntrinsicFunction getIntrinsicFunction() {
        return intrinsicFunction;
    }

    public void setIntrinsicFunction(IntrinsicFunction intrinsicFunction) {
        this.intrinsicFunction = intrinsicFunction;
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
    public List<UseBox> getKeepAlives() {
        return keepAlives;
    }

    @Override
    public void addKeepAlive(Value ka) {
        this.keepAlives.add(use(ka));
    }

    @Override
    public Type getType() {
        return intrinsicFunction.getType();
    }

}