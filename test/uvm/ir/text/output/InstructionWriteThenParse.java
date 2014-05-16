package uvm.ir.text.output;

import java.io.FileNotFoundException;
import java.io.IOException;

import uvm.ir.text.input.InstructionParsingTest;

public class InstructionWriteThenParse extends InstructionParsingTest {
    @Override
    protected void loadBundle() throws IOException, FileNotFoundException {
        bundle = WriterTestUtils.loadWriteReload(bundleName());
    }
}