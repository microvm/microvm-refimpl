package uvm.refimpl.mem;

import static uvm.platformsupport.Config.*;
import uvm.util.LogUtil;
import uvm.util.Logger;

public class HeaderUtils {
    private static final Logger logger = LogUtil.getLogger("HeaderUtils");

    public static void postAllocScalar(long addr, long tag) {
        logger.format("Storing tag %d at addr %d", tag, addr
                + TypeSizes.GC_HEADER_OFFSET_TAG);
        MEMORY_SUPPORT.storeLong(addr + TypeSizes.GC_HEADER_OFFSET_TAG, tag);
    }

    public static void postAllocHybrid(long addr, long tag, long len) {
        postAllocScalar(addr, tag);
        MEMORY_SUPPORT.storeLong(addr
                + TypeSizes.GC_HEADER_OFFSET_HYBRID_LENGTH, len);
    }

}
