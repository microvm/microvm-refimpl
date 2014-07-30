package uvm.ir.text.input;

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
import uvm.ir.text.input.RecursiveBundleBuilder;

public class TestingHelper {
    public static Bundle parseUir(String file, Bundle globalBundle)
            throws IOException, FileNotFoundException {
        FileInputStream is = new FileInputStream(file);
        return parseUir(is, globalBundle);
    }

    public static Bundle parseUir(String file) throws IOException,
            FileNotFoundException {
        FileInputStream is = new FileInputStream(file);
        return parseUir(is, null);
    }

    public static Bundle parseUir(InputStream is) throws IOException {
        return parseUir(is, null);
    }

    public static Bundle parseUir(InputStream is, Bundle globalBundle)
            throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(is);
        uIRLexer lexer = new uIRLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        uIRParser parser = new uIRParser(tokens);
        IrContext ir = parser.ir();

        if (parser.getNumberOfSyntaxErrors() > 0) {
            System.err.println("Syntax error");
            Assert.fail("Syntax error");
        }

        RecursiveBundleBuilder rbb = globalBundle == null ? new RecursiveBundleBuilder()
                : new RecursiveBundleBuilder(globalBundle);
        rbb.build(ir);
        return rbb.getBundle();
    }
}
