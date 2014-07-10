package uvm.refimpl.mem.simpleimmix;

import uvm.refimpl.mem.Heap;
import uvm.refimpl.mem.MemConstants;
import uvm.refimpl.mem.Mutator;
import uvm.refimpl.mem.TypeSizes;
import uvm.util.ErrorUtils;

public class SimpleImmixMutator extends Mutator {

    public long curBlockAddr;
    public long cursor;
    public long limit;
    private Heap heap;
    private SimpleImmixSpace space;

    public SimpleImmixMutator(Heap simpleImmixHeap,
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

    @Override
    public long alloc(long size, long align, long headerSize) {
        System.out.format("alloc(%d, %d, %d)\n", size, align, headerSize);
        align = align < MemConstants.WORD_SIZE_BYTES ? MemConstants.WORD_SIZE_BYTES
                : align;

        while (true) {
            long gcStart = TypeSizes.alignUp(cursor, align);
            long userStart = TypeSizes.alignUp(gcStart + headerSize, align);
            long userEnd = userStart + size;
            if (userEnd >= limit) {
                if (userEnd - gcStart > SimpleImmixSpace.BLOCK_SIZE) {
                    ErrorUtils.uvmError("Object too big: "
                            + (userEnd - gcStart));
                }
                System.out.println("Getting new block...");
                getNewBlock();
                continue;
            } else {
                cursor = userEnd;

                return userStart;
            }
        }
    }

}
