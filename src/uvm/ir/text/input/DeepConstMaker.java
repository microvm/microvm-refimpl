package uvm.ir.text.input;

import parser.uIRParser.ReferencedConstContext;
import parser.uIRParser.StructConstContext;
import uvm.ssavalue.Constant;
import uvm.ssavalue.StructConstant;
import uvm.type.Struct;
import uvm.type.Type;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Like ShallowConstMaker, but also recursively construct nested (but not
 * recursive) Constant objects.
 */
public class DeepConstMaker extends ShallowConstMaker {

    public DeepConstMaker(RecursiveBundleBuilder recursiveBundleBuilder,
            Type expectedType) {
        super(recursiveBundleBuilder, expectedType);
    }

    @Override
    public Constant visitReferencedConst(ReferencedConstContext ctx) {
        String name = ctx.GLOBAL_ID().getText();
        Constant constant = rbb.bundle.getGlobalValueNs().getByName(name);
        
        if (constant == null) {
            constant = rbb.globalBundle.getGlobalValueNs().getByName(name);
            if (constant == null) {
                ParserHelper.parseError(ctx, "Undefined constant " + name);
            }
        }
        
        return constant;
    }

    @Override
    public StructConstant visitStructConst(StructConstContext ctx) {
        StructConstant constant = super.visitStructConst(ctx);
        rbb.constPopulator.visitStructConstant(constant, (Struct) expectedType,
                ctx);
        return constant;
    }
}
