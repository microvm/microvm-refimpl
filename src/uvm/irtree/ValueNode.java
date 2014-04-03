package uvm.irtree;

import uvm.Value;

public class ValueNode<T> extends BaseIRTreeNode<T> {
    private Value value;

    public ValueNode() {
    }

    public ValueNode(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public int getOpCode() {
        return value.opcode();
    }

    @Override
    public int getID() {
        return value.getID();
    }

    @Override
    public String getName() {
        return value.getName();
    }
}
