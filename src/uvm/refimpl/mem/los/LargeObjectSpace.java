package uvm.refimpl.mem.los;

import static uvm.platformsupport.Config.*;
import uvm.refimpl.mem.MemUtils;
import uvm.refimpl.mem.Space;
import uvm.refimpl.mem.TypeSizes;
import uvm.refimpl.mem.simpleimmix.SimpleImmixHeap;
import uvm.refimpl.mem.simpleimmix.SimpleImmixSpace;
import uvm.util.ErrorUtils;
import uvm.util.LogUtil;
import uvm.util.Logger;

/**
 * A mark-sweep freelist-based space to allocate large objects. An object always
 * occupies contiguous blocks and a block is used by at most one object at any
 * moment.
 * <p>
 * It has an extra header of three words before the GC header. The two words
 * form a doubly linked list of all live objects in the LOS. When sweeping, the
 * linked list is traversed to unlink all un-marked objects.
 * <p>
 * The third header is a mark bit for the block. It is used when traversing the
 * doubly linked list because the offset of the GC header relative to the block
 * start is not known (hybrids have one extra word of offset).
 * <p>
 * Objects in this space are never moved.
 */
public class LargeObjectSpace extends Space {
    private static final Logger logger = LogUtil.getLogger("LOS");
    public static final long BLOCK_SIZE = SimpleImmixSpace.BLOCK_SIZE / 4;

    private static final long OFFSET_PREV = 0;

    private static final long OFFSET_NEXT = 8;
    private static final long OFFSET_MARK = 16;

    private static final long MARK_BIT = 0x1;

    private SimpleImmixHeap heap;
    private FreeList freeList;

    private long head; // Head of the linked list of all live objects. 0 if
                       // there is no live object.

    public LargeObjectSpace(SimpleImmixHeap heap, String name, long begin,
            long extend) {
        super(name, begin, extend);
        ErrorUtils.uvmAssert(extend % BLOCK_SIZE == 0, String.format(
                "extend %d should be a multiple of BLOCK_SIZE %d", extend,
                BLOCK_SIZE));

        this.heap = heap;
        freeList = new FreeList((int) (extend / BLOCK_SIZE));
        head = 0;
    }

    public long alloc(long size, long align, long headerSize) {
        long userStart = TypeSizes.alignUp(16 + headerSize, align);
        long totalSize = userStart + size;

        long nBlocks = (totalSize - 1) / BLOCK_SIZE + 1;

        if (nBlocks > 0xffffffffL) {
            ErrorUtils.uvmError("Object too large: " + totalSize);
            return 0; // unreachable
        }

        int iBlocks = (int) nBlocks;
        int blockIndex = -1;
        for (int tries = 0; tries < 2; tries++) {
            blockIndex = freeList.allocate(iBlocks);
            if (blockIndex == -1 && tries == 0) {
                heap.mutatorTriggerAndWaitForGCEnd(true);
            } else {
                break;
            }
        }

        if (blockIndex == -1) {
            ErrorUtils
                    .uvmError("Out of memory when allocating large object of size: "
                            + totalSize);
            return 0; // unreachable
        }

        long blockAddr = blockIndexToBlockAddr(blockIndex);
        long regionSize = nBlocks * BLOCK_SIZE;
        MemUtils.zeroRegion(blockAddr, regionSize);

        link(blockAddr);

        long objRef = blockAddr + userStart;
        return objRef;
    }

    public void markBlockByObjRef(long objRef) {
        long blockAddr = objRefToBlockAddr(objRef);
        logger.format("marking block addr %d for obj %d...", blockAddr, objRef);
        markBlock(blockAddr);
    }

    public boolean collect() {
        logger.format("Start collecting...");

        if (head == 0) {
            logger.format("not iterating because head == 0");
            return false;
        }

        boolean anyDeallocated = false;
        long curBlock = head;
        long lastBlock = getPrev(curBlock);
        long nextBlock = getNext(curBlock);

        logger.format("Begin iteration from %d to %d", curBlock, lastBlock);

        while (true) {
            logger.format("Visiting block %d..", curBlock);
            long mark = getBlockMark(curBlock);
            if (mark != MARK_BIT) {
                logger.format("Deallocating block addr %d...", curBlock);
                dealloc(curBlock);
                anyDeallocated = true;
            } else {
                logger.format("Block addr %d contains live object.", curBlock);
                unmarkBlock(curBlock);
            }

            if (curBlock == lastBlock) {
                break;
            } else {
                curBlock = nextBlock;
                nextBlock = getNext(curBlock);
            }
        }
        return anyDeallocated;

    }

    private void dealloc(long blockAddr) {
        int blockIndex = blockAddrToBlockIndex(blockAddr);
        freeList.deallocate(blockIndex);
        unlink(blockAddr);
    }

    public int objRefToBlockIndex(long objRef) {
        long blockAddr = objRefToBlockAddr(objRef);
        /*
         * NOTE: My SimpleImmixMutator refuses to fill up a block to exactly its
         * upper-bound, in which case if the last object is a "void", its header
         * will occupy the last word in the block, but the the objRef appears to
         * be the beginning of the next block. This has plagued Rifat, but I
         * cheated by avoiding the problem in the allocator.
         */

        int blockIndex = blockAddrToBlockIndex(blockAddr);

        return blockIndex;
    }

    public long objRefToBlockAddr(long objRef) {
        return objRef & ~(BLOCK_SIZE - 1);
    }

    public long blockIndexToBlockAddr(int blockIndex) {
        return begin + BLOCK_SIZE * (long) blockIndex;
    }

    public int blockAddrToBlockIndex(long blockAddr) {
        return (int) ((blockAddr - begin) / BLOCK_SIZE);
    }

    private void markBlock(long blockAddr) {
        MEMORY_SUPPORT.storeLong(blockAddr + OFFSET_MARK, MARK_BIT);
    }

    private void unmarkBlock(long blockAddr) {
        MEMORY_SUPPORT.storeLong(blockAddr + OFFSET_MARK, 0);
    }

    private long getBlockMark(long blockAddr) {
        return MEMORY_SUPPORT.loadLong(blockAddr + OFFSET_MARK);
    }

    private void link(long blockAddr) {
        if (head == 0) {
            head = blockAddr;
            setPrev(blockAddr, blockAddr);
            setNext(blockAddr, blockAddr);
        } else {
            long last = getPrev(head);
            setPrev(blockAddr, last);
            setNext(blockAddr, head);
            setPrev(head, blockAddr);
            setNext(last, blockAddr);
        }
    }

    private void unlink(long blockAddr) {
        long next = getNext(blockAddr);
        if (next == blockAddr) {
            head = 0;
        } else {
            long prev = getPrev(blockAddr);
            setNext(prev, next);
            setPrev(next, prev);
            head = next;
        }
    }

    private long getPrev(long blockAddr) {
        return MEMORY_SUPPORT.loadLong(blockAddr + OFFSET_PREV);
    }

    private long getNext(long blockAddr) {
        return MEMORY_SUPPORT.loadLong(blockAddr + OFFSET_NEXT);
    }

    private void setPrev(long blockAddr, long toBlock) {
        MEMORY_SUPPORT.storeLong(blockAddr + OFFSET_PREV, toBlock);
    }

    private void setNext(long blockAddr, long toBlock) {
        MEMORY_SUPPORT.storeLong(blockAddr + OFFSET_NEXT, toBlock);
    }

}
