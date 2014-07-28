package uvm.refimpl.itpr;

import java.math.BigInteger;

public class OpHelper {
    public static BigInteger mask(int n) {
        return BigInteger.ONE.shiftLeft(n).subtract(BigInteger.ONE);
    }

    // Trunc/ext from BigInteger

    public static BigInteger truncFromBigInteger(BigInteger n, int len) {
        return n.and(mask(len));
    }

    public static BigInteger zextToBigInteger(BigInteger n, int len) {
        return n.and(mask(len));
    }

    public static BigInteger sextToBigInteger(BigInteger n, int len) {
        boolean bit = n.testBit(len - 1);
        if (bit) {
            return n.or(mask(len - 1).not());
        } else {
            return n.and(mask(len - 1));
        }
    }

    // Prepare integers for binop or cmp
    public static BigInteger prepareUnsigned(BigInteger n, int len) {
        return truncFromBigInteger(n, len);
    }

    public static BigInteger prepareSigned(BigInteger n, int len) {
        return sextToBigInteger(truncFromBigInteger(n, len), len);
    }

    public static BigInteger unprepare(BigInteger n, int len) {
        return truncFromBigInteger(n, len);
    }

    // From one size to another size
    public static BigInteger trunc(BigInteger n, int toLen) {
        return truncFromBigInteger(n, toLen);
    }

    public static BigInteger zext(BigInteger n, int fromLen, int toLen) {
        return truncFromBigInteger(n, fromLen);
    }

    public static BigInteger sext(BigInteger n, int fromLen, int toLen) {
        return truncFromBigInteger(sextToBigInteger(n, fromLen), toLen);
    }

    // TagRef64 operations

    public static boolean tr64IsInt(long opnd) {
        return (opnd & 0x7ff0000000000001L) == 0x7ff0000000000001L;
    }

    public static boolean tr64IsFp(long opnd) {
        return (opnd & 0x7ff0000000000001L) != 0x7ff0000000000001L
                && (opnd & 0x7ff0000000000003L) != 0x7ff0000000000002L;
    }

    public static boolean tr64IsRef(long opnd) {
        return (opnd & 0x7ff0000000000003L) == 0x7ff0000000000002L;
    }

    public static long intToTr64(long opnd) {
        // NOTE: opnd is a 52-bit integer.
        return (0x7ff0000000000001L | // exponent and last two bits
        ((opnd & 0x7ffffffffffffL) << 1) | // last 51 bits, shifted
        ((opnd & 0x8000000000000L) << 12)); // the 51st bit
    }

    public static long fpToTr64(double opnd) {
        long bits = Double.doubleToRawLongBits(opnd);
        if (Double.isNaN(opnd)) {
            // Real NaN. Use this "canonical" NaN to avoid conflicting with Ref
            // and inf
            bits = bits & 0xfff8000000000000L | 0x0000000000000008L;
        }
        return bits;
    }

    public static long refToTr64(long opnd, long tag) {
        return (0x7ff0000000000002L | // exponent and last two bits
                (opnd & 0x7ffffffffff8L) | // 44 bits (no last 3 bits)
                ((opnd & 0x800000000000L) << 16) | // the 47th bit
                ((tag & 0x3eL) << 46) | // first 5 bits of tag
        ((tag & 0x1) << 2)); // the last bit of tag
    }

    public static long tr64ToInt(long opnd) {
        return (((opnd & 0xffffffffffffeL) >> 1) | // last 51 bits, shifted
        ((opnd & 0x8000000000000000L) >> 12) & (1L << 51));// the 51st bit
    }

    public static double tr64ToFp(long opnd) {
        return Double.longBitsToDouble(opnd); // all valid Doubles are "as-is"
    }

    public static long tr64ToRef(long opnd) {
        return ((opnd & 0x7ffffffffff8L) | // 44 bits (no last 3 bits)
        (((~(((opnd & 0x8000000000000000L) << 1) - 1)) >> 17) // sign bits
        & 0xffff800000000000L)); // Mask the high bits
    }

    public static long tr64ToTag(long opnd) {
        return (((opnd & 0x000f800000000000L) >> 46) | // high 5 bits
        ((opnd & 0x4) >> 2)); // the last bit
    }

}
