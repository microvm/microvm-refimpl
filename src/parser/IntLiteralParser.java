package parser;

import parser.uIRParser.DecIntLiteralContext;
import parser.uIRParser.HexIntLiteralContext;
import parser.uIRParser.OctIntLiteralContext;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Parse integer literals.
 */
class IntLiteralParser extends uIRBaseVisitor<Long> {
    @Override
    public Long visitDecIntLiteral(DecIntLiteralContext ctx) {
        return Long.parseLong(ctx.getText());
    }

    @Override
    public Long visitOctIntLiteral(OctIntLiteralContext ctx) {
        return Long.parseLong(ctx.getText(), 8);
    }

    @Override
    public Long visitHexIntLiteral(HexIntLiteralContext ctx) {
        String text = ctx.getText();
        boolean negative = text.charAt(0) == '-';
        String body = text.substring(text.indexOf('x') + 1);
        long num = Long.parseLong(body, 16);
        if (negative) {
            num = -num;
        }
        return num;
    }
}