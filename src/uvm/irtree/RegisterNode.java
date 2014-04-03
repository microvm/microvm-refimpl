package uvm.irtree;

import uvm.OpCode;

/**
 * A Register node denotes a Value which is already computed and stored in a
 * virtual register.
 * 
 * @param <T>
 */
public class RegisterNode<T> extends BaseIRTreeNode<T> {
    private ValueNode<T> valueNode;

    public RegisterNode() {
    }

    public RegisterNode(ValueNode<T> valueNode) {
        this.valueNode = valueNode;
    }

    public ValueNode<T> getValueNode() {
        return valueNode;
    }

    public void setValueNode(ValueNode<T> valueNode) {
        this.valueNode = valueNode;
    }

    @Override
    public int getOpCode() {
        return OpCode.REG;
    }

    @Override
    public int getID() {
        return valueNode.getID();
    }

    @Override
    public String getName() {
        return valueNode.getName();
    }

}
