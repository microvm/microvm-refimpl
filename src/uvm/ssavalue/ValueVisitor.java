package uvm.ssavalue;


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

    T visitFPConstant(FPConstant fpConstant);

    T visitParameter(Parameter parameter);

    T visitBinOp(InstBinOp instBinOp);

    T visitCmp(InstCmp instCmp);

    T visitBranch(InstBranch instBranch);

    T visitBranch2(InstBranch2 instBranch2);

    T visitPhi(InstPhi instPhi);

    T visitRet(InstRet instRet);

    T visitRetVoid(InstRetVoid instRetVoid);

    T visitStructConstant(StructConstant structConstant);

    T visitNullConstant(NullConstant nullConstant);

}
