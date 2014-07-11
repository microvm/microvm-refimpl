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

    public Heap heap;

    public int nBlocks;

    public boolean[] blockMarks;
    public int[] freeList;
    public int maxFree;
    public int pageCursor;

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
        blockMarks = new boolean[nBlocks];
        freeList = new int[nBlocks];
        for (int i = 0; i < nBlocks; i++) {
            freeList[i] = i;
        }
        maxFree = nBlocks;
        pageCursor = 0;

    }

    /**
     * Page index to address. No side-effect.
     */
    public long getBlockAddr(int pageNum) {
        return begin + BLOCK_SIZE * (long) pageNum;
    }

    /**
     * Get a new block. Not thread safe. Must synchronise on heap.lock.
     * 
     * @return A block address, or 0 if no pages available.
     */
    public long tryGetBlock() {
        int myCursor = pageCursor;
        pageCursor++;

        if (myCursor >= maxFree) {
            return 0L;
        }

        int pageNum = freeList[myCursor];

        long blockAddr = getBlockAddr(pageNum);

        return blockAddr;
    }
}
