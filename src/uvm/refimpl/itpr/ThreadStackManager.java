package uvm.refimpl.itpr;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import uvm.Function;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.mem.Mutator;
import uvm.refimpl.mem.StackMemory;

public class ThreadStackManager {

    private MicroVM microVM;

    public ThreadStackManager(MicroVM microVM) {
        super();
        this.microVM = microVM;
    }

    private Map<Integer, InterpreterStack> stackRegistry = new HashMap<Integer, InterpreterStack>();
    private Map<Integer, InterpreterThread> threadRegistry = new HashMap<Integer, InterpreterThread>();

    public InterpreterStack getStackByID(int id) {
        return stackRegistry.get(id);
    }

    public InterpreterThread getThreadByID(int id) {
        return threadRegistry.get(id);
    }

    private AtomicInteger nextStackID = new AtomicInteger(1);

    private int makeStackID() {
        return nextStackID.getAndIncrement();
    }

    private AtomicInteger nextThreadID = new AtomicInteger(1);

    private int makeThreadID() {
        return nextThreadID.getAndIncrement();
    }

    public InterpreterStack newStack(Function function) {
        StackMemory stackMemory = microVM.getMemoryManager().makeStackMemory();
        int id = makeStackID();
        InterpreterStack sta = new InterpreterStack(id, stackMemory);
        InterpreterFrame fra = new InterpreterFrame(function, null);
        sta.setTop(fra);
        stackRegistry.put(id, sta);
        return sta;
    }

    public InterpreterThread newThread(InterpreterStack stack) {
        Mutator mutator = microVM.getMemoryManager().makeMutator();
        int id = makeThreadID();
        InterpreterThread thr = new InterpreterThread(id, microVM,
                stack, mutator);
        threadRegistry.put(id, thr);
        return thr;
    }
}
