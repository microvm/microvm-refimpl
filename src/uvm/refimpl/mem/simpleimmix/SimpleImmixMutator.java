package uvm.refimpl.mem.simpleimmix;

import uvm.refimpl.mem.Allocator;
import uvm.refimpl.mem.MemConstants;
import uvm.refimpl.mem.Mutator;
import uvm.refimpl.mem.TypeSizes;

public class SimpleImmixMutator extends Mutator implements Allocator {

    public long curBlockAddr;
    public long cursor;
    public long limit;
    private SimpleImmixHeap heap;

    public SimpleImmixMutator(SimpleImmixHeap simpleImmixHeap,
            SimpleImmixSpace simpleImmixSpace) {
        this.heap = simpleImmixHeap;
        this.curBlockAddr = 0L;
        getNewBlock();
    }

    private void getNewBlock() {
        curBlockAddr = heap.getBlock(curBlockAddr);
        cursor = curBlockAddr;
        limit = curBlockAddr + SimpleImmixSpace.BLOCK_SIZE;
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
                    return heap.allocLargeObject(size, align, headerSize);
                }
                System.out.println("Getting new block...");
                getNewBlock();
                System.out.println("got new block.");
                continue;
            } else {
                cursor = userEnd;

                return userStart;
            }
        }
    }

    public void close() {
        heap.returnBlock(curBlockAddr);
    }

}
