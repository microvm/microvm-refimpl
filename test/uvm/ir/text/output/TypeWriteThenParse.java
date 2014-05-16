package uvm.ir.text.output;

import java.io.FileNotFoundException;
import java.io.IOException;

import uvm.ir.text.input.TypeParsingTest;

public class TypeWriteThenParse extends TypeParsingTest {
    @Override
    protected void loadBundle() throws IOException, FileNotFoundException {
        bundle = WriterTestUtils.loadWriteReload(bundleName());
    }
}