package uvm.refimpl.mem.simpleimmix;

import static uvm.util.ErrorUtils.*;
import uvm.refimpl.mem.Allocator;
import uvm.refimpl.mem.MemConstants;
import uvm.refimpl.mem.Mutator;
import uvm.refimpl.mem.TypeSizes;
import uvm.util.LogUtil;
import uvm.util.Logger;

public class SimpleImmixDefragMutator extends Mutator implements Allocator {
    private static final Logger logger = LogUtil.getLogger("SIDM");

    private SimpleImmixHeap heap;
    private SimpleImmixSpace space;

    private long curBlockAddr; // may be 0 to indicate no more reserved block
    private long cursor;
    private long limit;

    public SimpleImmixDefragMutator(SimpleImmixHeap simpleImmixHeap,
            SimpleImmixSpace simpleImmixSpace) {
        this.heap = simpleImmixHeap;
        this.space = simpleImmixSpace;
        this.curBlockAddr = 0L;
        getNewBlock();
    }

    private void getNewBlock() {
        curBlockAddr = space.getDefragBlock(curBlockAddr);
        cursor = curBlockAddr;
        limit = curBlockAddr + SimpleImmixSpace.BLOCK_SIZE;
    }

    /**
     * Ordinary (non-defrag) mutator never returns 0, but out-of-memory error is
     * fatal. Defragment mutator returns 0 when there is no reserved block.
     */
    @Override
    public long alloc(long size, long align, long headerSize) {
        logger.format("alloc(%d, %d, %d)", size, align, headerSize);

        if (curBlockAddr == 0) {
            logger.format("No more reserved blocks. Cannot defragment.");
            return 0;
        }

        align = align < MemConstants.WORD_SIZE_BYTES ? MemConstants.WORD_SIZE_BYTES
                : align;

        while (true) {
            long gcStart = TypeSizes.alignUp(cursor, align);
            long userStart = TypeSizes.alignUp(gcStart + headerSize, align);
            long userEnd = userStart + size;
            if (userEnd >= limit) {
                if (userEnd - gcStart > SimpleImmixSpace.BLOCK_SIZE) {
                    uvmError("Defrag mutators should not be used to allocate large objects.");
                    return 0; // unreachable
                }
                logger.format("Getting new reserved block...");
                getNewBlock();
                logger.format("got new reserved block.");
                if (curBlockAddr == 0) {
                    logger.format("No more reserved blocks. Cannot defragment.");
                    return 0;
                }
                continue;
            } else {
                cursor = userEnd;

                return userStart;
            }
        }
    }

    public void close() {
        if (curBlockAddr != 0) {
            space.returnBlock(curBlockAddr);
        }
    }

}
