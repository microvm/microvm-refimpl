package parser;

import parser.uIRParser.FpLiteralContext;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Parse FP literals.
 */
class FPLiteralParser extends uIRBaseVisitor<Double> {
    @Override
    public Double visitFpLiteral(FpLiteralContext ctx) {
        return Double.parseDouble(ctx.getText());
    }
}