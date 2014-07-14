package uvm.refimpl.facade;

import java.util.Collections;
import java.util.List;

import uvm.Bundle;
import uvm.Function;
import uvm.platformsupport.Config;
import uvm.platformsupport.MemorySupport;
import uvm.refimpl.itpr.ConstantPool;
import uvm.refimpl.itpr.InterpreterFrame;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.ThreadStackManager;
import uvm.refimpl.itpr.TrapManager;
import uvm.refimpl.itpr.ValueBox;
import uvm.refimpl.mem.MemoryManager;
import uvm.ssavalue.Constant;
import uvm.ssavalue.Parameter;

public class MicroVM {
    public static final long DEFAULT_HEAP_SIZE = 0x400000L; // 4MiB
    public static final long DEFAULT_GLOBAL_SIZE = 0x100000L; // 1MiB
    public static final long DEFAULT_STACK_SIZE = 0x1000L; // 4KiB per stack

    private MemorySupport memorySupport;

    private Bundle globalBundle;

    private ConstantPool constantPool;

    private ThreadStackManager threadStackManager;

    private MemoryManager memoryManager;

    private TrapManager trapManager;

    private MicroVMClient client;

    public MicroVM() {
        this(DEFAULT_HEAP_SIZE, DEFAULT_GLOBAL_SIZE, DEFAULT_STACK_SIZE);
    }

    /**
     * Create a new instance of Micro VM.
     */
    public MicroVM(long heapSize, long globalSize, long stackSize) {
        memorySupport = Config.MEMORY_SUPPORT;
        globalBundle = new Bundle();
        constantPool = new ConstantPool();
        threadStackManager = new ThreadStackManager(this);
        memoryManager = new MemoryManager(heapSize, globalSize, stackSize, this);
        memorySupport = Config.MEMORY_SUPPORT;
        trapManager = new TrapManager(this);
    }

    /**
     * Add things from a bundle to the Micro VM.
     */
    public void addBundle(Bundle bundle) {
        globalBundle.mergeFrom(bundle);

        for (Constant constant : bundle.getGlobalValueNs().getObjects()) {
            constantPool.addConstant(constant);
        }

        // TODO Add global memory items.
    }

    public InterpreterStack newStack(Function function, List<ValueBox> args) {
        InterpreterStack sta = threadStackManager.newStack(function);
        InterpreterFrame top = sta.getTop();
        List<Parameter> vParams = function.getCFG().getParams();
        for (int i = 0; i < vParams.size(); i++) {
            Parameter pv = vParams.get(i);
            ValueBox pb = top.getValueBox(pv);
            ValueBox ab = args.get(i);
            pb.copyValue(ab);
        }
        return sta;
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

    public MicroVMClient getClient() {
        return client;
    }

    public void setClient(MicroVMClient client) {
        this.client = client;
    }

    public List<Long> extraRoots() {
        if (client != null) {
            return client.extraRoots();
        } else {
            return Collections.emptyList();
        }
    }
}
