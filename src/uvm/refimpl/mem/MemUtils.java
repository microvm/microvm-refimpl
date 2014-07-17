package uvm.refimpl.mem;

import static uvm.platformsupport.Config.MEMORY_SUPPORT;

public class MemUtils {
    public static void zeroRegion(long start, long length) {
        long end = start + length;
        for (long a = start; a < end; a++) {
            MEMORY_SUPPORT.storeLong(a, 0);
        }
    }
}
