package uvm.ir.textoutput;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.Test;

import uvm.Bundle;
import uvm.ir.textinput.TestingHelper;

public class WriterTest {

    @Test
    public void tests() throws FileNotFoundException, IOException {
        writeBundleFile("tests/uvm-parsing-test/types.uir");
        writeBundleFile("tests/uvm-parsing-test/constants.uir");
        writeBundleFile("tests/uvm-parsing-test/functions.uir");
        writeBundleFile("tests/uvm-parsing-test/instructions.uir");
        writeBundleFile("tests/micro-bm/int-prime-number/prime-number.uir");
    }

    private void writeBundleFile(String file) throws IOException,
            FileNotFoundException {
        System.out.format("==========================%s====================\n",
                file);
        Bundle bundle = TestingHelper.parseUir(file);
        writeBundle(bundle);
    }

    private void writeBundle(Bundle bundle) {
        new IRTextWriter(new OutputStreamWriter(System.out))
                .writeBundle(bundle);
    }

}
