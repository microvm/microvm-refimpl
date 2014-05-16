package uvm.ir.text.input;

import org.antlr.v4.runtime.ParserRuleContext;

public class ParserHelper {
    public static void parseError(String msg) {
        throw new ASTParsingException(msg);
    }

    public static void parseError(ParserRuleContext ctx, String msg) {
        throw new ASTParsingException(String.format("line %d col %d: %s", ctx
                .getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                msg));
    }
}
