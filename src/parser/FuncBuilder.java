package parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.TerminalNode;

import parser.uIRParser.AnonymousInstructionContext;
import parser.uIRParser.ConstDefContext;
import parser.uIRParser.FuncDefContext;
import parser.uIRParser.InstBinOpContext;
import parser.uIRParser.InstBodyContext;
import parser.uIRParser.InstBranch2Context;
import parser.uIRParser.InstCmpContext;
import parser.uIRParser.InstParamContext;
import parser.uIRParser.InstPhiContext;
import parser.uIRParser.InstRetContext;
import parser.uIRParser.InstRetVoidContext;
import parser.uIRParser.LabelContext;
import parser.uIRParser.NamedInstructionContext;
import parser.uIRParser.ReferencedValueContext;
import parser.uIRParser.ValueContext;
import uvm.BasicBlock;
import uvm.CFG;
import uvm.Function;
import uvm.ssavalue.BinOptr;
import uvm.ssavalue.CmpOptr;
import uvm.ssavalue.Constant;
import uvm.ssavalue.InstBinOp;
import uvm.ssavalue.InstBranch2;
import uvm.ssavalue.InstCmp;
import uvm.ssavalue.InstPhi;
import uvm.ssavalue.InstRet;
import uvm.ssavalue.InstRetVoid;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.Parameter;
import uvm.ssavalue.Value;
import uvm.type.Int;
import uvm.type.Type;

import compiler.UVMCompiler;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * This class keeps the context of building a function, e.g. label to basic
 * mapping, register name to SSA Value mapping, etc.
 */
class FuncBuilder {

    private final RecursiveBundleBuilder rbb;

    /**
     * The given Function object to define CFG in.
     */
    private Function func;

    /**
     * The CFG.
     */
    private CFG cfg = new CFG();

    /**
     * If true, we have encountered neither a label nor an instruction. This
     * is needed because the first basic block (the entry block) may be
     * implicit (does not need a label).
     */
    private boolean expectImplicitEntry = true;

    /**
     * A mapping from symbolic label to actual basic blocks.
     */
    private Map<String, BasicBlock> labelToBB = new HashMap<String, BasicBlock>();

    /**
     * A mapping from symbolic register name to actual SSA Values.
     */
    private Map<String, Value> nameToValue = new HashMap<String, Value>();

    /**
     * In the first pass (see below), remember the created SSA Value so that
     * they can be re-processed in the second pass.
     */
    private Map<InstBodyContext, Value> ctxToValue = new HashMap<InstBodyContext, Value>();

    /**
     * The current basic block.
     */
    private BasicBlock curBB = null;

    public FuncBuilder(RecursiveBundleBuilder recursiveBundleBuilder, Function func) {
        rbb = recursiveBundleBuilder;
        this.func = func;
        func.setCFG(cfg);
        cfg.setFunc(func);
    }

    public void handleFuncDef(FuncDefContext ctx) {
        FirstPass firstPass = new FirstPass();
        SecondPass secondPass = new SecondPass();
        firstPass.visit(ctx.funcBody());
        secondPass.visit(ctx.funcBody());
    }

    /**
     * Parsing a function takes two passes. The first pass merely creates
     * instances for each instruction and gives them register names. This is
     * because the references between instructions may form loops.
     * <p>
     * However, since constants and parameters do not contain cycles, they
     * can be done in the first pass alone.
     */
    class FirstPass extends uIRBaseVisitor<Object> {

        @Override
        public Void visitLabel(LabelContext ctx) {
            if (curBB != null) {
                UVMCompiler._assert(curBB.getInsts().size() != 0,
                        "A basic block needs at least one instruction. BB "
                                + curBB.getName() + " is empty. ");
            }

            String text = ctx.IDENTIFIER().getText();
            BasicBlock bb = new BasicBlock(cfg);
            bb.setID(FuncBuilder.this.rbb.makeID());
            bb.setName(text);
            curBB = bb;
            labelToBB.put(text, bb);
            cfg.getBBs().add(bb);

            if (expectImplicitEntry) {
                cfg.setEntry(bb);
                expectImplicitEntry = false;
            }

            return null;
        }

        @Override
        public Void visitConstDef(ConstDefContext ctx) {
            String name = ctx.IDENTIFIER().getText();
            Type type = myVisitType(ctx.type());

            Constant constant;

            if (type instanceof Int) {
                long value = (long) visit(ctx);
                // May be wrong type. In that case it throws an exception.

                constant = new IntConstant((Int) type, value);
                constant.setID(FuncBuilder.this.rbb.makeID());
                constant.setName(name);
                cfg.getConstPool().put(constant.getID(), constant);
            } else {
                throw new ASTParsingException("Constant of type "
                        + type.getClass().getName()
                        + " is not implemented.");
            }

            nameToValue.put(name, constant);

            return null;
        }

        @Override
        public Void visitNamedInstruction(NamedInstructionContext ctx) {
            Instruction inst = doInstruction(ctx.instBody());

            String name = ctx.IDENTIFIER().getText();
            nameToValue.put(name, inst);
            inst.setName(name);
            return null;
        }

        @Override
        public Void visitAnonymousInstruction(
                AnonymousInstructionContext ctx) {
            doInstruction(ctx.instBody());
            return null;
        }

        private Instruction doInstruction(InstBodyContext ctx) {
            if (expectImplicitEntry) {
                BasicBlock bb = new BasicBlock(cfg);
                bb.setID(FuncBuilder.this.rbb.makeID());
                bb.setName("(entry)");
                cfg.setEntry(bb);
                expectImplicitEntry = false;
            }
            Instruction inst = (Instruction) visit(ctx);
            inst.setID(FuncBuilder.this.rbb.makeID());
            curBB.addInstruction(inst);
            ctxToValue.put(ctx, inst);
            return inst;
        }

        @Override
        public Parameter visitInstParam(InstParamContext ctx) {
            int index = FuncBuilder.this.rbb.intLitToInt(ctx.intLiteral());
            return new Parameter(func.getSig(), index);
        }

        @Override
        public InstBinOp visitInstBinOp(InstBinOpContext ctx) {
            return new InstBinOp();
        }

        @Override
        public InstCmp visitInstCmp(InstCmpContext ctx) {
            return new InstCmp();
        }

        @Override
        public InstBranch2 visitInstBranch2(InstBranch2Context ctx) {
            return new InstBranch2();
        }

        @Override
        public InstPhi visitInstPhi(InstPhiContext ctx) {
            return new InstPhi();
        }

        @Override
        public InstRet visitInstRet(InstRetContext ctx) {
            return new InstRet();
        }

        @Override
        public InstRetVoid visitInstRetVoid(InstRetVoidContext ctx) {
            return new InstRetVoid();
        }

    }

    /**
     * The second pass populates each concrete instruction and links them.
     */
    class SecondPass extends uIRBaseVisitor<Object> {

        @Override
        public Object visitReferencedValue(ReferencedValueContext ctx) {
            return nameToValue.get(ctx.IDENTIFIER().getText());
        }

        private Value valueWithInferredLiteral(ValueContext ctx, Type type) {
            Object result = visit(ctx);

            if (result instanceof Value) {
                return (Value) result;
            } else if (result instanceof Long) {
                if (type instanceof Int) {
                    IntConstant constant = new IntConstant((Int) type,
                            (long) result);
                    int id = FuncBuilder.this.rbb.makeID();
                    constant.setID(id);
                    cfg.getConstPool().put(id, constant);
                    return constant;
                } else {
                    throw new ASTParsingException(String.format(
                            "Found intImmediate, but %s expected", type
                                    .getClass().getName()));
                }
            } else if (result instanceof Double) {
                if (type instanceof uvm.type.Float) {
                    throw new ASTParsingException("Float not implemented");
                } else if (type instanceof uvm.type.Double) {
                    throw new ASTParsingException("Double not implemented");
                } else {
                    throw new ASTParsingException(String.format(
                            "Found intImmediate, but %s expected", type
                                    .getClass().getName()));
                }
            } else {
                throw new ASTParsingException("unreachable");
            }

        }

        @Override
        public Void visitInstBinOp(InstBinOpContext ctx) {
            InstBinOp inst = (InstBinOp) ctxToValue.get(ctx);
            Type type = myVisitType(ctx.type());
            inst.setType(type);
            inst.setOptr(BinOptr.valueOf(ctx.BINOPS().getText()));
            inst.setOp1(valueWithInferredLiteral(ctx.value(0), type));
            inst.setOp2(valueWithInferredLiteral(ctx.value(1), type));
            return null;
        }

        @Override
        public Void visitInstCmp(InstCmpContext ctx) {
            InstCmp inst = (InstCmp) ctxToValue.get(ctx);
            Type opndType = myVisitType(ctx.type());
            inst.setOpndType(opndType);
            inst.setOptr(CmpOptr.valueOf(ctx.CMPOPS().getText()));
            inst.setOp1(valueWithInferredLiteral(ctx.value(0), opndType));
            inst.setOp2(valueWithInferredLiteral(ctx.value(1), opndType));
            return null;
        }

        @Override
        public Void visitInstBranch2(InstBranch2Context ctx) {
            InstBranch2 inst = (InstBranch2) ctxToValue.get(ctx);
            inst.setCond((Value) visit(ctx.value()));
            inst.setIfTrue(labelToBB.get(ctx.IDENTIFIER(0).getText()));
            inst.setIfFalse(labelToBB.get(ctx.IDENTIFIER(1).getText()));
            return null;
        }

        @Override
        public Void visitInstPhi(InstPhiContext ctx) {
            InstPhi inst = (InstPhi) ctxToValue.get(ctx);
            Type type = myVisitType(ctx.type());
            inst.setType(type);

            List<TerminalNode> ids = ctx.IDENTIFIER();
            List<ValueContext> values = ctx.value();

            for (int i = 0; i < ids.size(); i++) {
                String label = ids.get(i).getText();
                BasicBlock bb = labelToBB.get(label);
                Value value = valueWithInferredLiteral(values.get(i), type);
                inst.setValueFrom(bb, value);
            }

            return null;
        }

        @Override
        public Void visitInstRet(InstRetContext ctx) {
            InstRet inst = (InstRet) ctxToValue.get(ctx);
            Type type = myVisitType(ctx.type());
            Value value = valueWithInferredLiteral(ctx.value(), type);
            inst.setRetType(type);
            inst.setRetVal(value);
            return null;
        }

        @Override
        public Void visitInstRetVoid(InstRetVoidContext ctx) {
            // Do nothing
            return null;
        }

    }

}