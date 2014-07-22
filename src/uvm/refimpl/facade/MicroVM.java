package uvm.refimpl.facade;

import java.util.List;

import uvm.Bundle;
import uvm.Function;
import uvm.GlobalData;
import uvm.Namespace;
import uvm.refimpl.itpr.ConstantPool;
import uvm.refimpl.itpr.InterpreterFrame;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.MicroVMInternalTypes;
import uvm.refimpl.itpr.ThreadStackManager;
import uvm.refimpl.itpr.TrapManager;
import uvm.refimpl.itpr.ValueBox;
import uvm.refimpl.mem.MemoryManager;
import uvm.refimpl.mem.scanning.ObjectMarker;
import uvm.ssavalue.Constant;
import uvm.ssavalue.GlobalDataConstant;
import uvm.ssavalue.Parameter;
import uvm.type.Type;

public class MicroVM {
    public static final long DEFAULT_HEAP_SIZE = 4 * 1024 * 1024; // 4MiB
    public static final long DEFAULT_GLOBAL_SIZE = 1 * 1024 * 1024; // 1MiB
    public static final long DEFAULT_STACK_SIZE = 63 * 1024L; // 60KiB per stack

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
        globalBundle = new Bundle();
        constantPool = new ConstantPool(this);
        threadStackManager = new ThreadStackManager(this);
        memoryManager = new MemoryManager(heapSize, globalSize, stackSize, this);
        trapManager = new TrapManager(this);

        Namespace<Type> typeNs = globalBundle.getTypeNs();
        typeNs.put(MicroVMInternalTypes.VOID_TYPE.getID(),
                MicroVMInternalTypes.VOID_TYPE.getName(),
                MicroVMInternalTypes.VOID_TYPE);
        typeNs.put(MicroVMInternalTypes.BYTE_TYPE.getID(),
                MicroVMInternalTypes.BYTE_TYPE.getName(),
                MicroVMInternalTypes.BYTE_TYPE);
        typeNs.put(MicroVMInternalTypes.BYTE_ARRAY_TYPE.getID(),
                MicroVMInternalTypes.BYTE_ARRAY_TYPE.getName(),
                MicroVMInternalTypes.BYTE_ARRAY_TYPE);
    }

    /**
     * Add things from a bundle to the Micro VM.
     */
    public void addBundle(Bundle bundle) {
        globalBundle.mergeFrom(bundle);

        for (Constant constant : bundle.getGlobalValueNs().getObjects()) {
            if (constant instanceof GlobalDataConstant) {
                GlobalDataConstant gdc = (GlobalDataConstant) constant;
                GlobalData gd = gdc.getGlobalData();
                memoryManager.getGlobalMemory().addGlobalData(gd);
            }
            constantPool.addConstant(constant);
        }
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

    public void clientMark(ObjectMarker marker) {
        System.out.println(client);
        client.markExternalRoots(marker);
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
        System.out.println("Client set to: "+client);
        this.client = client;
    }

}
