package compiler.phase;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.MicroVM;
import uvm.Register;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.Value;

public class DefUseGeneration extends CompilationPhase{
    static final boolean VERBOSE = false;

    public DefUseGeneration(String name) {
        super(name);
    }

    @Override
    protected void visitInstruction(Instruction inst) {
        if (VERBOSE)
            System.out.println("generating def-use for inst:" + inst.prettyPrint());
        // uses
        for (Value v : inst.getOperands()) {
            if (v.isRegister()) {
                Register reg = (Register) v;
                inst.getRegUses().add(reg);
                reg.addUse(inst);
                
                if (VERBOSE)
                    System.out.println("  uses:" + v.prettyPrint());
            }
        }
        
        // def
        if (inst.getDefReg() != null) {
            inst.getDefReg().setDef(inst);
            if (VERBOSE)
                System.out.println("  def:" + inst.getDefReg().prettyPrint());
        }
    }

    @Override
    protected void visitFunction(Function f) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void visitBasicBlock(BasicBlock bb) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void preChecklist() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void postChecklist() {
        if (VERBOSE)
            System.out.println();
    }

    @Override
    protected void visitTreeNode(IRTreeNode node) {
        // TODO Auto-generated method stub
        
    }
}
