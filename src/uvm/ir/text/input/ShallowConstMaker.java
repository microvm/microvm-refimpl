package uvm.ir.text.input;

import parser.uIRBaseVisitor;
import parser.uIRParser.DoubleConstContext;
import parser.uIRParser.FloatConstContext;
import parser.uIRParser.IntConstContext;
import parser.uIRParser.NullConstContext;
import parser.uIRParser.StructConstContext;
import uvm.ssavalue.Constant;
import uvm.ssavalue.DoubleConstant;
import uvm.ssavalue.FloatConstant;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.StructConstant;
import uvm.type.AbstractReferenceType;
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
            ParserHelper.parseError(ctx, "Int literal " + ctx.getText()
                    + " found. Expect" + expectedType);
        }

        Int type = (Int) expectedType;
        long value = rbb.intLitToLong(ctx.intLiteral());
        IntConstant constant = new IntConstant(type, value);
        makeIDAndRegister(constant);
        return constant;
    }

    @Override
    public FloatConstant visitFloatConst(FloatConstContext ctx) {
        if (!(expectedType instanceof uvm.type.Float)) {
            ParserHelper.parseError(ctx, "Float literal " + ctx.getText()
                    + " found. Expect" + expectedType);
        }

        float value = rbb.floatLiteralParser.visit(ctx.floatLiteral());
        FloatConstant constant = new FloatConstant(expectedType, value);
        makeIDAndRegister(constant);
        return constant;
    }

    @Override
    public DoubleConstant visitDoubleConst(DoubleConstContext ctx) {
        if (!(expectedType instanceof uvm.type.Double)) {
            ParserHelper.parseError(ctx, "Double literal " + ctx.getText()
                    + " found. Expect" + expectedType);
        }

        double value = rbb.doubleLiteralParser.visit(ctx.doubleLiteral());
        DoubleConstant constant = new DoubleConstant(expectedType, value);
        makeIDAndRegister(constant);
        return constant;
    }

    @Override
    public StructConstant visitStructConst(StructConstContext ctx) {
        if (!(expectedType instanceof Struct)) {
            ParserHelper.parseError(ctx, "Struct literal " + ctx.getText()
                    + " found. Expect" + expectedType);
        }

        Struct type = (Struct) expectedType;

        int actualFields = ctx.constant().size();
        int expectedFields = type.getFieldTypes().size();

        if (actualFields != expectedFields) {
            ParserHelper.parseError(ctx, "Found " + actualFields + " fields: "
                    + ctx.getText() + " Expect " + expectedFields + " fields.");
        }

        StructConstant constant = new StructConstant();
        constant.setType(type);
        makeIDAndRegister(constant);

        return constant;
    }

    @Override
    public NullConstant visitNullConst(NullConstContext ctx) {
        if (!((expectedType instanceof AbstractReferenceType) //
                || (expectedType instanceof Func) //
                || (expectedType instanceof uvm.type.Thread) //
        || (expectedType instanceof uvm.type.Stack)//
        )) {
            ParserHelper.parseError(ctx, "NULL literal found. Expect"
                    + expectedType);
        }

        NullConstant constant = new NullConstant();
        constant.setType(expectedType);
        makeIDAndRegister(constant);

        return constant;
    }

    private void makeIDAndRegister(Constant constant) {
        int id = rbb.makeID();
        constant.setID(id);
        rbb.bundle.getGlobalValueNs().put(id, null, constant);
        rbb.bundle.getDeclaredConstNs().put(id, null, constant);
    }
}