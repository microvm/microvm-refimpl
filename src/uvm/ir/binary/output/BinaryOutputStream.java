package uvm.ir.binary.output;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import uvm.Identified;
import uvm.ir.io.NestedIOException;

/**
 * A helper class that writes numbers to an output stream.
 * <p>
 * This class uses little endian as specified by the µVM design document.
 */
public class BinaryOutputStream extends FilterOutputStream {
    public BinaryOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    public void writeByte(byte num) {
        try {
            out.write(num);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    public void writeShort(short num) {
        try {
            int b0 = num & 0xff;
            int b1 = (num >> 8) & 0xff;
            out.write(b0);
            out.write(b1);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    public void writeInt(int num) {
        try {
            int b0 = num & 0xff;
            int b1 = (num >> 8) & 0xff;
            int b2 = (num >> 16) & 0xff;
            int b3 = (num >> 24) & 0xff;
            out.write(b0);
            out.write(b1);
            out.write(b2);
            out.write(b3);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    public void writeLong(long num) {
        try {
            int b0 = (int) (num & 0xff);
            int b1 = (int) ((num >> 8) & 0xff);
            int b2 = (int) ((num >> 16) & 0xff);
            int b3 = (int) ((num >> 24) & 0xff);
            int b4 = (int) ((num >> 32) & 0xff);
            int b5 = (int) ((num >> 40) & 0xff);
            int b6 = (int) ((num >> 48) & 0xff);
            int b7 = (int) ((num >> 56) & 0xff);
            out.write(b0);
            out.write(b1);
            out.write(b2);
            out.write(b3);
            out.write(b4);
            out.write(b5);
            out.write(b6);
            out.write(b7);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    public void writeFloat(float num) {
        writeInt(Float.floatToRawIntBits(num));
    }

    public void writeDouble(double num) {
        writeLong(Double.doubleToRawLongBits(num));
    }

    public void writeID(int id) {
        writeInt(id);
    }

    public void writeID(Identified obj) {
        writeInt(obj.getID());
    }

    public void writeLen(int len) {
        writeShort((short) len);
    }

    public void writeLen(Collection<?> col) {
        writeShort((short) col.size());
    }

    public void writeArySz(int sz) {
        writeLong(sz);
    }

    public void writeOpc(byte opc) {
        writeByte(opc);
    }

    public void writeOpc(int opcode) {
        writeByte((byte) opcode);
    }
}
