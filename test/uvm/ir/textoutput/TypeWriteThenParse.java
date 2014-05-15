package uvm.ir.textoutput;

import java.io.FileNotFoundException;
import java.io.IOException;

import uvm.ir.textinput.TypeParsingTest;

public class TypeWriteThenParse extends TypeParsingTest {
    @Override
    protected void loadBundle() throws IOException, FileNotFoundException {
        bundle = WriterTestUtils.loadWriteReload(bundleName());
    }
}