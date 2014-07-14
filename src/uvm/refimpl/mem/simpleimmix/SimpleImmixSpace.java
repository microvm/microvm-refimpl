package uvm.refimpl.mem.simpleimmix;

import static uvm.util.ErrorUtils.*;
import uvm.refimpl.mem.Heap;
import uvm.refimpl.mem.Space;

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

    public static final long BLOCK_SIZE = 32768;

    public static final int BLOCK_MARKED = 0x1;
    public static final int BLOCK_RESERVED = 0x2;

    public Heap heap;

    public int nBlocks;

    public int[] blockFlags; // for GC marking. maps block index -> flag
    
    public int[] freeList; // a list of all indices of free blocks
    public int freeListValidCount; // number of valid items in freeList
    public int nextFree; // index into freeList, the next page to get

    public SimpleImmixSpace(Heap heap, String name, long begin, long extend) {
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
     * Page index to address. No side-effect.
     */
    public long getBlockAddr(int pageNum) {
        return begin + BLOCK_SIZE * (long) pageNum;
    }

    /**
     * Get a new block. Automatically reserve that block.
     * 
     * @return A block address, or 0 if no pages available.
     */
    public long tryGetBlock() {
        int myCursor = nextFree;
        nextFree++;

        if (myCursor >= freeListValidCount) {
            return 0L;
        }

        int blockNum = freeList[myCursor];
        reserve(blockNum);

        long blockAddr = getBlockAddr(blockNum);

        return blockAddr;
    }

    private void reserve(int pageNum) {
        blockFlags[pageNum] |= BLOCK_RESERVED;
    }

    public int objRefToBlockIndex(long objRef) {
        long blockBegin = objRef & ~(BLOCK_SIZE - 1);
        /*
         * NOTE: My SimpleImmixMutator refuses to fill up a block to exactly its
         * upper-bound, in which case if the last object is a "void", its header
         * will occupy the last word in the block, but the the objRef appears to
         * be the beginning of the next block. This has plagued Rifat, but I
         * cheated by avoiding the problem in the allocator.
         */

        long blockIndex = (blockBegin - begin) / BLOCK_SIZE;

        return (int) blockIndex;
    }

    public void markBlockByIndex(int index) {
        blockFlags[index] |= BLOCK_MARKED;
    }

    public void markBlockByObjRef(long objRef) {
        int blockIndex = objRefToBlockIndex(objRef);
        markBlockByIndex(blockIndex);
    }
}
