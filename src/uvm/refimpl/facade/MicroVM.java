package uvm.refimpl.facade;

import uvm.Bundle;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.mem.MemoryManager;
import uvm.util.ErrorUtils;

public class MicroVM {
    public static final long HEAP_SIZE = 0x400000L; // 4MiB
    public static final long GLOBAL_SIZE = 0x100000L; // 1MiB
    public static final long STACK_SIZE = 0x1000L; // 4KiB per stack

    public MemoryManager mm;

    public MicroVM() {
        mm = new MemoryManager(HEAP_SIZE, GLOBAL_SIZE, STACK_SIZE);
    }

    public void addBundle(Bundle bundle) {
        ErrorUtils.uvmError("Not implemented");
    }

    public InterpreterStack newStack(int id) {
        ErrorUtils.uvmError("Not implemented");
        return null;
    }

    public InterpreterThread newThread(InterpreterStack stack) {
        ErrorUtils.uvmError("Not implemented");
        return null;
    }
}
