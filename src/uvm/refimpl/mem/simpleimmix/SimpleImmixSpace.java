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

    private SimpleImmixHeap heap;

    private int nBlocks;

    private int[] blockFlags; // for GC marking. maps block index -> flag

    // Freelist block allocator
    private int[] freeList; // a list of all indices of free blocks
    private int freeListValidCount; // number of valid items in freeList
    private int nextFree; // index into freeList, the next block to get

    // Defrag
    private long[] blockUsedStats; // statistics for GC to defrag
    private int nReserved; // number of reserved blocks for defrag
    private int[] defragResv; // A list of reserved blocks for defrag
    private int defragResvFree; // number of free reserved blocks for defrag
    private int nextResv; // index into defragResv, the next reserved block

    // We don't have line, but line-sized buckets are used to estimate the
    // defrag candidate threshold.
    private static final long LINE_SIZE = 128;
    private static final int N_BUCKETS = 256;
    private long[] buckets = new long[N_BUCKETS];

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
        blockUsedStats = new long[nBlocks];

        nReserved = nBlocks / 20; // Reserve 5% of blocks for defrag. may be 0
        defragResv = new int[nReserved];

        for (int i = 0; i < nReserved; i++) {
            defragResv[i] = i;
            reserve(i);
        }
        defragResvFree = nReserved;
        nextResv = 0;

        for (int i = nReserved; i < nBlocks; i++) {
            freeList[i - nReserved] = i;
        }
        freeListValidCount = nBlocks - nReserved;
        nextFree = 0;

    }

    /**
     * Get a new block. Automatically reserve that block.
     * 
     * @return A block address, or 0 if no blocks are available.
     */
    public long tryGetBlock(long oldBlockAddr) {
        if (oldBlockAddr != 0) {
            returnBlock(oldBlockAddr);
        }

        int myCursor = nextFree;

        if (myCursor >= freeListValidCount) {
            return 0L;
        }

        nextFree++;

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
        // Shift defrag reserved blocks to the beginning;
        for (int i = nextResv; i < defragResvFree; i++) {
            defragResv[i - nextResv] = defragResv[i];
        }

        int newDefragResvFree = defragResvFree - nextResv;
        int newNFree = 0;
        for (int i = 0; i < nBlocks; i++) {
            int flag = blockFlags[i];
            int bits = (flag & (BLOCK_MARKED | BLOCK_RESERVED));
            if (bits == 0) {
                if (newDefragResvFree < nReserved) {
                    defragResv[newDefragResvFree] = i;
                    newDefragResvFree++;
                    flag |= BLOCK_RESERVED;
                } else {
                    freeList[newNFree] = i;
                    newNFree++;
                    flag &= ~BLOCK_RESERVED;
                }
            } else {
                logger.format("Block %d is not freed because flag bits is %x",
                        i, bits);
            }
            flag &= ~BLOCK_MARKED;
            blockFlags[i] = flag;
        }
        defragResvFree = newDefragResvFree;
        freeListValidCount = newNFree;

        if (logger.isEnabled()) {
            StringBuilder sb1 = new StringBuilder("New reserved freelist:");
            for (int i = 0; i < defragResvFree; i++) {
                sb1.append(" ").append(defragResv[i]);
            }
            logger.format("%s", sb1.toString());
            StringBuilder sb2 = new StringBuilder("New freelist:");
            for (int i = 0; i < freeListValidCount; i++) {
                sb2.append(" ").append(freeList[i]);
            }
            logger.format("%s", sb2.toString());
            for (int i = 0; i < nBlocks; i++) {
                logger.format("blockFlags[%d] = %d", i, blockFlags[i]);
            }
        }

        nextResv = 0;
        nextFree = 0;

        return newNFree > 0;
    }

    public void returnBlock(long blockAddr) {
        int blockNum = blockAddrToBlockIndex(blockAddr);
        unreserve(blockNum);
    }

    // Statistics

    public void clearStats() {
        for (int i = 0; i < nBlocks; i++) {
            blockUsedStats[i] = 0L;
        }
    }

    public void incStat(int blockNum, long size) {
        blockUsedStats[blockNum] += size;
    }

    public long getStat(int pageNum) {
        return blockUsedStats[pageNum];
    }

    public long getTotalReserveSpace() {
        return defragResvFree * BLOCK_SIZE;
    }

    /**
     * Blocks whose used bytes larger than the returned threshold are subject to
     * defrag.
     */
    public long findThreshold(long avail) {
        if (logger.isEnabled()) {
            logger.format("Finding threshold. avail = %d", avail);
            for (int i = 0; i < nBlocks; i++) {
                logger.format("blockUsedStats[%d] = %d", i, blockUsedStats[i]);
            }
        }

        for (int i = 0; i < N_BUCKETS; i++) {
            buckets[i] = 0;
        }

        for (int i = 0; i < nBlocks; i++) {
            if ((blockFlags[i] & BLOCK_MARKED) != 0) {
                long used = blockUsedStats[i];
                int bucket = (int) (used / LINE_SIZE);
                buckets[bucket] += used;
            }
        }

        if (logger.isEnabled()) {
            long accum = 0;
            for (int i = 0; i < nBlocks; i++) {
                accum += buckets[i];
                logger.format("buckets[%d] = %d, accum: %d", i, buckets[i],
                        accum);
            }
        }

        long curUsed = 0;
        int curBucket = 0;
        while (curBucket < N_BUCKETS) {
            curUsed += buckets[curBucket];
            if (curUsed > avail) {
                break;
            }
            curBucket += 1;
        }

        long threshold = (long) curBucket * LINE_SIZE;

        if (logger.isEnabled()) {
            logger.format("threshold = %d", threshold);
        }

        return threshold;
    }

    // Defrag
    public long getDefragBlock(long oldBlockAddr) {
        if (oldBlockAddr != 0) {
            returnBlock(oldBlockAddr);
        }

        int myCursor = nextResv;

        if (myCursor >= defragResvFree) {
            return 0L;
        }

        nextResv++;

        int blockNum = defragResv[myCursor];

        long blockAddr = blockIndexToBlockAddr(blockNum);
        MemUtils.zeroRegion(blockAddr, BLOCK_SIZE);

        return blockAddr;
    }

}
