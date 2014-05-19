package uvm.ir.binary.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import uvm.ir.io.NestedIOException;
import uvm.util.LogUtil;

/**
 * A helper class that reads numbers from an input stream.
 * <p>
 * This class uses little endian as specified by the µVM design document.
 */
public class BinaryInputStream extends FilterInputStream {
    public BinaryInputStream(InputStream in) {
        super(in);
    }

    public int readByte() {
        try {
            int b0 = in.read();
            int rv = b0;
            LogUtil.log("Read byte %d [%02x]\n", rv, b0);
            return rv;
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    public int readShort() {
        try {
            int b0 = in.read();
            int b1 = in.read();
            int rv = (b0 | (b1 << 8));
            LogUtil.log("Read short %d [%02x %02x]\n", rv, b0, b1);
            return rv;
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
            int rv = b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
            LogUtil.log("Read int %d [%02x %02x %02x %02x]\n", rv, b0, b1, b2,
                    b3);
            return rv;
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
            long rv = b0 | (b1 << 8L) | (b2 << 16L) | (b3 << 24L) | (b4 << 32L)
                    | (b5 << 40L) | (b6 << 48L) | (b7 << 56L);
            LogUtil.log(
                    "Read long %d [%02x %02x %02x %02x %02x %02x %02x %02x]\n",
                    rv, b0, b1, b2, b3, b4, b5, b6, b7);
            return rv;
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    public float readFloat() {
        int bits = readInt();
        float rv = Float.intBitsToFloat(bits);
        LogUtil.log(" ... then interpret to float %f\n", rv);
        return rv;
    }

    public double readDouble() {
        long bits = readLong();
        double rv = Double.longBitsToDouble(bits);
        LogUtil.log(" ... then interpret to double %f\n", rv);
        return rv;
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
        try {
            int rv = read();
            LogUtil.log("Read opc %d [%02x]\n", rv, rv);
            return rv;
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    /**
     * Read a byte, anticipating EOF.
     * 
     * @return The byte, or -1 when encountering EOF.
     */
    public int maybeReadOpc() {
        try {
            int rv = read();
            LogUtil.log("Read maybeOpc %d [%02x]\n", rv, rv);
            return rv;
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

}
