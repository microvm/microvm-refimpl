package uvm.refimpl.mem;

import static uvm.platformsupport.Config.*;

public class HeaderUtils {

    public static void postAllocScalar(long addr, long tag) {
        MEMORY_SUPPORT.storeLong(addr + TypeSizes.GC_HEADER_OFFSET_TAG, tag);
    }

    public static void postAllocHybrid(long addr, long tag, long len) {
        postAllocScalar(addr, tag);
        MEMORY_SUPPORT.storeLong(addr
                + TypeSizes.GC_HEADER_OFFSET_HYBRID_LENGTH, len);
    }

}
