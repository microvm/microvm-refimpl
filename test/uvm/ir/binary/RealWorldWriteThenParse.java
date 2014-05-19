package uvm.ir.binary;

import java.io.FileNotFoundException;
import java.io.IOException;

import uvm.ir.text.input.RealWorldParsingTest;

public class RealWorldWriteThenParse extends RealWorldParsingTest {
    @Override
    protected void loadBundle() throws IOException, FileNotFoundException {
        bundle = WriterTestUtils.loadWriteReload(bundleName());
    }
}