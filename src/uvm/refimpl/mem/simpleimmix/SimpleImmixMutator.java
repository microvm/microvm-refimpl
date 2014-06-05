package uvm.refimpl.mem.simpleimmix;

public class SimpleImmixMutator {
    
    public long curBlockAddr;
    public long curBlockFree;

    public SimpleImmixMutator(SimpleImmixSpace simpleImmixSpace) {
        curBlockAddr = simpleImmixSpace.getBlock();
        curBlockFree = SimpleImmixSpace.BLOCK_SIZE;
    }

}
