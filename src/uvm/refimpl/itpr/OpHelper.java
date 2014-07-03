package uvm.refimpl.itpr;

public class OpHelper {
    public static long mask(long n) {
        if (n >= 64) {
            return -1;
        } else {
            return (1L << n) - 1;
        }
    }

    // Prepare integers for binop or cmp
    public static long prepareUnsigned(long n, long len) {
        return truncFromLong(n, len);
    }

    public static long prepareSigned(long n, long len) {
        return sextToLong(truncFromLong(n, len), len);
    }

    public static long unprepare(long n, long len) {
        return truncFromLong(n, len);
    }

    // Truncate and extend

    // From/to long
    public static long truncFromLong(long n, long toLen) {
        return n & mask(toLen);
    }

    public static long zextToLong(long n, long fromLen) {
        return truncFromLong(n, fromLen);
    }

    public static long sextToLong(long n, long fromLen) {
        long sign = n & (1 << (fromLen - 1));
        long mask = ~(sign - 1); // 00100000 -> 00011111 -> 11100000
                                 // 00000000 -> 11111111 -> 00000000
        return n | mask;
    }

    // From one size to another size
    public static long trunc(long n, long toLen) {
        return truncFromLong(n, toLen);
    }

    public static long zext(long n, long fromLen, long toLen) {
        return truncFromLong(n, fromLen);
    }

    public static long sext(long n, long fromLen, long toLen) {
        return truncFromLong(sextToLong(n, fromLen), toLen);
    }
}
