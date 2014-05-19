package uvm.ir.binary;

import java.io.FileNotFoundException;
import java.io.IOException;

import uvm.ir.text.input.ConstantParsingTest;

public class ConstantWriteThenParse extends ConstantParsingTest {
    @Override
    protected void loadBundle() throws IOException, FileNotFoundException {
        bundle = WriterTestUtils.loadWriteReload(bundleName());
    }
}
