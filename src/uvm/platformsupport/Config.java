package uvm.platformsupport;

import uvm.platformsupport.ordinaryjava.UnsafeMemorySupport;

public class Config {
    public static final MemorySupport MEMORY_SUPPORT = new UnsafeMemorySupport();
}
