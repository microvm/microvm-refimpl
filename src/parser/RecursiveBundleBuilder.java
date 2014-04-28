package parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import parser.uIRParser.AnonymousInstructionContext;
import parser.uIRParser.ArrayTypeContext;
import parser.uIRParser.ConstDefContext;
import parser.uIRParser.ConstExprContext;
import parser.uIRParser.DecIntLiteralContext;
import parser.uIRParser.DoubleTypeContext;
import parser.uIRParser.FPConstContext;
import parser.uIRParser.FloatTypeContext;
import parser.uIRParser.FpLiteralContext;
import parser.uIRParser.FuncDefContext;
import parser.uIRParser.FuncSigConstructorContext;
import parser.uIRParser.FuncSigDefContext;
import parser.uIRParser.FuncTypeContext;
import parser.uIRParser.HexIntLiteralContext;
import parser.uIRParser.HybridTypeContext;
import parser.uIRParser.IRefTypeContext;
import parser.uIRParser.InLineFuncSigContext;
import parser.uIRParser.InLineTypeContext;
import parser.uIRParser.InstBinOpContext;
import parser.uIRParser.InstBodyContext;
import parser.uIRParser.InstBranch2Context;
import parser.uIRParser.InstCmpContext;
import parser.uIRParser.InstParamContext;
import parser.uIRParser.InstPhiContext;
import parser.uIRParser.InstRetContext;
import parser.uIRParser.InstRetVoidContext;
import parser.uIRParser.IntConstContext;
import parser.uIRParser.IntLiteralContext;
import parser.uIRParser.IntTypeContext;
import parser.uIRParser.LabelContext;
import parser.uIRParser.NamedInstructionContext;
import parser.uIRParser.NullConstContext;
import parser.uIRParser.OctIntLiteralContext;
import parser.uIRParser.RefTypeContext;
import parser.uIRParser.ReferencedFuncSigContext;
import parser.uIRParser.ReferencedTypeContext;
import parser.uIRParser.ReferencedValueContext;
import parser.uIRParser.StackTypeContext;
import parser.uIRParser.StructConstContext;
import parser.uIRParser.StructTypeContext;
import parser.uIRParser.TagRef64TypeContext;
import parser.uIRParser.ThreadTypeContext;
import parser.uIRParser.TypeContext;
import parser.uIRParser.TypeDefContext;
import parser.uIRParser.ValueContext;
import parser.uIRParser.VoidTypeContext;
import parser.uIRParser.WeakRefTypeContext;
import uvm.BasicBlock;
import uvm.Bundle;
import uvm.CFG;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.ssavalue.BinOptr;
import uvm.ssavalue.CmpOptr;
import uvm.ssavalue.Constant;
import uvm.ssavalue.FPConstant;
import uvm.ssavalue.InstBinOp;
import uvm.ssavalue.InstBranch2;
import uvm.ssavalue.InstCmp;
import uvm.ssavalue.InstPhi;
import uvm.ssavalue.InstRet;
import uvm.ssavalue.InstRetVoid;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.Parameter;
import uvm.ssavalue.StructConstant;
import uvm.ssavalue.Value;
import uvm.type.AbstractReferenceType;
import uvm.type.Array;
import uvm.type.FPType;
import uvm.type.Func;
import uvm.type.Hybrid;
import uvm.type.IRef;
import uvm.type.Int;
import uvm.type.Ref;
import uvm.type.Struct;
import uvm.type.TagRef64;
import uvm.type.Type;
import uvm.type.WeakRef;
import compiler.UVMCompiler;

/**
 * RecursiveUIRBuilder builds a uvm Bundle from a uir parse tree.
 * <p>
 * Not thread safe. Don't use from multiple threads.
 */
public class RecursiveBundleBuilder extends uIRBaseVisitor<Object> {
    /**
     * The result bundle.
     */
    private Bundle bundle;

    public RecursiveBundleBuilder() {
        bundle = new Bundle();
    }

    /**
     * @return The result bundle. Should only be called after building.
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Build the bundle.
     * 
     * @param pt
     *            The ParseTree object of the root, i.e. the result of
     *            parser.ir()
     */
    public void build(ParseTree pt) {
        // Build types in two passes.

        // // The first pass, create a stub for all .typedef and .funcsig.
        uIRBaseVisitor<Void> makeDeclaredTypes = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitTypeDef(TypeDefContext ctx) {
                Type type = shallowTypeMaker.visit(ctx.typeConstructor());
                String name = ctx.IDENTIFIER().getText();
                type.setName(name);
                bundle.bind(type.getID(), name);
                return null;
            }

            @Override
            public Void visitFuncSigDef(FuncSigDefContext ctx) {
                FunctionSignature sig = shallowFuncSigMaker.visit(ctx
                        .funcSigConstructor());
                String name = ctx.IDENTIFIER().getText();
                sig.setName(name);
                bundle.bind(sig.getID(), name);
                return null;
            }
        };
        makeDeclaredTypes.visit(pt);

        // // The second pass, populate its dependencies.
        uIRBaseVisitor<Void> refineDeclaredTypes = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitTypeDef(TypeDefContext ctx) {
                String name = ctx.IDENTIFIER().getText();
                final Type oldType = bundle.getTypeByName(name);

                new uIRBaseVisitor<Void>() {

                    @Override
                    public Void visitRefType(RefTypeContext ctx) {
                        Ref type = (Ref) oldType;
                        typeAndSigRefiner.visitRefType(type, ctx);
                        return null;
                    }

                    @Override
                    public Void visitIRefType(IRefTypeContext ctx) {
                        IRef type = (IRef) oldType;
                        typeAndSigRefiner.visitIRefType(type, ctx);
                        return null;
                    }

                    @Override
                    public Void visitWeakRefType(WeakRefTypeContext ctx) {
                        WeakRef type = (WeakRef) oldType;
                        typeAndSigRefiner.visitWeakRefType(type, ctx);
                        return null;
                    }

                    @Override
                    public Void visitStructType(StructTypeContext ctx) {
                        Struct type = (Struct) oldType;
                        typeAndSigRefiner.visitStructType(type, ctx);
                        return null;
                    }

                    @Override
                    public Void visitArrayType(ArrayTypeContext ctx) {
                        Array type = (Array) oldType;
                        typeAndSigRefiner.visitArrayType(type, ctx);
                        return null;
                    }

                    @Override
                    public Void visitHybridType(HybridTypeContext ctx) {
                        Hybrid type = (Hybrid) oldType;
                        typeAndSigRefiner.visitHybridType(type, ctx);
                        return null;
                    }

                    @Override
                    public Void visitFuncType(FuncTypeContext ctx) {
                        Func type = (Func) oldType;
                        typeAndSigRefiner.visitFuncType(type, ctx);
                        return null;
                    }
                }.visit(ctx.typeConstructor());

                return null;
            }

            @Override
            public Void visitFuncSigDef(FuncSigDefContext ctx) {
                String name = ctx.IDENTIFIER().getText();
                FunctionSignature sig = bundle.getFuncSigByName(name);

                typeAndSigRefiner.visitFuncSigConstructor(sig,
                        ctx.funcSigConstructor());

                return null;
            }

        };
        refineDeclaredTypes.visit(pt);

        // Visit all global constant definitions. Skip local definitions in this
        // step.
        uIRBaseVisitor<Void> globalConstDefVisitor = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitConstDef(ConstDefContext ctx) {
                handleGlobalConstDef(ctx);
                return null;
            }
            
            @Override
            public Void visitFuncDef(FuncDefContext ctx) {
                return null; // Skip functions to skip local ".const"
            }
        };

        globalConstDefVisitor.visit(pt);
    }

    // ID facilities

    private int nextId = 1;

    /**
     * Create a new globally unique ID.
     * <p>
     * TODO: Consider moving to a more "global" place. Candidate: MicroVM
     * 
     * @return A unique ID for everything: types, signatures, functions,
     *         instructions, ...
     */
    private int makeID() {
        int thisId = nextId++;
        return thisId;
    }

    // Types and function signatures

    /**
     * Make types according to the grammar, but never handle recursive types (
     * which takes other types as parameters).
     */
    private class ShallowTypeMaker extends uIRBaseVisitor<Type> {
        @Override
        public Type visitIntType(IntTypeContext ctx) {
            int bitSize = intLitToInt(ctx.intLiteral());
            Int type = new Int(bitSize);
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public uvm.type.Float visitFloatType(FloatTypeContext ctx) {
            uvm.type.Float type = new uvm.type.Float();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public uvm.type.Double visitDoubleType(DoubleTypeContext ctx) {
            uvm.type.Double type = new uvm.type.Double();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public Ref visitRefType(RefTypeContext ctx) {
            Ref type = new Ref();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public IRef visitIRefType(IRefTypeContext ctx) {
            IRef type = new IRef();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public WeakRef visitWeakRefType(WeakRefTypeContext ctx) {
            WeakRef type = new WeakRef();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public Struct visitStructType(StructTypeContext ctx) {
            Struct type = new Struct();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public uvm.type.Array visitArrayType(ArrayTypeContext ctx) {
            uvm.type.Array type = new uvm.type.Array();
            type.setLength(intLitToInt(ctx.intLiteral()));
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public Hybrid visitHybridType(HybridTypeContext ctx) {
            Hybrid type = new Hybrid();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public uvm.type.Void visitVoidType(VoidTypeContext ctx) {
            uvm.type.Void type = new uvm.type.Void();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public Func visitFuncType(FuncTypeContext ctx) {
            Func type = new Func();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public uvm.type.Thread visitThreadType(ThreadTypeContext ctx) {
            uvm.type.Thread type = new uvm.type.Thread();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public uvm.type.Stack visitStackType(StackTypeContext ctx) {
            uvm.type.Stack type = new uvm.type.Stack();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }

        @Override
        public TagRef64 visitTagRef64Type(TagRef64TypeContext ctx) {
            TagRef64 type = new TagRef64();
            type.setID(makeID());
            bundle.registerType(type.getID(), null, type);
            return type;
        }
    }

    /**
     * Similar to ShallowTypeMaker, but works on ".funcsig" definitions.
     */
    private class ShallowFuncSigMaker extends uIRBaseVisitor<FunctionSignature> {
        @Override
        public FunctionSignature visitFuncSigConstructor(
                FuncSigConstructorContext ctx) {
            FunctionSignature sig = new FunctionSignature();
            sig.setID(makeID());
            bundle.registerFuncSig(sig.getID(), null, sig);
            return sig;
        }
    }

    private ShallowTypeMaker shallowTypeMaker = new ShallowTypeMaker();
    private ShallowFuncSigMaker shallowFuncSigMaker = new ShallowFuncSigMaker();

    /**
     * Visit types and function signatures, fill in their dependencies to other
     * types or function signatures.
     */
    private class TypeAndSigRefiner {

        public void visitRefType(Ref type, RefTypeContext ctx) {
            type.setReferenced(deepTypeMaker.visit(ctx.type()));
        }

        public void visitIRefType(IRef type, IRefTypeContext ctx) {
            type.setReferenced(deepTypeMaker.visit(ctx.type()));
        }

        public void visitWeakRefType(WeakRef type, WeakRefTypeContext ctx) {
            type.setReferenced(deepTypeMaker.visit(ctx.type()));
        }

        public void visitStructType(Struct type, StructTypeContext ctx) {
            for (TypeContext childCtx : ctx.type()) {
                Type childType = deepTypeMaker.visit(childCtx);
                type.getFieldTypes().add(childType);
            }
        }

        public void visitArrayType(Array type, ArrayTypeContext ctx) {
            type.setElemType(deepTypeMaker.visit(ctx.type()));
        }

        public void visitHybridType(Hybrid type, HybridTypeContext ctx) {
            type.setFixedPart(deepTypeMaker.visit(ctx.type(0)));
            type.setVarPart(deepTypeMaker.visit(ctx.type(1)));
        }

        public void visitFuncType(Func type, FuncTypeContext ctx) {
            type.setSig(deepFuncSigMaker.visit(ctx.funcSig()));
        }

        public void visitFuncSigConstructor(FunctionSignature sig,
                FuncSigConstructorContext ctx) {
            List<TypeContext> types = ctx.type();

            Type returnType = deepTypeMaker.visit(types.get(0));
            sig.setReturnType(returnType);

            for (int i = 1; i < types.size(); i++) {
                Type paramType = deepTypeMaker.visit(types.get(i));
                sig.getParamTypes().add(paramType);
            }
        }
    }

    private TypeAndSigRefiner typeAndSigRefiner = new TypeAndSigRefiner();

    /**
     * Visit types recursively. This visitor class assumes that all declared
     * types (by .typedef) and declared function signatures (by .funcsig) are
     * already handled and will lookup the bundle for those types.
     */
    private class DeepTypeMaker extends ShallowTypeMaker {
        @Override
        public Type visitReferencedType(ReferencedTypeContext ctx) {
            String name = ctx.IDENTIFIER().getText();
            Type type = bundle.getTypeByName(name);

            if (type == null) {
                throw new ASTParsingException("Undefined type " + name);
            }

            return type;
        }

        @Override
        public Ref visitRefType(RefTypeContext ctx) {
            Ref type = super.visitRefType(ctx);
            typeAndSigRefiner.visitRefType(type, ctx);
            return type;
        }

        @Override
        public IRef visitIRefType(IRefTypeContext ctx) {
            IRef type = super.visitIRefType(ctx);
            typeAndSigRefiner.visitIRefType(type, ctx);
            return type;
        }

        @Override
        public WeakRef visitWeakRefType(WeakRefTypeContext ctx) {
            WeakRef type = super.visitWeakRefType(ctx);
            typeAndSigRefiner.visitWeakRefType(type, ctx);
            return type;
        }

        @Override
        public Struct visitStructType(StructTypeContext ctx) {
            Struct type = super.visitStructType(ctx);
            typeAndSigRefiner.visitStructType(type, ctx);
            return type;
        }

        @Override
        public uvm.type.Array visitArrayType(ArrayTypeContext ctx) {
            Array type = super.visitArrayType(ctx);
            typeAndSigRefiner.visitArrayType(type, ctx);
            return type;
        }

        @Override
        public Hybrid visitHybridType(HybridTypeContext ctx) {
            Hybrid type = super.visitHybridType(ctx);
            typeAndSigRefiner.visitHybridType(type, ctx);
            return type;
        }

        @Override
        public Func visitFuncType(FuncTypeContext ctx) {
            Func type = super.visitFuncType(ctx);
            typeAndSigRefiner.visitFuncType(type, ctx);
            return type;
        }
    }

    /**
     * Similar to DeepTypeMaker, but works on ".funcsig" definitions.
     */
    private class DeepFuncSigMaker extends ShallowFuncSigMaker {
        @Override
        public FunctionSignature visitReferencedFuncSig(
                ReferencedFuncSigContext ctx) {
            String name = ctx.IDENTIFIER().getText();
            FunctionSignature sig = bundle.getFuncSigByName(name);

            if (sig == null) {
                throw new ASTParsingException("Undefined sig " + name);
            }

            return sig;
        }

        @Override
        public FunctionSignature visitFuncSigConstructor(
                FuncSigConstructorContext ctx) {
            FunctionSignature sig = super.visitFuncSigConstructor(ctx);
            typeAndSigRefiner.visitFuncSigConstructor(sig, ctx);
            return sig;
        }
    }

    private DeepTypeMaker deepTypeMaker = new DeepTypeMaker();
    private DeepFuncSigMaker deepFuncSigMaker = new DeepFuncSigMaker();

    // ///////BEGIN LEGACY CODE////////////

    /**
     * Not a visitor method. A shorthand for getting a type from a node.
     * Otherwise functions in nested classes (e.g. FuncBuilder, etc.) will have
     * to write
     * <code>(Type) RecursiveBundleBuilder.this.visit(ctx.type())</code>, which
     * is long.
     * 
     * @param ctx
     *            The TypeContext for either a ReferencedType or a InLineType
     * @return the type.
     */
    @Deprecated
    public Type myVisitType(TypeContext ctx) {
        return (Type) visit(ctx);
    }

    @Deprecated
    @Override
    public Type visitReferencedType(ReferencedTypeContext ctx) {
        throw new ASTParsingException("Named type not implemented");
    }

    @Deprecated
    @Override
    public Type visitInLineType(InLineTypeContext ctx) {
        return (Type) visit(ctx.typeConstructor());
    }

    @Deprecated
    @Override
    public Type visitIntType(IntTypeContext ctx) {
        return shallowTypeMaker.visitIntType(ctx);
    }

    @Deprecated
    @Override
    public Type visitFloatType(FloatTypeContext ctx) {
        return shallowTypeMaker.visitFloatType(ctx);
    }

    // ///////END LEGACY CODE////////////

    // Constant expression helpers

    // Literals Literals may and may not be Constants.
    // The int type also takes a literal as a parameter.

    class IntLiteralParser extends uIRBaseVisitor<Long> {
        @Override
        public Long visitDecIntLiteral(DecIntLiteralContext ctx) {
            return Long.parseLong(ctx.getText());
        }

        @Override
        public Long visitOctIntLiteral(OctIntLiteralContext ctx) {
            return Long.parseLong(ctx.getText(), 8);
        }

        @Override
        public Long visitHexIntLiteral(HexIntLiteralContext ctx) {
            String text = ctx.getText();
            boolean negative = text.charAt(0) == '-';
            String body = text.substring(text.indexOf('x') + 1);
            long num = Long.parseLong(body, 16);
            if (negative) {
                num = -num;
            }
            return num;
        }
    }

    private IntLiteralParser intLiteralParser = new IntLiteralParser();

    /**
     * A convenient method that handles the cast.
     */
    private long intLitToLong(IntLiteralContext ctx) {
        return (long) intLiteralParser.visit(ctx);
    }

    /**
     * A convenient method that handles the cast.
     */
    private int intLitToInt(IntLiteralContext ctx) {
        return (int) ((long) intLiteralParser.visit(ctx));
    }

    private class FPLiteralParser extends uIRBaseVisitor<Double> {
        @Override
        public Double visitFpLiteral(FpLiteralContext ctx) {
            return Double.parseDouble(ctx.getText());
        }
    }

    private FPLiteralParser fpLiteralParser = new FPLiteralParser();

    // Constant processors

    /**
     * A general function to handle ".const" definitions, global or local.
     */
    private Constant handleConstDef(ConstDefContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        Type type = deepTypeMaker.visit(ctx.type());
        Constant constant = new ConstExprHandler(type).visit(ctx.constExpr());

        constant.setName(name);

        return constant;
    }

    public Constant handleGlobalConstDef(ConstDefContext ctx) {
        Constant constant = handleConstDef(ctx);

        String name = constant.getName();
        if (!name.startsWith("@")) {
            throw new ASTParsingException("Local identifier " + name
                    + " found. Expect Galobal identifier.");
        }

        bundle.bind(constant.getID(), name);

        return constant;
    }

    /**
     * Handles constExpr. Match the literals against the expected type and
     * recursively construct nested (but not recursive) Constant objects.
     */
    private class ConstExprHandler extends uIRBaseVisitor<Constant> {
        private Type expectedType;

        public ConstExprHandler(Type expectedType) {
            this.expectedType = expectedType;
        }

        @Override
        public Constant visitIntConst(IntConstContext ctx) {
            if (!(expectedType instanceof Int)) {
                throw new ASTParsingException("Int literal " + ctx.getText()
                        + " found. Expect" + expectedType);
            }

            long value = intLitToLong(ctx.intLiteral());
            IntConstant constant = new IntConstant(expectedType, value);
            constant.setID(makeID());
            bundle.registerConstant(constant.getID(), null, constant);
            return constant;
        }

        @Override
        public Constant visitFPConst(FPConstContext ctx) {
            if (!(expectedType instanceof FPType)) {
                throw new ASTParsingException("FP literal " + ctx.getText()
                        + " found. Expect" + expectedType);
            }

            double value = fpLiteralParser.visit(ctx.fpLiteral());
            FPConstant constant = new FPConstant(expectedType, value);
            constant.setID(makeID());
            bundle.registerConstant(constant.getID(), null, constant);
            return constant;
        }

        @Override
        public Constant visitStructConst(StructConstContext ctx) {
            if (!(expectedType instanceof Struct)) {
                throw new ASTParsingException("Int literal " + ctx.getText()
                        + " found. Expect" + expectedType);
            }

            Struct type = (Struct) expectedType;

            int actualFields = ctx.constExpr().size();
            int expectedFields = type.getFieldTypes().size();

            if (actualFields != expectedFields) {
                throw new ASTParsingException("Found " + actualFields
                        + " fields: " + ctx.getText() + " Expect "
                        + expectedFields + " fields.");
            }

            StructConstant constant = new StructConstant();
            constant.setType(type);
            constant.setID(makeID());
            bundle.registerConstant(constant.getID(), null, constant);

            for (int i = 0; i < actualFields; i++) {
                ConstExprContext subExpr = ctx.constExpr(i);
                Type fieldType = type.getFieldTypes().get(i);
                Constant subConstant = new ConstExprHandler(fieldType)
                        .visit(subExpr);
                constant.getValues().add(subConstant);
            }

            return constant;
        }

        @Override
        public Constant visitNullConst(NullConstContext ctx) {
            if (!((expectedType instanceof AbstractReferenceType) //
                    || (expectedType instanceof Func) //
                    || (expectedType instanceof uvm.type.Thread) //
            || (expectedType instanceof uvm.type.Stack)//
            )) {
                throw new ASTParsingException("NULL literal found. Expect"
                        + expectedType);
            }

            NullConstant constant = new NullConstant();
            constant.setType(expectedType);
            constant.setID(makeID());
            bundle.registerConstant(constant.getID(), null, constant);

            return constant;
        }
    }

    // Functions and instructions.

    /**
     * This class keeps the context of building a function, e.g. label to basic
     * mapping, register name to SSA Value mapping, etc.
     */
    class FuncBuilder extends uIRBaseVisitor<Object> {

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

        public FuncBuilder(Function func) {
            this.func = func;
            func.setCFG(cfg);
            cfg.setFunc(func);
        }

        @Override
        public Void visitFuncDef(FuncDefContext ctx) {
            FirstPass firstPass = new FirstPass();
            SecondPass secondPass = new SecondPass();
            firstPass.visit(ctx.funcBody());
            secondPass.visit(ctx.funcBody());
            return null;
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
                bb.setID(makeID());
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

                    constant = new IntConstant(type, value);
                    constant.setID(makeID());
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
                    bb.setID(makeID());
                    bb.setName("(entry)");
                    cfg.setEntry(bb);
                    expectImplicitEntry = false;
                }
                Instruction inst = (Instruction) visit(ctx);
                inst.setID(makeID());
                curBB.addInstruction(inst);
                ctxToValue.put(ctx, inst);
                return inst;
            }

            @Override
            public Parameter visitInstParam(InstParamContext ctx) {
                int index = intLitToInt(ctx.intLiteral());
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
                        IntConstant constant = new IntConstant(type,
                                (long) result);
                        int id = makeID();
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
                inst.setType(type);
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

    @Override
    public Void visitFuncDef(FuncDefContext ctx) {
        Function func = new Function();
        func.setID(makeID());
        func.setName(assertGlobal(ctx.IDENTIFIER().getText()));
        System.out.printf("Function found: %s\n", func.getName());
        func.setSig((FunctionSignature) visit(ctx.funcSig()));

        FuncBuilder funcBuilder = new FuncBuilder(func);
        funcBuilder.visit(ctx);

        bundle.getFuncs().put(func.getID(), func);

        return null;
    }

    @Override
    public FunctionSignature visitReferencedFuncSig(ReferencedFuncSigContext ctx) {
        throw new ASTParsingException("Named type not implemented");
    }

    @Override
    public FunctionSignature visitInLineFuncSig(InLineFuncSigContext ctx) {
        System.out.printf("Function signature: %s\n", ctx.getText());
        FunctionSignature sig = new FunctionSignature();
        sig.setID(makeID());

        List<TypeContext> types = ctx.funcSigConstructor().type();

        Type returnType = (Type) visit(types.get(0));
        System.out.printf("Return type is %s\n", returnType);
        sig.setReturnType(returnType);

        for (int i = 1; i < types.size(); i++) {
            Type paramType = (Type) visit(types.get(i));
            System.out.printf("Param type %s\n", paramType);
            sig.getParamTypes().add(paramType);
        }

        bundle.getFuncSigs().put(sig.getID(), sig);

        return sig;
    }

    /**
     * Assert if an identifier is global
     * 
     * @param id
     *            An id.
     * @return The id parameter itself
     * @throws ASTParsingException
     *             Thrown if the ID is not global.
     */
    public static String assertGlobal(String id) throws ASTParsingException {
        if (id.charAt(0) != '@') {
            throw new ASTParsingException("Met identifier " + id
                    + " while expecting a global identifier");
        }
        return id;
    }

}
