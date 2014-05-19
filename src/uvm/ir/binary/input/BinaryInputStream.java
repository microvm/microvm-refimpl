package uvm.ir.binary.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import uvm.ir.io.NestedIOException;

/**
 * A helper class that reads numbers from an input stream.
 * <p>
 * This class uses little endian as specified by the µVM design document.
 */
public class BinaryInputStream extends FilterInputStream {
    public BinaryInputStream(InputStream in) {
        super(in);
    }

    public byte readByte() {
        try {
            int b0 = in.read();
            return (byte) b0;
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    public short readShort() {
        try {
            int b0 = in.read();
            int b1 = in.read();
            return (short) (b0 | (b1 << 8));
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    public int readInt() {
        try {
            int b0 = in.read();
            int b1 = in.read();
            int b2 = in.read();
            int b3 = in.read();
            return (b0 | (b1 << 8) | (b2 << 16) | (b3 << 24));
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    public long readLong() {
        try {
            long b0 = in.read();
            long b1 = in.read();
            long b2 = in.read();
            long b3 = in.read();
            long b4 = in.read();
            long b5 = in.read();
            long b6 = in.read();
            long b7 = in.read();
            return (b0 | (b1 << 8L) | (b2 << 16L) | (b3 << 24L) | (b4 << 32L)
                    | (b5 << 40L) | (b6 << 48L) | (b7 << 56L));
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    public float readFloat() {
        int bits = readInt();
        return Float.intBitsToFloat(bits);
    }

    public double readDouble() {
        long bits = readLong();
        return Double.longBitsToDouble(bits);
    }

    public int readID() {
        return readInt();
    }

    public int readLen() {
        return readShort();
    }

    public long readArySz() {
        return readLong();
    }

    public int readOpc() {
        return readByte();
    }

    /**
     * Read a byte, anticipating EOF.
     * 
     * @return The byte, or -1 when encountering EOF.
     */
    public int maybeReadOpc() {
        try {
            return read();
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

}
