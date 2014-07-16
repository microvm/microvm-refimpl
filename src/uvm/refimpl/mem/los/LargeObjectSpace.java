package uvm.refimpl.mem.los;

import uvm.refimpl.mem.Space;
import uvm.refimpl.mem.simpleimmix.SimpleImmixSpace;

public class LargeObjectSpace extends Space {
    public static final long BLOCK_SIZE = SimpleImmixSpace.BLOCK_SIZE;

    public LargeObjectSpace(String name, long begin, long extend) {
        super(name, begin, extend);
        // TODO Auto-generated constructor stub
    }
    
}
