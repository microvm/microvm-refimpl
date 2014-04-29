package uvm.ssavalue;

import java.util.ArrayList;
import java.util.List;

import uvm.BasicBlock;
import uvm.type.Type;

/**
 * The parent class of TRAP and WATCHPOINT.
 */
public abstract class AbstractTrap extends Instruction {

    /**
     * The type of this instruction when the control is returned from the
     * client.
     */
    private Type type;

    /**
     * The normal continuation.
     */
    private BasicBlock nor;

    /**
     * The exceptional continuation.
     */
    private BasicBlock exc;

    /**
     * Registers to be retained on the stack frame.
     */
    private List<UseBox> keepAlives = new ArrayList<UseBox>();

    protected AbstractTrap() {
    }

    public AbstractTrap(Type type, BasicBlock nor, BasicBlock exc,
            List<Value> keepAlives) {
        super();
        this.type = type;
        this.nor = nor;
        this.exc = exc;
        for (Value ka : keepAlives) {
            this.keepAlives.add(use(ka));
        }
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public List<UseBox> getKeepAlives() {
        return keepAlives;
    }

    /**
     * Add a new keep-alive register. A UseBox will be created.
     * 
     * @param ka
     *            The register.
     */
    public void addKeepAlives(Value ka) {
        this.keepAlives.add(use(ka));
    }

}
