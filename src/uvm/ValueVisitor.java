package uvm;

import uvm.inst.InstBinOp;
import uvm.inst.InstBranch;
import uvm.inst.InstBranch2;
import uvm.inst.InstCmp;
import uvm.inst.InstPhi;
import uvm.inst.InstRet;
import uvm.inst.InstRetVoid;

/**
 * Visitor to a subclass of Value. Good for writing a polymorphic function over
 * the type Value.
 * <p>
 * p.s. I wish I were programming in Scala...
 * 
 * @param <T>
 *            The return type of each visitor function.
 */
public interface ValueVisitor<T> {
    T visitIntConstant(IntConstant intConstant);

    T visitParameter(Parameter parameter);

    T visitBinOp(InstBinOp instBinOp);

    T visitCmp(InstCmp instCmp);

    T visitBranch(InstBranch instBranch);

    T visitBranch2(InstBranch2 instBranch2);

    T visitPhi(InstPhi instPhi);

    T visitRet(InstRet instRet);

    T visitRetVoid(InstRetVoid instRetVoid);
}
