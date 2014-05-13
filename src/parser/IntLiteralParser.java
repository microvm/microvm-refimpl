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
        return parseText(ctx.getText(), 10L);
    }

    @Override
    public Long visitOctIntLiteral(OctIntLiteralContext ctx) {
        return parseText(ctx.getText(), 8L);
    }

    @Override
    public Long visitHexIntLiteral(HexIntLiteralContext ctx) {
        return parseText(ctx.getText(), 16L);
    }

    private long parseText(String text, long base) {
        boolean negative = text.charAt(0) == '-';
        String body = text.substring(text.indexOf('x') + 1);
        long num = IntParsingUtils.manualParse(body, base);
        if (negative) {
            num = -num;
        }
        return num;
    }

}