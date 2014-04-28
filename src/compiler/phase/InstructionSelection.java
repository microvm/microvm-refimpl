package compiler.phase;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.MicroVM;
import uvm.ssavalue.Instruction;
import burm.BURM_GENERATED;
import burm.BurmState;

public class InstructionSelection extends CompilationPhase{
    static final boolean VERBOSE = true;
    
    public InstructionSelection(String name) {
        super(name);
    }
    
    private static void print(IRTreeNode node) {
        System.out.println(node.printNode());
        System.out.println(node.state.prettyPrint());
        
        for (int i = 0; i < node.getArity(); i++)
            print(node.getChild(i));
    }
    
    public static BurmState label(IRTreeNode p) {        
        if (p != null) {
            BurmState[] leaves = new BurmState[p.getArity()];
            for (int i = 0; i < leaves.length; i++)
                leaves[i] = label(p.getChild(i));
            
            p.state = BURM_GENERATED.state(p, leaves);
            return p.state;
        } else
            return null;
    }

    @Override
    protected void preChecklist() {
        if (VERBOSE)
            System.out.println("\nStart instruction selection ... \n");
    }

    @Override
    protected void postChecklist() {
        if (VERBOSE) {
            for (Function f : MicroVM.v.funcs.values()) {
                for (IRTreeNode node : f.tree) {
                    print(node);
                    System.out.println();
                }
            }
        }
    }

    @Override
    protected void visitTreeNode(IRTreeNode node) {
        label(node);
    }

    @Override
    protected void visitInstruction(Instruction inst) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void visitFunction(Function f) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void visitBasicBlock(BasicBlock bb) {
        // TODO Auto-generated method stub
        
    }
}
