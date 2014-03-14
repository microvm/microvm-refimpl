package uvm;

import java.util.ArrayList;
import java.util.List;

import compiler.UVMCompiler;

public abstract class IRTreeNode {
    protected List<IRTreeNode> children = new ArrayList<IRTreeNode>();
    protected int opcode;
    
    public int getArity() {
        return children.size();
    }
    
    public IRTreeNode getChild(int index) {
        return children.get(index);
    }
    
    public void addChild(IRTreeNode node) {
        children.add(node);
    }
    
    public int getOpcode() {
        return opcode;
    }
    
    public abstract String prettyPrint();
}