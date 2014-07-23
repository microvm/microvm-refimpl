package uvm.refimpl.mem.simpleimmix;

import static uvm.util.ErrorUtils.*;
import uvm.refimpl.mem.MemUtils;
import uvm.refimpl.mem.Space;
import uvm.util.LogUtil;
import uvm.util.Logger;

/**
 * A simple mark-region space.
 * <p>
 * The space is partitioned into blocks. Each mutator has one active block at a
 * time and allocates sequentially within the block. When a mutator exhausts a
 * block, it asks the space for another block. When there is no block available
 * globally, it will trigger a GC and try again. If the GC does not release any
 * memory, it is an out-of-memory case. The VM terminates in this case.
 */
public class SimpleImmixSpace extends Space {
    private static final Logger logger = LogUtil.getLogger("SIS");

    public static final long BLOCK_SIZE = 32768;

    public static final int BLOCK_MARKED = 0x1;
    public static final int BLOCK_RESERVED = 0x2;

    public SimpleImmixHeap heap;

    public int nBlocks;

    public int[] blockFlags; // for GC marking. maps block index -> flag

    public int[] freeList; // a list of all indices of free blocks
    public int freeListValidCount; // number of valid items in freeList
    public int nextFree; // index into freeList, the next block to get

    public SimpleImmixSpace(SimpleImmixHeap heap, String name, long begin,
            long extend) {
        super(name, begin, extend);
        this.heap = heap;

        if (begin % BLOCK_SIZE != 0) {
            uvmError("space should be aligned to BLOCK_SIZE " + BLOCK_SIZE);
        }
        if (extend % BLOCK_SIZE != 0) {
            uvmError("space size should be a multiple of BLOCK_SIZE "
                    + BLOCK_SIZE);
        }
        nBlocks = (int) (extend / BLOCK_SIZE);
        blockFlags = new int[nBlocks];
        freeList = new int[nBlocks];
        for (int i = 0; i < nBlocks; i++) {
            freeList[i] = i;
        }
        freeListValidCount = nBlocks;
        nextFree = 0;

    }

    /**
     * Get a new block. Automatically reserve that block.
     * 
     * @return A block address, or 0 if no blocks are available.
     */
    public long tryGetBlock(long oldBlockAddr) {
        int myCursor = nextFree;
        nextFree++;

        if (myCursor >= freeListValidCount) {
            return 0L;
        }

        if (oldBlockAddr != 0) {
            int oldBlockNum = blockAddrToBlockIndex(oldBlockAddr);
            unreserve(oldBlockNum);
        }

        int blockNum = freeList[myCursor];
        reserve(blockNum);

        long blockAddr = blockIndexToBlockAddr(blockNum);
        MemUtils.zeroRegion(blockAddr, BLOCK_SIZE);

        return blockAddr;
    }

    private void reserve(int blockNum) {
        blockFlags[blockNum] |= BLOCK_RESERVED;
    }

    private void unreserve(int blockNum) {
        blockFlags[blockNum] &= ~BLOCK_RESERVED;
    }

    public int objRefToBlockIndex(long objRef) {
        long blockAddr = objRef & ~(BLOCK_SIZE - 1);
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

    public long blockIndexToBlockAddr(int blockIndex) {
        return begin + BLOCK_SIZE * (long) blockIndex;
    }

    public int blockAddrToBlockIndex(long blockAddr) {
        return (int) ((blockAddr - begin) / BLOCK_SIZE);
    }

    public void markBlockByIndex(int index) {
        blockFlags[index] |= BLOCK_MARKED;
    }

    public void markBlockByObjRef(long objRef) {
        int blockIndex = objRefToBlockIndex(objRef);
        markBlockByIndex(blockIndex);
        logger.format("Marked block %d", blockIndex);
    }

    public boolean collectBlocks() {
        int newNFree = 0;
        for (int i = 0; i < nBlocks; i++) {
            int flag = blockFlags[i];
            int bits = (flag & (BLOCK_MARKED | BLOCK_RESERVED));
            if (bits == 0) {
                freeList[newNFree] = i;
                newNFree++;
            } else {
                logger.format("Block %d is not freed because flag bits is %x",
                        i, bits);
            }
            flag &= ~BLOCK_MARKED;
            blockFlags[i] = flag;
        }
        freeListValidCount = newNFree;

        if (logger.isEnabled()) {
            StringBuilder sb = new StringBuilder("New freelist:");
            for (int i = 0; i < freeListValidCount; i++) {
                sb.append(" ").append(freeList[i]);
            }
            logger.format("%s", sb.toString());
        }

        nextFree = 0;

        return newNFree > 0;
    }

    public void returnBlock(long blockAddr) {
        int blockNum = blockAddrToBlockIndex(blockAddr);
        unreserve(blockNum);
    }
}
