package uvm.ir.textinput;

import parser.uIRParser.ConstantContext;
import parser.uIRParser.StructConstContext;
import uvm.ssavalue.Constant;
import uvm.ssavalue.StructConstant;
import uvm.type.Struct;
import uvm.type.Type;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Re-visit a constant, fill in their dependencies to other constants.
 */
final class ConstPopulator {
    private final RecursiveBundleBuilder rbb;

    ConstPopulator(RecursiveBundleBuilder recursiveBundleBuilder) {
        rbb = recursiveBundleBuilder;
    }

    public void visitStructConstant(StructConstant constant, Struct type,
            StructConstContext ctx) {

        int fields = type.getFieldTypes().size();

        for (int i = 0; i < fields; i++) {
            ConstantContext subExpr = ctx.constant(i);
            Type fieldType = type.getFieldTypes().get(i);
            Constant subConstant = new DeepConstMaker(rbb, fieldType)
                    .visit(subExpr);
            constant.getValues().add(subConstant);
        }

    }

}