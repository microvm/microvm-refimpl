package uvm.refimpl.itpr;

import java.util.HashMap;
import java.util.Map;

import uvm.Function;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.mem.Mutator;
import uvm.refimpl.mem.StackMemory;

public class ThreadStackManager {

    private MicroVM microVM;

    private Map<Integer, InterpreterStack> stackRegistry = new HashMap<Integer, InterpreterStack>();
    private Map<Integer, InterpreterThread> threadRegistry = new HashMap<Integer, InterpreterThread>();

    public ThreadStackManager(MicroVM microVM) {
        super();
        this.microVM = microVM;
    }

    public Map<Integer, InterpreterStack> getStackRegistry() {
        return this.stackRegistry;
    }

    public Map<Integer, InterpreterThread> getThreadRegistry() {
        return threadRegistry;
    }

    public InterpreterStack getStackByID(int id) {
        return stackRegistry.get(id);
    }

    public InterpreterThread getThreadByID(int id) {
        return threadRegistry.get(id);
    }

    private int nextStackID = 1;

    private int makeStackID() {
        return nextStackID++;
    }

    private int nextThreadID = 1;

    private int makeThreadID() {
        return nextThreadID++;
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
        InterpreterThread thr = new InterpreterThread(id, microVM, stack,
                mutator);
        threadRegistry.put(id, thr);
        thr.start();

        return thr;
    }

    public void joinAll() {
        boolean someRunning;
        do {
            someRunning = false;
            for (InterpreterThread thr2 : threadRegistry.values()) {
                thr2.step();
                someRunning = thr2.isRunning() || someRunning;
            }
        } while (someRunning);
    }

    public void joinThread(InterpreterThread thr) {
        while (thr.isRunning()) {
            for (InterpreterThread thr2 : threadRegistry.values()) {
                thr2.step();
            }
        }
    }

}
