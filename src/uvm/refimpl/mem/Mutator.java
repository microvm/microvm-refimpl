package uvm.refimpl.mem;

import static uvm.platformsupport.Config.*;
import uvm.type.Hybrid;
import uvm.type.Type;

public abstract class Mutator {

    public Mutator() {
        super();
    }

    public abstract long alloc(long size, long align, long headerSize);

    public long newScalar(Type type) {
        long tag = type.getID();
        long size = TypeSizes.sizeOf(type);
        long align = TypeSizes.alignOf(type);
        long objAddr = alloc(size, align, TypeSizes.GC_HEADER_SIZE_SCALAR);
        HeaderUtils.postAllocScalar(objAddr, tag);

        return objAddr;
    }

    public long newHybrid(Hybrid type, long len) {
        long tag = type.getID();
        long size = TypeSizes.hybridSizeOf(type, len);
        long align = TypeSizes.hybridAlignOf(type, len);
        long objAddr = alloc(size, align, TypeSizes.GC_HEADER_SIZE_HYBRID);
        HeaderUtils.postAllocHybrid(objAddr, tag, len);

        return objAddr;
    }

    public long allocaScalar(StackMemory sm, Type type) {
        long tag = type.getID();
        long size = TypeSizes.sizeOf(type);
        long align = TypeSizes.alignOf(type);
        long objAddr = sm.alloc(size, align, TypeSizes.GC_HEADER_SIZE_SCALAR);
        HeaderUtils.postAllocScalar(objAddr, tag);

        return objAddr;
    }

    public long allocaHybrid(StackMemory sm, Hybrid type, long len) {
        long tag = type.getID();
        long size = TypeSizes.hybridSizeOf(type, len);
        long align = TypeSizes.hybridAlignOf(type, len);
        long objAddr = sm.alloc(size, align, TypeSizes.GC_HEADER_SIZE_HYBRID);
        HeaderUtils.postAllocHybrid(objAddr, tag, len);

        return objAddr;
    }

    public abstract void close();
}