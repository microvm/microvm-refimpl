package uvm.ir.text.input;

import java.util.List;

import parser.uIRParser.ArrayTypeContext;
import parser.uIRParser.FuncSigConstructorContext;
import parser.uIRParser.FuncTypeContext;
import parser.uIRParser.HybridTypeContext;
import parser.uIRParser.IRefTypeContext;
import parser.uIRParser.RefTypeContext;
import parser.uIRParser.StructTypeContext;
import parser.uIRParser.TypeContext;
import parser.uIRParser.WeakRefTypeContext;
import uvm.FunctionSignature;
import uvm.type.Array;
import uvm.type.Func;
import uvm.type.Hybrid;
import uvm.type.IRef;
import uvm.type.Ref;
import uvm.type.Struct;
import uvm.type.Type;
import uvm.type.WeakRef;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Visit types and function signatures, fill in their dependencies to other
 * types or function signatures.
 */
final class TypeAndSigPopulator {
    private final RecursiveBundleBuilder rbb;

    TypeAndSigPopulator(RecursiveBundleBuilder recursiveBundleBuilder) {
        rbb = recursiveBundleBuilder;
    }

    public void visitRefType(Ref type, RefTypeContext ctx) {
        type.setReferenced(rbb.deepTypeMaker.visit(ctx.type()));
    }

    public void visitIRefType(IRef type, IRefTypeContext ctx) {
        type.setReferenced(rbb.deepTypeMaker.visit(ctx.type()));
    }

    public void visitWeakRefType(WeakRef type, WeakRefTypeContext ctx) {
        type.setReferenced(rbb.deepTypeMaker.visit(ctx.type()));
    }

    public void visitStructType(Struct type, StructTypeContext ctx) {
        for (TypeContext childCtx : ctx.type()) {
            Type childType = rbb.deepTypeMaker.visit(childCtx);
            type.getFieldTypes().add(childType);
        }
    }

    public void visitArrayType(Array type, ArrayTypeContext ctx) {
        type.setElemType(rbb.deepTypeMaker.visit(ctx.type()));
    }

    public void visitHybridType(Hybrid type, HybridTypeContext ctx) {
        type.setFixedPart(rbb.deepTypeMaker.visit(ctx.type(0)));
        type.setVarPart(rbb.deepTypeMaker.visit(ctx.type(1)));
    }

    public void visitFuncType(Func type, FuncTypeContext ctx) {
        type.setSig(rbb.deepFuncSigMaker.visit(ctx.funcSig()));
    }

    public void visitFuncSigConstructor(FunctionSignature sig,
            FuncSigConstructorContext ctx) {
        List<TypeContext> types = ctx.type();

        Type returnType = rbb.deepTypeMaker.visit(types.get(0));
        sig.setReturnType(returnType);

        for (int i = 1; i < types.size(); i++) {
            Type paramType = rbb.deepTypeMaker.visit(types.get(i));
            sig.getParamTypes().add(paramType);
        }
    }
}