package uvm.refimpl.mem.simpleimmix;

import static uvm.util.ErrorUtils.uvmError;
import uvm.refimpl.mem.Space;

public class SimpleImmixSpace extends Space {

    public static final long BLOCK_SIZE = 32768;

    public long nBlocks;

    public boolean[] blockMarks;
    public long[] freeList;
    public long nFree;
    public long pageCursor;

    public SimpleImmixSpace(String name, long begin, long extend) {
        super(name, begin, extend);
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
        
        // TODO: This is one point that can trigger GC or out-of-memory.
        //
        // while(true) {
        //     addr = atomicTryGetBlock();
        // 
        //     if (addr) {
        //         return addr;
        //     } else {
        //         stopMyMutator();
        //         iAmFirst = trySetGlobalPauseFlag()
        //         if (iAmFirst) {
        //             waitForMutators();
        //             doGC();
        // TODO: How to determine real out-of-memory case?
        //       Not being able to recycle any space is an option.
        //         } else {
        //             waitForGC();
        //         }
        //     }
        // }
        
        long pageNum = freeList[(int) pageCursor++];
        return getBlockAddr(pageNum);
    }

    public SimpleImmixMutator makeMutator() {
        return new SimpleImmixMutator(this);
    }

}
