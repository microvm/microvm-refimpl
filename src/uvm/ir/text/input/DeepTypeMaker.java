package uvm.ir.text.input;

import parser.uIRParser.ArrayTypeContext;
import parser.uIRParser.FuncTypeContext;
import parser.uIRParser.HybridTypeContext;
import parser.uIRParser.IRefTypeContext;
import parser.uIRParser.RefTypeContext;
import parser.uIRParser.ReferencedTypeContext;
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
 * Visit types recursively. This visitor class assumes that all declared types
 * (by .typedef) and declared function signatures (by .funcsig) are already
 * handled and will lookup the bundle for those types.
 */
class DeepTypeMaker extends ShallowTypeMaker {

    DeepTypeMaker(RecursiveBundleBuilder recursiveBundleBuilder) {
        super(recursiveBundleBuilder);
    }

    @Override
    public Type visitReferencedType(ReferencedTypeContext ctx) {
        String name = ctx.GLOBAL_ID().getText();
        Type type = this.rbb.bundle.getTypeNs().getByName(name);

        if (type == null) {
            type = this.rbb.globalBundle.getTypeNs().getByName(name);
            if (type == null) {
                ParserHelper.parseError(ctx, "Undefined type " + name);
            }
        }

        return type;
    }

    @Override
    public Ref visitRefType(RefTypeContext ctx) {
        Ref type = super.visitRefType(ctx);
        this.rbb.typeAndSigPopulator.visitRefType(type, ctx);
        return type;
    }

    @Override
    public IRef visitIRefType(IRefTypeContext ctx) {
        IRef type = super.visitIRefType(ctx);
        this.rbb.typeAndSigPopulator.visitIRefType(type, ctx);
        return type;
    }

    @Override
    public WeakRef visitWeakRefType(WeakRefTypeContext ctx) {
        WeakRef type = super.visitWeakRefType(ctx);
        this.rbb.typeAndSigPopulator.visitWeakRefType(type, ctx);
        return type;
    }

    @Override
    public Struct visitStructType(StructTypeContext ctx) {
        Struct type = super.visitStructType(ctx);
        this.rbb.typeAndSigPopulator.visitStructType(type, ctx);
        return type;
    }

    @Override
    public uvm.type.Array visitArrayType(ArrayTypeContext ctx) {
        Array type = super.visitArrayType(ctx);
        this.rbb.typeAndSigPopulator.visitArrayType(type, ctx);
        return type;
    }

    @Override
    public Hybrid visitHybridType(HybridTypeContext ctx) {
        Hybrid type = super.visitHybridType(ctx);
        this.rbb.typeAndSigPopulator.visitHybridType(type, ctx);
        return type;
    }

    @Override
    public Func visitFuncType(FuncTypeContext ctx) {
        Func type = super.visitFuncType(ctx);
        this.rbb.typeAndSigPopulator.visitFuncType(type, ctx);
        return type;
    }
}