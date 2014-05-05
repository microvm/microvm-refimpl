package uvm.ssavalue;

import java.util.ArrayList;
import java.util.List;

import uvm.FunctionSignature;

/**
 * The parent class of all non-tail calls to MicroVM functions. These include
 * CALL and INVOKE.
 */
public abstract class NonTailCall extends AbstractCall implements HasKeepAlives {

    /**
     * Registers to be retained on the stack frame.
     */
    private List<UseBox> keepAlives = new ArrayList<UseBox>();

    protected NonTailCall() {
    }

    protected NonTailCall(FunctionSignature sig, Value func, List<Value> args,
            List<Value> keepAlives) {
        super(sig, func, args);
        for (Value ka : keepAlives) {
            this.keepAlives.add(use(ka));
        }
    }

    @Override
    public List<UseBox> getKeepAlives() {
        return keepAlives;
    }

    /**
     * Add a new keep-alive register. A UseBox will be created.
     * 
     * @param ka
     *            The register.
     */
    @Override
    public void addKeepAlive(Value ka) {
        this.keepAlives.add(use(ka));
    }
}
