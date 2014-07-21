package uvm.refimpl.itpr;

public interface TrapHandler {
    /**
     * Handle a trap or watch-point event.
     * 
     * @param thread
     *            The thread which executed a TRAP or WATCHPOINT instruction.
     * @return null if return normally. Otherwise return a boxed Long containing
     *         the address of the exception.
     */
    Long onTrap(InterpreterThread thread, ValueBox trapValue);
}
