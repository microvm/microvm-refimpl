package uvm.refimpl.mem.simpleimmix;

import static uvm.util.ErrorUtils.uvmError;
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

    public SimpleImmixHeap heap;

    public long nBlocks;

    public boolean[] blockMarks;
    public long[] freeList;
    public long nFree;
    public long pageCursor;

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
        nBlocks = extend / BLOCK_SIZE;
        blockMarks = new boolean[(int) nBlocks];
        freeList = new long[(int) nBlocks];
        nFree = nBlocks;
        pageCursor = 0L;

    }

    public long getBlockAddr(long pageNum) {
        return begin + BLOCK_SIZE * pageNum;
    }

    public long getBlock() {
        while (true) {
            // Try to get a block, may fail.
            long myBlock = tryGetBlock();

            // If successful, return.
            if (myBlock != 0) {
                return myBlock;
            }

            // Otherwise trigger the GC and wait for the GC to complete.
            heap.triggerAndWaitForGC();
        }
    }

    private long tryGetBlock() {
        long myCursor;

        // Does not really need lock. An atomic getAndInc will suffice.
        heap.lock.lock();
        try {
            myCursor = pageCursor;
            pageCursor++;
        } finally {
            heap.lock.unlock();
        }

        if (myCursor >= nFree) {
            return -1;
        }

        long pageNum = freeList[(int) myCursor];

        long blockAddr = getBlockAddr(pageNum);

        return blockAddr;
    }

}
