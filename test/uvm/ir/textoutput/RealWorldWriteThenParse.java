package uvm.ir.textoutput;

import java.io.FileNotFoundException;
import java.io.IOException;

import uvm.ir.textinput.RealWorldParsingTest;

public class RealWorldWriteThenParse extends RealWorldParsingTest {
    @Override
    protected void loadBundle() throws IOException, FileNotFoundException {
        bundle = WriterTestUtils.loadWriteReload(bundleName());
    }
}