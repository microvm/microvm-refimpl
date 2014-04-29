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
    T visitIntConstant(IntConstant constant);

    T visitFPConstant(FPConstant constant);

    T visitStructConstant(StructConstant constant);

    T visitNullConstant(NullConstant constant);

    T visitParameter(Parameter parameter);

    T visitBinOp(InstBinOp inst);

    T visitCmp(InstCmp inst);

    T visitConversion(InstConversion inst);

    T visitSelect(InstSelect inst);

    T visitBranch(InstBranch inst);

    T visitBranch2(InstBranch2 inst);

    T visitSwitch(InstSwitch inst);

    T visitPhi(InstPhi inst);

    T visitCall(InstCall inst);

    T visitInvoke(InstInvoke inst);

    T visitTailCall(InstTailCall inst);

    T visitRet(InstRet inst);

    T visitRetVoid(InstRetVoid inst);

    T visitThrow(InstThrow inst);

    T visitLandingPad(InstLandingPad inst);

    T visitExtractValue(InstExtractValue inst);

    T visitInsertValue(InstInsertValue inst);

    T visitNew(InstNew inst);

    T visitNewHybrid(InstNewHybrid inst);

    T visitAlloca(InstAlloca inst);

    T visitAllocaHybrid(InstAllocaHybrid inst);

    T visitGetIRef(InstGetIRef inst);

    T visitGetFieldIRef(InstGetFieldIRef inst);

    T visitGetElemIRef(InstGetElemIRef inst);

    T visitShiftIRef(InstShiftIRef inst);

    T visitGetFixedPartIRef(InstGetFixedPartIRef inst);

    T visitGetVarPartIRef(InstGetVarPartIRef inst);

    T visitLoad(InstLoad inst);

    T visitStore(InstStore inst);

    T visitCmpXchg(InstCmpXchg inst);

    T visitAtomicRMW(InstAtomicRMW inst);

    T visitFence(InstFence inst);

    T acceptTrap(InstTrap inst);

    T acceptWatchPoint(InstWatchPoint inst);

    T acceptCCall(InstCCall inst);

    T visitNewStack(InstNewStack inst);

    T visitICall(InstICall inst);

    T visitIInvoke(InstIInvoke inst);
}
