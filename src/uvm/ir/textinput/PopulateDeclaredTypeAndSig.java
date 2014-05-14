package uvm.ir.textinput;

import parser.uIRBaseVisitor;
import parser.uIRParser.ArrayTypeContext;
import parser.uIRParser.FuncTypeContext;
import parser.uIRParser.HybridTypeContext;
import parser.uIRParser.IRefTypeContext;
import parser.uIRParser.RefTypeContext;
import parser.uIRParser.StructTypeContext;
import parser.uIRParser.WeakRefTypeContext;
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
 * Call the TypeAndSigPopulator on all type declarations (.typedef).
 */
final class PopulateDeclaredTypeAndSig extends uIRBaseVisitor<Void> {
    private final RecursiveBundleBuilder rbb;
    private final Type oldType;

    PopulateDeclaredTypeAndSig(RecursiveBundleBuilder recursiveBundleBuilder, Type oldType) {
        rbb = recursiveBundleBuilder;
        this.oldType = oldType;
    }

    @Override
    public Void visitRefType(RefTypeContext ctx) {
        Ref type = (Ref) oldType;
        rbb.typeAndSigPopulator.visitRefType(type, ctx);
        return null;
    }

    @Override
    public Void visitIRefType(IRefTypeContext ctx) {
        IRef type = (IRef) oldType;
        rbb.typeAndSigPopulator.visitIRefType(type, ctx);
        return null;
    }

    @Override
    public Void visitWeakRefType(WeakRefTypeContext ctx) {
        WeakRef type = (WeakRef) oldType;
        rbb.typeAndSigPopulator.visitWeakRefType(type, ctx);
        return null;
    }

    @Override
    public Void visitStructType(StructTypeContext ctx) {
        Struct type = (Struct) oldType;
        rbb.typeAndSigPopulator.visitStructType(type, ctx);
        return null;
    }

    @Override
    public Void visitArrayType(ArrayTypeContext ctx) {
        Array type = (Array) oldType;
        rbb.typeAndSigPopulator.visitArrayType(type, ctx);
        return null;
    }

    @Override
    public Void visitHybridType(HybridTypeContext ctx) {
        Hybrid type = (Hybrid) oldType;
        rbb.typeAndSigPopulator.visitHybridType(type, ctx);
        return null;
    }

    @Override
    public Void visitFuncType(FuncTypeContext ctx) {
        Func type = (Func) oldType;
        rbb.typeAndSigPopulator.visitFuncType(type, ctx);
        return null;
    }
}