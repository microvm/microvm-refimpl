package uvm.irtree;

import uvm.CFG;

/**
 * A Function node corresponds to a CFG in the uVM. Its immediate children are a
 * list of ValueNodes and/or LabelNodes. Not all instructions are immediate
 * children. If one instruction I1 is in the same basic block of another
 * instruction I2 and I1 is uniquely used by I2, and I1 does not have side
 * effect, then I1 becomes a child of I2.
 */
public class FunctionNode<T> extends BaseIRTreeNode<T> {
    private CFG cfg;

    public FunctionNode(CFG cfg) {
        this.cfg = cfg;
    }

    public CFG getCfg() {
        return cfg;
    }

    public void setCfg(CFG cfg) {
        this.cfg = cfg;
    }

    @Override
    public int getOpCode() {
        return -1;
    }

    @Override
    public int getID() {
        return cfg.getFunc().getID();
    }

    @Override
    public String getName() {
        return cfg.getFunc().getName();
    }

}
