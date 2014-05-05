package uvm.ssavalue;

import uvm.FunctionSignature;

/**
 * An instruction is "call-like" if it has a signature, a callee and a list of
 * arguments.
 */
public interface CallLike extends HasArgs {
    public abstract FunctionSignature getSig();

    public abstract void setSig(FunctionSignature sig);

    public abstract Value getFunc();

    public abstract void setFunc(Value func);
}