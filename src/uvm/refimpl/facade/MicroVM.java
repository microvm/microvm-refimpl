package uvm.refimpl.facade;

import uvm.Bundle;
import uvm.Function;
import uvm.platformsupport.Config;
import uvm.platformsupport.MemorySupport;
import uvm.platformsupport.ordinaryjava.UnsafeMemorySupport;
import uvm.refimpl.itpr.ConstantPool;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.ThreadStackManager;
import uvm.refimpl.itpr.TrapManager;
import uvm.refimpl.mem.MemoryManager;
import uvm.ssavalue.Constant;

public class MicroVM {
    public static final long HEAP_SIZE = 0x400000L; // 4MiB
    public static final long GLOBAL_SIZE = 0x100000L; // 1MiB
    public static final long STACK_SIZE = 0x1000L; // 4KiB per stack

    private MemorySupport memorySupport;

    private Bundle globalBundle;

    private ConstantPool constantPool;

    private ThreadStackManager threadStackManager;

    private MemoryManager memoryManager;

    private TrapManager trapManager;

    /**
     * Create a new instance of Micro VM.
     */
    public MicroVM() {
        memorySupport = new UnsafeMemorySupport();
        globalBundle = new Bundle();
        constantPool = new ConstantPool();
        threadStackManager = new ThreadStackManager(this);
        memoryManager = new MemoryManager(HEAP_SIZE, GLOBAL_SIZE, STACK_SIZE);
        memorySupport = Config.MEMORY_SUPPORT;
        trapManager = new TrapManager(this);
    }

    /**
     * Add things from a bundle to the Micro VM.
     */
    public void addBundle(Bundle bundle) {
        globalBundle.mergeFrom(bundle);
        
        for(Constant constant : bundle.getGlobalValueNs().getObjects()) {
            constantPool.addConstant(constant);
        }
        
        // TODO Add global memory items.
    }

    public InterpreterStack newStack(Function function) {
        return threadStackManager.newStack(function);
    }

    public InterpreterThread newThread(InterpreterStack stack) {
        return threadStackManager.newThread(stack);
    }

    public MemorySupport getMemorySupport() {
        return memorySupport;
    }

    // Getters
    public Bundle getGlobalBundle() {
        return globalBundle;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public ThreadStackManager getThreadStackManager() {
        return threadStackManager;
    }

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    public TrapManager getTrapManager() {
        return trapManager;
    }
}
