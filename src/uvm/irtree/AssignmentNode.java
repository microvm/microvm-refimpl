package uvm.irtree;

import uvm.OpCode;

public class AssignmentNode<T> extends BaseIRTreeNode<T> {
    private RegisterNode<T> register;
    private ValueNode<T> value;

    public AssignmentNode() {
    }

    public AssignmentNode(RegisterNode<T> register, ValueNode<T> value) {
        this.register = register;
        this.value = value;
        this.getChildren().add(register);
        this.getChildren().add(value);
    }

    public RegisterNode<T> getRegister() {
        return register;
    }

    public void setRegister(RegisterNode<T> register) {
        this.register = register;
    }

    public ValueNode<T> getValue() {
        return value;
    }

    public void setValue(ValueNode<T> value) {
        this.value = value;
    }

    @Override
    public int getOpCode() {
        return OpCode.PSEUDO_ASSIGN;
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
