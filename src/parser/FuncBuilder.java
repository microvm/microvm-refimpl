package parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import parser.uIRParser.EntryBlockContext;
import parser.uIRParser.FuncDefContext;
import parser.uIRParser.InstContext;
import parser.uIRParser.LabelContext;
import parser.uIRParser.ReferencedValueContext;
import parser.uIRParser.RegularBlockContext;
import parser.uIRParser.ValueContext;
import uvm.BasicBlock;
import uvm.CFG;
import uvm.Function;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.Value;
import uvm.type.Type;
import static parser.ParserHelper.parseError;

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

    Map<RuleContext, Instruction> ctxToInst = new HashMap<RuleContext, Instruction>();
    Map<String, Instruction> nameToInst = new HashMap<String, Instruction>();
    Map<String, BasicBlock> nameToBB = new HashMap<String, BasicBlock>();

    FuncBuilder(RecursiveBundleBuilder rbb, Function func) {
        this.rbb = rbb;
        this.func = func;

        cfg = new CFG();

        cfg.setFunc(func);
        func.setCFG(cfg);
    }

    public void handleFuncDef(FuncDefContext ctx) {
        // The shallow-deep pattern:
        // First make all instructions, but does not resolve named values
        // or labels.
        handleEntryBlock(ctx.funcBody().basicBlocks().entryBlock());
        for (RegularBlockContext rb : ctx.funcBody().basicBlocks()
                .regularBlock()) {
            handleRegularBlock(rb);
        }

        // The second pass populate all instructions.

        populateInstruction.visit(ctx.funcBody());
    }

    private void handleEntryBlock(EntryBlockContext entryBlock) {
        BasicBlock entry = new BasicBlock(cfg);
        entry.setID(rbb.makeID());
        LabelContext label = entryBlock.label();
        String name = label != null ? label.IDENTIFIER().getText() : "(entry)";
        entry.setName(name);

        cfg.setEntry(entry);
        cfg.getBBs().add(entry);
        nameToBB.put(name, entry);

        populateBasicBlock(entry, entryBlock.inst());
    }

    private void handleRegularBlock(RegularBlockContext regularBlock) {
        BasicBlock bb = new BasicBlock(cfg);
        bb.setID(rbb.makeID());
        LabelContext label = regularBlock.label();
        String name = label.IDENTIFIER().getText();
        bb.setName(name);

        cfg.getBBs().add(bb);
        nameToBB.put(name, bb);

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
            nameToInst.put(name, inst);
            ctxToInst.put(ctx.instBody(), inst);
        }
    }

    // Associated objects
    ShallowInstructionMaker shallowInstructionMaker = new ShallowInstructionMaker(
            this);

    PopulateInstruction populateInstruction = new PopulateInstruction(this);

    // Helper functions

    /**
     * Handle a "value" non-terminal as a parameter to any instruction.
     * 
     * @param hint
     *            the expected type as a hint for in-line constants.
     */
    Value value(ValueContext ctx, Type hint) {
        if (ctx instanceof ReferencedValueContext) {
            ReferencedValueContext rCtx = (ReferencedValueContext) ctx;
            String name = rCtx.IDENTIFIER().getText();
            if (name.startsWith("@")) {
                return getGlobalVal(name);
            } else {
                return getLocalVal(name);
            }
        } else {
            if (hint == null) {
                parseError("Cannot build constant without a type hint");
            }
            return new DeepConstMaker(rbb, hint).visit(ctx);
        }
    }

    /**
     * Handle a "value" non-terminal which must refer to a local value.
     */
    Value localVal(ValueContext ctx) {
        if (ctx instanceof ReferencedValueContext) {
            String name = ctx.getText();
            if (name.startsWith("%")) {
                return getLocalVal(name);
            } else {
                parseError("Found global " + name + " Expect local value.");
            }
        } else {
            parseError("Found constant " + ctx.getText()
                    + " Expect local value.");
        }
        return null;
    }

    private Value getGlobalVal(String name) {
        Value rv = rbb.bundle.getConstantByName(name);
        if (rv == null) {
            parseError("Undefined global value " + name);
        }
        return rv;
    }

    private Value getLocalVal(String name) {
        Value rv = nameToInst.get(name);
        if (rv == null) {
            parseError("Undefined local value " + name);
        }
        return rv;
    }

    class ValueResolver extends uIRBaseVisitor<Value> {
        @Override
        public Value visitReferencedValue(ReferencedValueContext ctx) {
            String name = ctx.IDENTIFIER().getText();
            if (name.startsWith("%")) {
                Value rv = nameToInst.get(name);
                if (rv == null) {
                    parseError("Undefined instruction " + name);
                }
                return rv;
            } else {
                return null;
            }
        }
    }
}