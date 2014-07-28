package uvm.platformsupport.ordinaryjava;

import java.lang.reflect.Field;
import java.math.BigInteger;

import sun.misc.Unsafe;
import uvm.platformsupport.MemorySupport;

/**
 * This class uses a byte array and the sun.misc.Unsafe object and Java's
 * "synchronized" keyword to emulate (atomic) memory access. Not all memory
 * operations are supported due to the limitation of the Unsafe class itself.
 */
public class UnsafeMemorySupport implements MemorySupport {

    static final Unsafe UNSAFE;
    static final long ABO;
    static final long AIS;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
            ABO = UNSAFE.arrayBaseOffset(byte[].class);
            AIS = UNSAFE.arrayIndexScale(byte[].class);
        } catch (Exception e) {
            throw new RuntimeException("Cannot obtain Unsafe", e);
        }
    }

    public static final int MEMORY_SIZE = 0x1600000; // 16MiB

    private byte[] mem = new byte[MEMORY_SIZE];

    public byte[] getMem() {
        return mem;
    }

    @Override
    public byte loadByte(long addr) {
        return UNSAFE.getByte(mem, ABO + addr * AIS);
    }

    @Override
    public short loadShort(long addr) {
        return UNSAFE.getShort(mem, ABO + addr * AIS);
    }

    @Override
    public int loadInt(long addr) {
        return UNSAFE.getInt(mem, ABO + addr * AIS);
    }

    @Override
    public long loadLong(long addr) {
        return UNSAFE.getLong(mem, ABO + addr * AIS);
    }

    @Override
    public BigInteger loadI128(long addr) {
        long low = UNSAFE.getLong(mem, ABO + addr * AIS);
        long high = UNSAFE.getLong(mem, ABO + (addr + 8) * AIS);
        return BigInteger.valueOf(high).shiftLeft(64)
                .add(BigInteger.valueOf(low));
    }

    @Override
    public float loadFloat(long addr) {
        return UNSAFE.getFloat(mem, ABO + addr * AIS);
    }

    @Override
    public double loadDouble(long addr) {
        return UNSAFE.getDouble(mem, ABO + addr * AIS);
    }

    @Override
    public void storeByte(long addr, byte value) {
        UNSAFE.putByte(mem, ABO + addr * AIS, value);
    }

    @Override
    public void storeShort(long addr, short value) {
        UNSAFE.putShort(mem, ABO + addr * AIS, value);
    }

    @Override
    public void storeInt(long addr, int value) {
        UNSAFE.putInt(mem, ABO + addr * AIS, value);
    }

    @Override
    public void storeLong(long addr, long value) {
        UNSAFE.putLong(mem, ABO + addr * AIS, value);
    }

    @Override
    public void storeI128(long addr, BigInteger value) {
        long low = value.longValue();
        long high = value.shiftRight(64).longValue();
        UNSAFE.putLong(mem, ABO + addr * AIS, low);
        UNSAFE.putLong(mem, ABO + (addr + 8) * AIS, high);
    }

    @Override
    public void storeFloat(long addr, float value) {
        UNSAFE.putFloat(mem, ABO + addr * AIS, value);
    }

    @Override
    public void storeDouble(long addr, double value) {
        UNSAFE.putDouble(mem, ABO + addr * AIS, value);
    }

    @Override
    public synchronized int cmpXchgInt(long addr, int expected, int desired) {
        boolean succ = UNSAFE.compareAndSwapInt(mem, ABO + addr * AIS,
                expected, desired);
        return succ ? expected : loadInt(addr);
    }

    @Override
    public synchronized long cmpXchgLong(long addr, long expected, long desired) {
        boolean succ = UNSAFE.compareAndSwapLong(mem, ABO + addr * AIS,
                expected, desired);
        return succ ? expected : loadLong(addr);
    }

    @Override
    public synchronized int fetchXchgInt(long addr, int opnd) {
        int oldVal = loadInt(addr);
        int newVal = opnd;
        storeInt(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized int fetchAddInt(long addr, int opnd) {
        int oldVal = loadInt(addr);
        int newVal = oldVal + opnd;
        storeInt(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized int fetchSubInt(long addr, int opnd) {
        int oldVal = loadInt(addr);
        int newVal = oldVal - opnd;
        storeInt(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized int fetchAndInt(long addr, int opnd) {
        int oldVal = loadInt(addr);
        int newVal = oldVal & opnd;
        storeInt(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized int fetchNandInt(long addr, int opnd) {
        int oldVal = loadInt(addr);
        int newVal = ~(oldVal & opnd);
        storeInt(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized int fetchOrInt(long addr, int opnd) {
        int oldVal = loadInt(addr);
        int newVal = oldVal | opnd;
        storeInt(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized int fetchXorInt(long addr, int opnd) {
        int oldVal = loadInt(addr);
        int newVal = oldVal ^ opnd;
        storeInt(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized int fetchMaxInt(long addr, int opnd) {
        int oldVal = loadInt(addr);
        int newVal = Math.max(oldVal, opnd);
        storeInt(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized int fetchMinInt(long addr, int opnd) {
        int oldVal = loadInt(addr);
        int newVal = Math.min(oldVal, opnd);
        storeInt(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized int fetchUmaxInt(long addr, int opnd) {
        int oldVal = loadInt(addr);
        int newVal = Math.max(oldVal + 0x80000000, opnd + 0x80000000) - 0x80000000;
        storeInt(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized int fetchUminInt(long addr, int opnd) {
        int oldVal = loadInt(addr);
        int newVal = Math.min(oldVal + 0x80000000, opnd + 0x80000000) - 0x80000000;
        storeInt(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized long fetchXchgLong(long addr, long opnd) {
        long oldVal = loadLong(addr);
        long newVal = opnd;
        storeLong(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized long fetchAddLong(long addr, long opnd) {
        long oldVal = loadLong(addr);
        long newVal = oldVal + opnd;
        storeLong(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized long fetchSubLong(long addr, long opnd) {
        long oldVal = loadLong(addr);
        long newVal = oldVal - opnd;
        storeLong(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized long fetchAndLong(long addr, long opnd) {
        long oldVal = loadLong(addr);
        long newVal = oldVal & opnd;
        storeLong(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized long fetchNandLong(long addr, long opnd) {
        long oldVal = loadLong(addr);
        long newVal = ~(oldVal & opnd);
        storeLong(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized long fetchOrLong(long addr, long opnd) {
        long oldVal = loadLong(addr);
        long newVal = oldVal | opnd;
        storeLong(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized long fetchXorLong(long addr, long opnd) {
        long oldVal = loadLong(addr);
        long newVal = oldVal ^ opnd;
        storeLong(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized long fetchMaxLong(long addr, long opnd) {
        long oldVal = loadLong(addr);
        long newVal = Math.max(oldVal, opnd);
        storeLong(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized long fetchMinLong(long addr, long opnd) {
        long oldVal = loadLong(addr);
        long newVal = Math.min(oldVal, opnd);
        storeLong(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized long fetchUmaxLong(long addr, long opnd) {
        long oldVal = loadLong(addr);
        long newVal = Math.max(oldVal + 0x8000000000000000L,
                opnd + 0x8000000000000000L) - 0x8000000000000000L;
        storeLong(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized long fetchUminLong(long addr, long opnd) {
        long oldVal = loadLong(addr);
        long newVal = Math.min(oldVal + 0x8000000000000000L,
                opnd + 0x8000000000000000L) - 0x8000000000000000L;
        storeLong(addr, newVal);
        return oldVal;
    }

    @Override
    public synchronized void fence() {
        // TODO: Not sure how to implement this.
    }

}
