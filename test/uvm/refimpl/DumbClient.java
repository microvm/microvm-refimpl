package uvm.refimpl;

import uvm.Function;
import uvm.refimpl.facade.MicroVMClient;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.ValueBox;
import uvm.refimpl.mem.scanning.ObjectMarker;

public class DumbClient implements MicroVMClient {

    @Override
    public void markExternalRoots(ObjectMarker marker) {
        throw new RuntimeException(
                "This test should not involve external roots.");
    }

    @Override
    public Long onTrap(InterpreterThread thread, ValueBox trapValue) {
        throw new RuntimeException("This test should not involve traps.");
    }

    @Override
    public void onUndefinedFunction(InterpreterThread thread, Function func) {
        throw new RuntimeException(
                "This test should not involve underined functions.");
    }

}
