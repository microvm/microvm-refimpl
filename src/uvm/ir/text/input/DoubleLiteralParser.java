package uvm.ir.text.input;

import parser.uIRBaseVisitor;
import parser.uIRParser.DoubleBitsContext;
import parser.uIRParser.DoubleInfContext;
import parser.uIRParser.DoubleNanContext;
import parser.uIRParser.DoubleNumberContext;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Parse FP literals.
 */
class DoubleLiteralParser extends uIRBaseVisitor<Double> {
    public static IntLiteralParser ILP = new IntLiteralParser();

    @Override
    public Double visitDoubleNumber(DoubleNumberContext ctx) {
        return Double.parseDouble(ctx.FP_NUM().getText());
    }

    @Override
    public Double visitDoubleNan(DoubleNanContext ctx) {
        return Double.NaN;
    }

    @Override
    public Double visitDoubleInf(DoubleInfContext ctx) {
        if (ctx.INF().getText().charAt(0) == '+') {
            return Double.POSITIVE_INFINITY;
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    @Override
    public Double visitDoubleBits(DoubleBitsContext ctx) {
        long longBits = ILP.visit(ctx.intLiteral());
        return Double.longBitsToDouble(longBits);
    }
}