package parser;

import parser.uIRParser.FPConstContext;
import parser.uIRParser.IntConstContext;
import parser.uIRParser.NullConstContext;
import parser.uIRParser.StructConstContext;
import uvm.ssavalue.Constant;
import uvm.ssavalue.FPConstant;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.StructConstant;
import uvm.type.AbstractReferenceType;
import uvm.type.FPType;
import uvm.type.Func;
import uvm.type.Int;
import uvm.type.Struct;
import uvm.type.Type;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Handles constExpr. Match constant expressions (literals) against the expected
 * type, but does not populate nested constants (currently only struct
 * constants).
 */
class ShallowConstMaker extends uIRBaseVisitor<Constant> {
    final RecursiveBundleBuilder rbb;
    Type expectedType;

    public ShallowConstMaker(RecursiveBundleBuilder recursiveBundleBuilder,
            Type expectedType) {
        rbb = recursiveBundleBuilder;
        this.expectedType = expectedType;
    }

    @Override
    public IntConstant visitIntConst(IntConstContext ctx) {
        if (!(expectedType instanceof Int)) {
            throw new ASTParsingException("Int literal " + ctx.getText()
                    + " found. Expect" + expectedType);
        }

        Int type = (Int) expectedType;
        long value = rbb.intLitToLong(ctx.intLiteral());
        IntConstant constant = new IntConstant(type, value);
        constant.setID(rbb.makeID());
        rbb.bundle.registerConstant(constant.getID(), null, constant);
        return constant;
    }

    @Override
    public FPConstant visitFPConst(FPConstContext ctx) {
        if (!(expectedType instanceof FPType)) {
            throw new ASTParsingException("FP literal " + ctx.getText()
                    + " found. Expect" + expectedType);
        }

        double value = rbb.fpLiteralParser.visit(ctx.fpLiteral());
        FPConstant constant = new FPConstant(expectedType, value);
        constant.setID(rbb.makeID());
        rbb.bundle.registerConstant(constant.getID(), null, constant);
        return constant;
    }

    @Override
    public StructConstant visitStructConst(StructConstContext ctx) {
        if (!(expectedType instanceof Struct)) {
            throw new ASTParsingException("Int literal " + ctx.getText()
                    + " found. Expect" + expectedType);
        }

        Struct type = (Struct) expectedType;

        int actualFields = ctx.constant().size();
        int expectedFields = type.getFieldTypes().size();

        if (actualFields != expectedFields) {
            throw new ASTParsingException("Found " + actualFields + " fields: "
                    + ctx.getText() + " Expect " + expectedFields + " fields.");
        }

        StructConstant constant = new StructConstant();
        constant.setType(type);
        constant.setID(rbb.makeID());
        rbb.bundle.registerConstant(constant.getID(), null, constant);

        return constant;
    }

    @Override
    public NullConstant visitNullConst(NullConstContext ctx) {
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
        constant.setID(rbb.makeID());
        rbb.bundle.registerConstant(constant.getID(), null, constant);

        return constant;
    }
}