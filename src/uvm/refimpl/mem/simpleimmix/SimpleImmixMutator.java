package uvm.refimpl.mem.simpleimmix;

import uvm.refimpl.mem.TypeSizes;
import uvm.util.ErrorUtils;

public class SimpleImmixMutator {

    public long curBlockAddr;
    public long cursor;
    public long limit;
    private SimpleImmixHeap heap;
    private SimpleImmixSpace space;

    public SimpleImmixMutator(SimpleImmixHeap simpleImmixHeap,
            SimpleImmixSpace simpleImmixSpace) {
        this.heap = simpleImmixHeap;
        this.space = simpleImmixSpace;
        getNewBlock();
    }

    private void getNewBlock() {
        curBlockAddr = space.getBlock();
        cursor = curBlockAddr;
        limit = curBlockAddr + SimpleImmixSpace.BLOCK_SIZE;
    }

    public boolean getGlobalPauseFlag() {
        return heap.getGlobalPauseFlag();
    }

    public long alloc(long size, long align, long tag) {
        align = align < SimpleImmixHeap.GC_HEADER_SIZE_BYTES ? SimpleImmixHeap.GC_HEADER_SIZE_BYTES
                : align;

        while (true) {
            long gcStart = TypeSizes.alignUp(cursor, align);
            long userStart = TypeSizes.alignUp(gcStart
                    + SimpleImmixHeap.GC_HEADER_SIZE_BYTES, align);
            long userEnd = userStart + size;
            if (userEnd >= limit) {
                if (userEnd - gcStart > SimpleImmixSpace.BLOCK_SIZE) {
                    ErrorUtils.uvmError("Object too big: "
                            + (userEnd - gcStart));
                }
                getNewBlock();
                continue;
            } else {
                cursor = userEnd;
                heap.memorySupport.storeLongAtomic(userStart
                        - SimpleImmixHeap.GC_HEADER_SIZE_BYTES, tag);

                return userStart;
            }
        }
    }
}
