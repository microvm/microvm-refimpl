package uvm.ir.textoutput;

import java.io.FileNotFoundException;
import java.io.IOException;

import uvm.ir.textinput.ConstantParsingTest;

public class ConstantWriteThenParse extends ConstantParsingTest {
    @Override
    protected void loadBundle() throws IOException, FileNotFoundException {
        bundle = WriterTestUtils.loadWriteReload(bundleName());
    }
}
