package uvm.refimpl.itpr;

import uvm.Function;

public interface UndefinedFunctionHanlder {
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
