package uvm.refimpl.mem.simpleimmix;

import uvm.refimpl.mem.Allocator;
import uvm.refimpl.mem.MemConstants;
import uvm.refimpl.mem.Mutator;
import uvm.refimpl.mem.TypeSizes;
import uvm.refimpl.mem.los.LargeObjectSpace;
import uvm.util.LogUtil;
import uvm.util.Logger;

public class SimpleImmixMutator extends Mutator implements Allocator {
    private static final Logger logger = LogUtil.getLogger("SIM");

    private SimpleImmixHeap heap;
    private SimpleImmixSpace space;
    private LargeObjectSpace los;

    private long curBlockAddr;
    private long cursor;
    private long limit;

    public SimpleImmixMutator(SimpleImmixHeap simpleImmixHeap,
            SimpleImmixSpace simpleImmixSpace, LargeObjectSpace los) {
        this.heap = simpleImmixHeap;
        this.space = simpleImmixSpace;
        this.los = los;
        this.curBlockAddr = 0L;
        getNewBlock();
    }

    private void getNewBlock() {
        long newAddr = 0;
        while (true) {
            newAddr = space.tryGetBlock(curBlockAddr);

            if (newAddr != 0L) {
                break;
            }

            heap.mutatorTriggerAndWaitForGCEnd(true);
        }

        curBlockAddr = newAddr;
        cursor = curBlockAddr;
        limit = curBlockAddr + SimpleImmixSpace.BLOCK_SIZE;
    }

    @Override
    public long alloc(long size, long align, long headerSize) {
        logger.format("alloc(%d, %d, %d)", size, align, headerSize);

        align = align < MemConstants.WORD_SIZE_BYTES ? MemConstants.WORD_SIZE_BYTES
                : align;

        while (true) {
            long gcStart = TypeSizes.alignUp(cursor, align);
            long userStart = TypeSizes.alignUp(gcStart + headerSize, align);
            long userEnd = userStart + size;
            if (userEnd >= limit) {
                if (userEnd - gcStart > SimpleImmixSpace.BLOCK_SIZE) {
                    return los.alloc(size, align, headerSize);
                }
                logger.format("%s", "Getting new block...");
                getNewBlock();
                logger.format("%s", "got new block.");
                continue;
            } else {
                cursor = userEnd;

                return userStart;
            }
        }
    }

    public void close() {
        space.returnBlock(curBlockAddr);
    }

}
