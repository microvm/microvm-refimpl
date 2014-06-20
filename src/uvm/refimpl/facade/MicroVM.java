package uvm.refimpl.facade;

import uvm.Bundle;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.mem.simpleimmix.SimpleImmixHeap;
import uvm.util.ErrorUtils;

public class MicroVM {
    public static final long HEAP_SIZE = 0x400000L; // 4MiB

    public SimpleImmixHeap heap;

    public MicroVM() {
        heap = new SimpleImmixHeap(HEAP_SIZE);
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
