package uvm.ir.binary;

import static uvm.ir.text.input.TestingHelper.parseUir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import uvm.Bundle;
import uvm.ir.binary.input.IRBinaryReader;
import uvm.ir.binary.output.IRBinaryWriter;

public class WriterTestUtils {

    static Bundle loadWriteReload(String bundleName) throws IOException,
            FileNotFoundException {
        System.out.format("==========================%s====================\n",
                bundleName);
        Bundle goodBundle = parseUir(bundleName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        try (IRBinaryWriter bw = new IRBinaryWriter(baos)) {
            bw.writeBundle(goodBundle);
        }

        byte[] ba = baos.toByteArray();
        WriterTestUtils.hexDump(ba);

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);

        Bundle readBundle;

        try (IRBinaryReader br = new IRBinaryReader(bais)) {
            br.readBundle();
            readBundle = br.getBundle();
        }

        return readBundle;
    }

    public static void hexDump(byte[] ba) {
        int offset = 0;
        while (ba.length - offset > 16) {
            System.out.printf("%8x   ", offset);
            for (int i = offset; i < offset + 16; i++) {
                System.out.printf(" %02x", ba[i]);
            }
            System.out.println();
            offset += 16;
        }
        {
            System.out.printf("%8x   ", offset);
            for (int i = offset; i < ba.length; i++) {
                System.out.printf(" %02x", ba[i]);
            }
            System.out.println();
        }
    }

}
