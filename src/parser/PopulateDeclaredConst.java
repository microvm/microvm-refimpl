package parser;

import parser.uIRParser.StructConstContext;
import uvm.ssavalue.Constant;
import uvm.ssavalue.StructConstant;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Call the TypeAndSigPopulator on all type declarations (.typedef).
 */
final class PopulateDeclaredConst extends uIRBaseVisitor<Void> {
    private final RecursiveBundleBuilder rbb;
    private final Constant oldConstant;

    PopulateDeclaredConst(RecursiveBundleBuilder recursiveBundleBuilder,
            Constant oldConstant) {
        rbb = recursiveBundleBuilder;
        this.oldConstant = oldConstant;
    }

    @Override
    public Void visitStructConst(StructConstContext ctx) {
        StructConstant constant = (StructConstant) oldConstant;
        rbb.constPopulator.visitStructConstant(constant, constant.getType(),
                ctx);
        return null;
    }
}