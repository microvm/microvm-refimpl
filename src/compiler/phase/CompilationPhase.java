package compiler.phase;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.MicroVM;
import uvm.ssavalue.Instruction;

public abstract class CompilationPhase {
    String name;
    
    public CompilationPhase(String name) {
        this.name = name;
    }
    
    public void execute() {
        preChecklist();
        
        for (Function f : MicroVM.v.funcs.values()) {
            visitFunction(f);
            
            for (BasicBlock bb : f.getBBs()) {
                visitBasicBlock(bb);
                
                for (Instruction inst : bb.getInsts()) {
                    visitInstruction(inst);
                }
            }
            
            for (IRTreeNode node : f.tree) 
                visitTreeNode(node);
        }
        
        postChecklist();
    }
    
    protected abstract void preChecklist();
    protected abstract void postChecklist();
    
    protected abstract void visitTreeNode(IRTreeNode node);
    protected abstract void visitInstruction(Instruction inst);
    protected abstract void visitFunction(Function f);    
    protected abstract void visitBasicBlock(BasicBlock bb);
}
