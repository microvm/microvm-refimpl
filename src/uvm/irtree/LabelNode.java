package uvm.irtree;

import uvm.BasicBlock;
import uvm.OpCode;

public class LabelNode<T> extends BaseIRTreeNode<T> {

    private BasicBlock basicBlock;

    public LabelNode() {
    }

    public LabelNode(BasicBlock bb) {
        this.basicBlock = bb;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public void setBasicBlock(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    @Override
    public int getOpCode() {
        return OpCode.LABEL;
    }

    @Override
    public int getID() {
        return basicBlock.getID();
    }

    @Override
    public String getName() {
        return basicBlock.getName();
    }

}
