package uvm.ir.text.input;

import static uvm.ir.text.input.ParserHelper.parseError;

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
import uvm.ssavalue.Parameter;
import uvm.ssavalue.Value;
import uvm.type.Type;

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

    FuncBuilder(RecursiveBundleBuilder rbb, Function func) {
        this.rbb = rbb;
        this.func = func;

        cfg = new CFG();

        cfg.setFunc(func);
        func.setCFG(cfg);
    }

    public void handleFuncDef(FuncDefContext ctx) {
        for (int i = 0; i < ctx.paramList().LOCAL_ID().size(); i++) {
            String name = ctx.paramList().LOCAL_ID(i).getText();
            Parameter param = new Parameter(func.getSig(), i);
            int id = rbb.makeID();
            param.setID(id);
            param.setName(name);
            cfg.getParams().add(param);
            cfg.getInstNs().put(id, name, param);
        }

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
        int id = rbb.makeID();
        entry.setID(id);
        LabelContext label = entryBlock.label();
        String name = label != null ? label.LOCAL_ID().getText() : "%__entry__";
        entry.setName(name);

        cfg.setEntry(entry);
        cfg.getBBs().add(entry);
        cfg.getBBNs().put(id, name, entry);

        populateBasicBlock(entry, entryBlock.inst());
    }

    private void handleRegularBlock(RegularBlockContext regularBlock) {
        BasicBlock bb = new BasicBlock(cfg);
        int id = rbb.makeID();
        bb.setID(id);
        LabelContext label = regularBlock.label();
        String name = label.LOCAL_ID().getText();
        bb.setName(name);

        cfg.getBBs().add(bb);
        cfg.getBBNs().put(id, name, bb);

        populateBasicBlock(bb, regularBlock.inst());
    }

    private void populateBasicBlock(BasicBlock bb, List<InstContext> instCtxs) {
        for (InstContext ctx : instCtxs) {
            TerminalNode nameToken = ctx.LOCAL_ID();
            String name = nameToken != null ? nameToken.getText() : null;
            Instruction inst = shallowInstructionMaker.visit(ctx.instBody());
            int id = rbb.makeID();
            inst.setID(id);
            inst.setName(name);
            bb.addInstruction(inst);
            cfg.getInstNs().put(id, name, inst);
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
            String name = rCtx.identifier().getText();
            if (name.startsWith("@")) {
                return getGlobalVal(name);
            } else {
                return getLocalVal(name);
            }
        } else {
            if (hint == null) {
                parseError(ctx, "Cannot build constant without a type hint");
            }
            if (hint.getID() == 0) {
                parseError(ctx,
                        "Text parser cannot infer literal constant type when the"
                                + "type is not declared by the client.");
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
        Value rv = rbb.bundle.getGlobalValueNs().getByName(name);
        if (rv == null) {
            parseError("Undefined global value " + name);
        }
        return rv;
    }

    private Value getLocalVal(String name) {
        Value rv = cfg.getInstNs().getByName(name);
        if (rv == null) {
            parseError("Undefined local value " + name);
        }
        return rv;
    }
}