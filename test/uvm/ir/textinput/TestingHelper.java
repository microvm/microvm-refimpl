package uvm.ir.textinput;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Assert;

import parser.uIRLexer;
import parser.uIRParser;
import parser.uIRParser.IrContext;
import uvm.Bundle;

public class TestingHelper {

    public static Bundle parseUir(String file) throws IOException,
            FileNotFoundException {
        FileInputStream is = new FileInputStream(file);
        return parseUir(is);
    }

    public static Bundle parseUir(InputStream is) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(is);
        uIRLexer lexer = new uIRLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        uIRParser parser = new uIRParser(tokens);
        IrContext ir = parser.ir();

        if (parser.getNumberOfSyntaxErrors() > 0) {
            System.err.println("Syntax error");
            Assert.fail("Syntax error");
        }

        RecursiveBundleBuilder rbb = new RecursiveBundleBuilder();
        rbb.build(ir);
        return rbb.getBundle();
    }
}
