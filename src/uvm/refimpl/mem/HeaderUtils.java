package uvm.refimpl.mem;

import static uvm.platformsupport.Config.*;
import uvm.refimpl.facade.MicroVM;
import uvm.type.Type;
import uvm.util.LogUtil;
import uvm.util.Logger;

public class HeaderUtils {
    private static final Logger logger = LogUtil.getLogger("HeaderUtils");

    public static void postAllocScalar(long addr, long tag) {
        setTag(addr, tag);
    }

    public static void postAllocHybrid(long addr, long tag, long len) {
        postAllocScalar(addr, tag);
        setVarLength(addr, len);
    }

    public static long getTag(long objRef) {
        return MEMORY_SUPPORT.loadLong(objRef + TypeSizes.GC_HEADER_OFFSET_TAG);
    }

    public static long getVarLength(long objRef) {
        return MEMORY_SUPPORT.loadLong(objRef
                + TypeSizes.GC_HEADER_OFFSET_HYBRID_LENGTH);
    }

    public static void setTag(long objRef, long tag) {
        logger.format("Storing tag %d at addr %d", tag, objRef
                + TypeSizes.GC_HEADER_OFFSET_TAG);
        MEMORY_SUPPORT.storeLong(objRef + TypeSizes.GC_HEADER_OFFSET_TAG, tag);
    }

    public static void setVarLength(long objRef, long len) {
        MEMORY_SUPPORT.storeLong(objRef
                + TypeSizes.GC_HEADER_OFFSET_HYBRID_LENGTH, len);
    }

    public static int getTypeID(long tag) {
        return (int) (tag & 0x00000000ffffffffL);
    }

    public static Type getType(MicroVM microVM, long tag) {
        int typeID = getTypeID(tag);
        return microVM.getGlobalBundle().getTypeNs().getByID(typeID);
    }

    public static long getForwardedDest(long oldHeader) {
        return oldHeader & 0x0000ffffffffffffL;
    }
}
