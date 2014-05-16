package uvm.ir.text.input;

import parser.uIRBaseVisitor;
import parser.uIRParser.FloatBitsContext;
import parser.uIRParser.FloatInfContext;
import parser.uIRParser.FloatNanContext;
import parser.uIRParser.FloatNumberContext;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Parse FP literals.
 */
class FloatLiteralParser extends uIRBaseVisitor<Float> {
    public static IntLiteralParser ILP = new IntLiteralParser();

    @Override
    public Float visitFloatNumber(FloatNumberContext ctx) {
        return Float.parseFloat(ctx.FP_NUM().getText());
    }

    @Override
    public Float visitFloatNan(FloatNanContext ctx) {
        return Float.NaN;
    }

    @Override
    public Float visitFloatInf(FloatInfContext ctx) {
        if (ctx.INF().getText().charAt(0) == '+') {
            return Float.POSITIVE_INFINITY;
        } else {
            return Float.NEGATIVE_INFINITY;
        }
    }

    @Override
    public Float visitFloatBits(FloatBitsContext ctx) {
        long longBits = ILP.visit(ctx.intLiteral());
        int intBits = (int) longBits;
        return Float.intBitsToFloat(intBits);
    }
}