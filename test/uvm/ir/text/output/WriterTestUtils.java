package uvm.ir.text.output;

import static uvm.ir.text.input.TestingHelper.parseUir;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import uvm.Bundle;
import uvm.ir.text.output.IRTextWriter;

public class WriterTestUtils {

    static Bundle loadWriteReload(String bundleName) throws IOException,
            FileNotFoundException {
        System.out.format("==========================%s====================\n",
                bundleName);
        Bundle goodBundle = parseUir(bundleName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        OutputStreamWriter osw = new OutputStreamWriter(baos);
        new IRTextWriter(osw).writeBundle(goodBundle);
        byte[] ba = baos.toByteArray();
        WriterTestUtils.printWithLineNumber(ba);
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        Bundle readBundle = parseUir(bais);
        return readBundle;
    }

    static void printWithLineNumber(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        InputStreamReader isr = new InputStreamReader(bais);
        BufferedReader br = new BufferedReader(isr);
        int lineNo = 1;
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            System.out.format("%-8d", lineNo++);
            System.out.println(line);
        }
    }

}
