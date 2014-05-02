package compiler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import parser.RecursiveBundleBuilder;
import parser.uIRLexer;
import parser.uIRParser;
import parser.uIRParser.IrContext;
import uvm.BasicBlock;
import uvm.Bundle;
import uvm.Function;
import uvm.IdentifiedHelper;
import uvm.irtree.IRTreeBuilder;
import uvm.irtree.IRTreeNode;
import uvm.ssavalue.Constant;
import uvm.ssavalue.Instruction;
import compiler.phase.DefUseGeneration;
import compiler.phase.IRTreeGeneration;
import compiler.phase.InstructionSelection;
import compiler.phase.MachineCodeEmission;

public class UVMCompiler {

    public static final String file = "tests/micro-bm/int-prime-number/prime-number.uir";

    public static void main(String[] args) {

        try {
            // create a CharStream that reads from standard input
            ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(
                    file));
            // create a lexer that feeds off of input CharStream
            uIRLexer lexer = new uIRLexer(input);
            // create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // create a parser that feeds off the tokens buffer
            uIRParser parser = new uIRParser(tokens);
            IrContext tree = parser.ir(); // begin parsing at init rule

            System.out.println("Parsing Tree:");
            System.out.println(tree.toStringTree(parser));
            System.out.println();

            RecursiveBundleBuilder rbb = new RecursiveBundleBuilder();
            rbb.build(tree);

            Bundle bundle = rbb.getBundle();

            for (Function func : bundle.getFuncs().values()) {
                System.out.format("function %s : %s\n",
                        IdentifiedHelper.repr(func), func.getSig());

                System.out.println("Constants:");

                for (Constant c : func.getCFG().getConstPool().values()) {
                    System.out.format("  %s = %s\n", IdentifiedHelper.repr(c),
                            c);
                }

                System.out.println();

                for (BasicBlock bb : func.getCFG().getBBs()) {
                    System.out.format("%s:\n", bb.getName());

                    for (Instruction inst : bb.getInsts()) {
                        System.out.format("  %s = %s\n",
                                IdentifiedHelper.repr(inst), inst);
                    }
                }
                System.out.println();
            }

            for (Function func : bundle.getFuncs().values()) {
                IRTreeNode<Void> irTree = IRTreeBuilder.build(func.getCFG());
                IRTreeBuilder.prettyPrintIRTree(irTree);
            }

            System.exit(0); // TODO: For debug only. Remove when fixed.

            // get uses
            new DefUseGeneration("defusegen").execute();

            new IRTreeGeneration("treegen").execute();

            new InstructionSelection("instsel").execute();

            new MachineCodeEmission("mcemit").execute();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static final void error(String message) {
        System.err.print(message);
        Thread.dumpStack();
        System.exit(1);
    }

    public static final void _assert(boolean cond, String message) {
        if (!cond)
            error(message);
    }
}
