package uvm.platformsupport;

import java.math.BigInteger;

/**
 * An abstract interface for accessing the memory.
 */
public interface MemorySupport {
    byte loadByte(long addr);

    short loadShort(long addr);

    int loadInt(long addr);

    long loadLong(long addr);

    BigInteger loadI128(long addr);

    float loadFloat(long addr);

    double loadDouble(long addr);

    void storeByte(long addr, byte value);

    void storeShort(long addr, short value);

    void storeInt(long addr, int value);

    void storeLong(long addr, long value);

    void storeI128(long addr, BigInteger value);
     
    void storeFloat(long addr, float value);

    void storeDouble(long addr, double value);

    int cmpXchgInt(long addr, int expected, int desired);

    long cmpXchgLong(long addr, long expected, long desired);

    int fetchXchgInt(long addr, int opnd);

    int fetchAddInt(long addr, int opnd);

    int fetchSubInt(long addr, int opnd);

    int fetchAndInt(long addr, int opnd);

    int fetchNandInt(long addr, int opnd);

    int fetchOrInt(long addr, int opnd);

    int fetchXorInt(long addr, int opnd);

    int fetchMaxInt(long addr, int opnd);

    int fetchMinInt(long addr, int opnd);

    int fetchUmaxInt(long addr, int opnd);

    int fetchUminInt(long addr, int opnd);

    long fetchXchgLong(long addr, long opnd);

    long fetchAddLong(long addr, long opnd);

    long fetchSubLong(long addr, long opnd);

    long fetchAndLong(long addr, long opnd);

    long fetchNandLong(long addr, long opnd);

    long fetchOrLong(long addr, long opnd);

    long fetchXorLong(long addr, long opnd);

    long fetchMaxLong(long addr, long opnd);

    long fetchMinLong(long addr, long opnd);

    long fetchUmaxLong(long addr, long opnd);

    long fetchUminLong(long addr, long opnd);

    void fence();

}
