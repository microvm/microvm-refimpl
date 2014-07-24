package uvm.refimpl.mem;

import static uvm.platformsupport.Config.MEMORY_SUPPORT;
import static uvm.refimpl.mem.MemConstants.WORD_SIZE_BYTES;
import uvm.util.LogUtil;
import uvm.util.Logger;

public class MemUtils {
    private static final Logger logger = LogUtil.getLogger("MemUtils");

    public static void zeroRegion(long start, long length) {
        long end = start + length;
        logger.format("Zeroing [%d -> %d] %d bytes", start, end, length);
        for (long a = start; a < end; a += WORD_SIZE_BYTES) {
            MEMORY_SUPPORT.storeLong(a, 0);
        }
    }

    public static void memcpy(long src, long dst, long length) {
        logger.format("Copying [%d -> %d] %d bytes", src, dst, length);
        for (long a = 0; a < length; a += WORD_SIZE_BYTES) {
            long oldWord = MEMORY_SUPPORT.loadLong(src + a);
            MEMORY_SUPPORT.storeLong(dst + a, oldWord);
        }
    }
}
