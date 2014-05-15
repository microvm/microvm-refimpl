package uvm.ir.textinput;

import parser.uIRBaseVisitor;
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
        String text = ctx.getText();
        boolean negative = text.charAt(0) == '-';
        if (negative) {
            text = text.substring(1);
        }
        return parseText(text, negative, 10L);
    }

    @Override
    public Long visitOctIntLiteral(OctIntLiteralContext ctx) {
        String text = ctx.getText();
        boolean negative = text.charAt(0) == '-';
        if (negative) {
            text = text.substring(1);
        }
        return parseText(ctx.getText(), negative, 8L);
    }

    @Override
    public Long visitHexIntLiteral(HexIntLiteralContext ctx) {
        String text = ctx.getText();
        boolean negative = text.charAt(0) == '-';
        if (negative) {
            text = text.substring(3); // '-0x'
        } else {
            text = text.substring(2); // '0x'
        }
        return parseText(ctx.getText(), negative, 16L);
    }

    private long parseText(String text, boolean negative, long base) {
        long num = IntParsingUtils.manualParse(text, base);
        if (negative) {
            num = -num;
        }
        return num;
    }

}