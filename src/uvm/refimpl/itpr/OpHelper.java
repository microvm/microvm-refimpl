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
}
