package uvm.refimpl.mem;

import static uvm.platformsupport.Config.MEMORY_SUPPORT;
import static uvm.refimpl.mem.MemConstants.WORD_SIZE_BYTES;

public class MemUtils {
    public static void zeroRegion(long start, long length) {
        long end = start + length;
        System.out.format("Zeroing [%d -> %d] %d bytes\n", start, end, length);
        for (long a = start; a < end; a += WORD_SIZE_BYTES) {
            MEMORY_SUPPORT.storeLong(a, 0);
        }
    }
}
