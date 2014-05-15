package uvm.ir.textoutput;

import java.io.FileNotFoundException;
import java.io.IOException;

import uvm.ir.textinput.FunctionParsingTest;

public class FunctionWriteThenParse extends FunctionParsingTest {
    @Override
    protected void loadBundle() throws IOException, FileNotFoundException {
        bundle = WriterTestUtils.loadWriteReload(bundleName());
    }
}