package uvm.ir.binary;

import java.io.FileNotFoundException;
import java.io.IOException;

import uvm.ir.text.input.FunctionParsingTest;

public class FunctionWriteThenParse extends FunctionParsingTest {
    @Override
    protected void loadBundle() throws IOException, FileNotFoundException {
        bundle = WriterTestUtils.loadWriteReload(bundleName());
    }
}