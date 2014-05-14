package uvm.ir.textinput;

public class ParserHelper {
    public static void parseError(String msg) {
        throw new ASTParsingException(msg);
    }
}
