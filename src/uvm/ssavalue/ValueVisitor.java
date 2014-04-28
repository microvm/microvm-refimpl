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

    T visitStructConstant(StructConstant structConstant);

    T visitNullConstant(NullConstant nullConstant);

    T visitBinOp(InstBinOp instBinOp);

    T visitCmp(InstCmp instCmp);

    T visitConversion(InstConversion instConversion);

    T visitSelect(InstSelect instSelect);

    T visitBranch(InstBranch instBranch);

    T visitBranch2(InstBranch2 instBranch2);

    T visitSwitch(InstSwitch instSwitch);

    T visitPhi(InstPhi instPhi);

    T visitCall(InstCall call);

    T visitInvoke(InstInvoke instInvoke);

    T visitTailCall(InstTailCall tailCall);

    T visitRet(InstRet instRet);

    T visitRetVoid(InstRetVoid instRetVoid);

    T visitThrow(InstThrow instThrow);

    T visitLandingPad(InstLandingPad instLandingPad);

    T visitExtractValue(InstExtractValue instExtractValue);

    T visitInsertValue(InstInsertValue instInsertValue);

    T visitNew(InstNew instNew);

    T visitAlloca(InstAlloca instAlloca);

    T visitNewHybrid(InstNewHybrid instNewHybrid);

    T visitAllocaHybrid(InstAllocaHybrid instAllocaHybrid);

    T visitGetIRef(InstGetIRef instGetIRef);

    T visitGetFieldIRef(InstGetFieldIRef instGetFieldIRef);

    T visitGetElemIRef(InstGetElemIRef instGetElemIRef);

    T visitShiftIRef(InstShiftIRef instShiftIRef);

    T visitGetFixedPartIRef(InstGetFixedPartIRef instGetFixedPartIRef);

    T visitGetVarPartIRef(InstGetVarPartIRef instGetVarPartIRef);
}
