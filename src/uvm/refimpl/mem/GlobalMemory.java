package uvm.refimpl.mem;

import java.util.HashMap;
import java.util.Map;

import uvm.GlobalData;
import uvm.IdentifiedHelper;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.mem.bumppointer.RewindableBumpPointerAllocator;
import uvm.type.Hybrid;
import uvm.type.Type;
import uvm.util.ErrorUtils;

public class GlobalMemory extends Space {

    private RewindableBumpPointerAllocator allocator;
    private Map<GlobalData, Long> locationMap = new HashMap<GlobalData, Long>();

    public GlobalMemory(long begin, long size, MicroVM microVM) {
        super("GlobalSpace", begin, size);

        allocator = new RewindableBumpPointerAllocator(begin, size, microVM);
    }

    public RewindableBumpPointerAllocator getAllocator() {
        return allocator;
    }

    public void addGlobalData(GlobalData globalData) {
        Type ty = globalData.getType();
        if (ty instanceof Hybrid) {
            ErrorUtils
                    .uvmError("It does not make sense to make global hybrid (use array and any fixed types). global data: "
                            + IdentifiedHelper.repr(globalData));
        }

        long addr = makeGlobalScalar(ty);
        locationMap.put(globalData, addr);
    }

    private long makeGlobalScalar(Type type) {
        long tag = type.getID();
        long size = TypeSizes.sizeOf(type);
        long align = TypeSizes.alignOf(type);
        long objAddr = allocator.alloc(size, align,
                TypeSizes.GC_HEADER_SIZE_SCALAR);
        HeaderUtils.postAllocScalar(objAddr, tag);

        return objAddr;
    }

    public long getGlobalDataIRef(GlobalData globalData) {
        return locationMap.get(globalData);
    }
}
