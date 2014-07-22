package uvm.refimpl.facade;

import uvm.Function;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.ValueBox;
import uvm.refimpl.mem.scanning.ObjectMarker;

public interface MicroVMClient {
    void markExternalRoots(ObjectMarker marker);
    /**
     * Handle a trap or watch-point event.
     * 
     * @param thread
     *            The thread which executed a TRAP or WATCHPOINT instruction.
     * @return null if return normally. Otherwise return a boxed Long containing
     *         the address of the exception.
     */
    Long onTrap(InterpreterThread thread, ValueBox trapValue);
    /**
     * Handle an undefined function event. After handling, the thread will retry
     * the instruction involving the undefined function unless explicitly
     * stopped.
     * 
     * @param thread
     *            The thread which called an undefined function.
     * @param func
     *            The undefined function.
     */
    void onUndefinedFunction(InterpreterThread thread, Function func);
}
