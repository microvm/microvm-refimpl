package uvm.ir.textoutput;

import java.io.FileNotFoundException;
import java.io.IOException;

import uvm.ir.textinput.InstructionParsingTest;

public class InstructionWriteThenParse extends InstructionParsingTest {
    @Override
    protected void loadBundle() throws IOException, FileNotFoundException {
        bundle = WriterTestUtils.loadWriteReload(bundleName());
    }
}