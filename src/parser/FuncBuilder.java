package parser;

import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import parser.uIRParser.EntryBlockContext;
import parser.uIRParser.FuncDefContext;
import parser.uIRParser.InstContext;
import parser.uIRParser.LabelContext;
import parser.uIRParser.RegularBlockContext;
import uvm.BasicBlock;
import uvm.CFG;
import uvm.Function;
import uvm.ssavalue.Instruction;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * This class keeps the context of building a function, e.g. label to basic
 * mapping, register name to SSA Value mapping, etc.
 */
class FuncBuilder {

    final RecursiveBundleBuilder rbb;

    /**
     * The given Function object to define CFG in.
     */
    Function func;

    /**
     * The CFG.
     */
    CFG cfg;

    FuncBuilder(RecursiveBundleBuilder rbb, Function func) {
        this.rbb = rbb;
        this.func = func;

        cfg = new CFG();

        cfg.setFunc(func);
        func.setCFG(cfg);
    }

    public void handleFuncDef(FuncDefContext ctx) {
        handleEntryBlock(ctx.funcBody().basicBlocks().entryBlock());
        for (RegularBlockContext rb : ctx.funcBody().basicBlocks()
                .regularBlock()) {
            handleRegularBlock(rb);
        }
    }

    private void handleEntryBlock(EntryBlockContext entryBlock) {
        BasicBlock entry = new BasicBlock(cfg);
        entry.setID(rbb.makeID());
        LabelContext label = entryBlock.label();
        String name = label != null ? label.IDENTIFIER().getText() : "(entry)";
        entry.setName(name);

        cfg.setEntry(entry);
        cfg.getBBs().add(entry);

        populateBasicBlock(entry, entryBlock.inst());
    }

    private void handleRegularBlock(RegularBlockContext regularBlock) {
        BasicBlock bb = new BasicBlock(cfg);
        bb.setID(rbb.makeID());
        LabelContext label = regularBlock.label();
        String name = label.IDENTIFIER().getText();
        bb.setName(name);

        cfg.getBBs().add(bb);

        populateBasicBlock(bb, regularBlock.inst());
    }

    private void populateBasicBlock(BasicBlock bb, List<InstContext> instCtxs) {
        for (InstContext ctx : instCtxs) {
            TerminalNode nameToken = ctx.IDENTIFIER();
            String name = nameToken != null ? nameToken.getText() : null;
            Instruction inst = shallowInstructionMaker.visit(ctx.instBody());
            inst.setID(rbb.makeID());
            inst.setName(name);
            bb.addInstruction(inst);
        }
    }

    // Associated objects
    ShallowInstructionMaker shallowInstructionMaker = new ShallowInstructionMaker(
            this);

}