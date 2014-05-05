package parser;

import parser.uIRParser.AtomicordContext;
import parser.uIRParser.FuncSigContext;
import parser.uIRParser.InstAllocaContext;
import parser.uIRParser.InstAllocaHybridContext;
import parser.uIRParser.InstAtomicRMWContext;
import parser.uIRParser.InstBinOpContext;
import parser.uIRParser.InstBranch2Context;
import parser.uIRParser.InstBranchContext;
import parser.uIRParser.InstCCallContext;
import parser.uIRParser.InstCallContext;
import parser.uIRParser.InstCmpContext;
import parser.uIRParser.InstCmpXchgContext;
import parser.uIRParser.InstConversionContext;
import parser.uIRParser.InstExtractValueContext;
import parser.uIRParser.InstFenceContext;
import parser.uIRParser.InstGetElemIRefContext;
import parser.uIRParser.InstGetFieldIRefContext;
import parser.uIRParser.InstGetFixedPartIRefContext;
import parser.uIRParser.InstGetIRefContext;
import parser.uIRParser.InstGetVarPartIRefContext;
import parser.uIRParser.InstICallContext;
import parser.uIRParser.InstIInvokeContext;
import parser.uIRParser.InstInsertValueContext;
import parser.uIRParser.InstInvokeContext;
import parser.uIRParser.InstLandingPadContext;
import parser.uIRParser.InstLoadContext;
import parser.uIRParser.InstNewContext;
import parser.uIRParser.InstNewHybridContext;
import parser.uIRParser.InstNewStackContext;
import parser.uIRParser.InstParamContext;
import parser.uIRParser.InstPhiContext;
import parser.uIRParser.InstRetContext;
import parser.uIRParser.InstRetVoidContext;
import parser.uIRParser.InstSelectContext;
import parser.uIRParser.InstShiftIRefContext;
import parser.uIRParser.InstStoreContext;
import parser.uIRParser.InstSwitchContext;
import parser.uIRParser.InstTailCallContext;
import parser.uIRParser.InstThrowContext;
import parser.uIRParser.InstTrapContext;
import parser.uIRParser.InstWatchPointContext;
import parser.uIRParser.IntLiteralContext;
import parser.uIRParser.TypeContext;
import uvm.FunctionSignature;
import uvm.intrinsicfunc.IntrinsicFunctionFactory;
import uvm.ssavalue.AtomicOrdering;
import uvm.ssavalue.AtomicRMWOp;
import uvm.ssavalue.BinOptr;
import uvm.ssavalue.CallConv;
import uvm.ssavalue.CmpOptr;
import uvm.ssavalue.ConvOptr;
import uvm.ssavalue.InstAlloca;
import uvm.ssavalue.InstAllocaHybrid;
import uvm.ssavalue.InstAtomicRMW;
import uvm.ssavalue.InstBinOp;
import uvm.ssavalue.InstBranch;
import uvm.ssavalue.InstBranch2;
import uvm.ssavalue.InstCCall;
import uvm.ssavalue.InstCall;
import uvm.ssavalue.InstCmp;
import uvm.ssavalue.InstCmpXchg;
import uvm.ssavalue.InstConversion;
import uvm.ssavalue.InstExtractValue;
import uvm.ssavalue.InstFence;
import uvm.ssavalue.InstGetElemIRef;
import uvm.ssavalue.InstGetFieldIRef;
import uvm.ssavalue.InstGetFixedPartIRef;
import uvm.ssavalue.InstGetIRef;
import uvm.ssavalue.InstGetVarPartIRef;
import uvm.ssavalue.InstICall;
import uvm.ssavalue.InstIInvoke;
import uvm.ssavalue.InstInsertValue;
import uvm.ssavalue.InstInvoke;
import uvm.ssavalue.InstLandingPad;
import uvm.ssavalue.InstLoad;
import uvm.ssavalue.InstNew;
import uvm.ssavalue.InstNewHybrid;
import uvm.ssavalue.InstNewStack;
import uvm.ssavalue.InstPhi;
import uvm.ssavalue.InstRet;
import uvm.ssavalue.InstRetVoid;
import uvm.ssavalue.InstSelect;
import uvm.ssavalue.InstShiftIRef;
import uvm.ssavalue.InstStore;
import uvm.ssavalue.InstSwitch;
import uvm.ssavalue.InstTailCall;
import uvm.ssavalue.InstThrow;
import uvm.ssavalue.InstTrap;
import uvm.ssavalue.InstWatchPoint;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.Parameter;
import uvm.type.Array;
import uvm.type.Hybrid;
import uvm.type.Struct;
import uvm.type.Type;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Visit the inst nodes in the parsing tree, create Instruction instances, but
 * does not link to other basic blocks or values.
 */
public class ShallowInstructionMaker extends uIRBaseVisitor<Instruction> {
    FuncBuilder fb;

    public ShallowInstructionMaker(FuncBuilder fb) {
        this.fb = fb;
    }

    // Visitor methods.

    @Override
    public Parameter visitInstParam(InstParamContext ctx) {
        Parameter inst = new Parameter();
        inst.setParamIndex(intLitToInt(ctx.intLiteral()));
        return inst;
    }

    @Override
    public InstBinOp visitInstBinOp(InstBinOpContext ctx) {
        InstBinOp inst = new InstBinOp();
        inst.setOptr(BinOptr.valueOf(ctx.binops().getText()));
        inst.setType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstCmp visitInstCmp(InstCmpContext ctx) {
        InstCmp inst = new InstCmp();
        inst.setOptr(CmpOptr.valueOf(ctx.cmpops().getText()));
        inst.setOpndType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstConversion visitInstConversion(InstConversionContext ctx) {
        InstConversion inst = new InstConversion();
        inst.setOptr(ConvOptr.valueOf(ctx.convops().getText()));
        inst.setFromType(makeType(ctx.type(0)));
        inst.setToType(makeType(ctx.type(1)));
        return inst;
    }

    @Override
    public InstSelect visitInstSelect(InstSelectContext ctx) {
        InstSelect inst = new InstSelect();
        inst.setType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstBranch visitInstBranch(InstBranchContext ctx) {
        InstBranch inst = new InstBranch();
        return inst;
    }

    @Override
    public InstBranch2 visitInstBranch2(InstBranch2Context ctx) {
        InstBranch2 inst = new InstBranch2();
        return inst;
    }

    @Override
    public InstSwitch visitInstSwitch(InstSwitchContext ctx) {
        InstSwitch inst = new InstSwitch();
        inst.setOpndType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstPhi visitInstPhi(InstPhiContext ctx) {
        InstPhi inst = new InstPhi();
        inst.setType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstCall visitInstCall(InstCallContext ctx) {
        InstCall inst = new InstCall();
        inst.setSig(makeFuncSig(ctx.funcCallBody().funcSig()));
        return inst;
    }

    @Override
    public InstInvoke visitInstInvoke(InstInvokeContext ctx) {
        InstInvoke inst = new InstInvoke();
        inst.setSig(makeFuncSig(ctx.funcCallBody().funcSig()));
        return inst;
    }

    @Override
    public InstTailCall visitInstTailCall(InstTailCallContext ctx) {
        InstTailCall inst = new InstTailCall();
        inst.setSig(makeFuncSig(ctx.funcCallBody().funcSig()));
        return inst;
    }

    @Override
    public InstRet visitInstRet(InstRetContext ctx) {
        InstRet inst = new InstRet();
        inst.setRetType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstRetVoid visitInstRetVoid(InstRetVoidContext ctx) {
        InstRetVoid inst = new InstRetVoid();
        return inst;
    }

    @Override
    public InstThrow visitInstThrow(InstThrowContext ctx) {
        InstThrow inst = new InstThrow();
        return inst;
    }

    @Override
    public InstLandingPad visitInstLandingPad(InstLandingPadContext ctx) {
        InstLandingPad inst = new InstLandingPad();
        return inst;
    }

    @Override
    public InstExtractValue visitInstExtractValue(InstExtractValueContext ctx) {
        InstExtractValue inst = new InstExtractValue();
        inst.setStructType((Struct) makeType(ctx.type()));
        inst.setIndex(intLitToInt(ctx.intLiteral()));
        return inst;
    }

    @Override
    public InstInsertValue visitInstInsertValue(InstInsertValueContext ctx) {
        InstInsertValue inst = new InstInsertValue();
        inst.setStructType((Struct) makeType(ctx.type()));
        inst.setIndex(intLitToInt(ctx.intLiteral()));
        return inst;
    }

    @Override
    public InstNew visitInstNew(InstNewContext ctx) {
        InstNew inst = new InstNew();
        inst.setAllocType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstNewHybrid visitInstNewHybrid(InstNewHybridContext ctx) {
        InstNewHybrid inst = new InstNewHybrid();
        inst.setAllocType((Hybrid) makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstAlloca visitInstAlloca(InstAllocaContext ctx) {
        InstAlloca inst = new InstAlloca();
        inst.setAllocType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstAllocaHybrid visitInstAllocaHybrid(InstAllocaHybridContext ctx) {
        InstAllocaHybrid inst = new InstAllocaHybrid();
        inst.setAllocType((Hybrid) makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstGetIRef visitInstGetIRef(InstGetIRefContext ctx) {
        InstGetIRef inst = new InstGetIRef();
        inst.setReferentType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstGetFieldIRef visitInstGetFieldIRef(InstGetFieldIRefContext ctx) {
        InstGetFieldIRef inst = new InstGetFieldIRef();
        inst.setReferentType((Struct) makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstGetElemIRef visitInstGetElemIRef(InstGetElemIRefContext ctx) {
        InstGetElemIRef inst = new InstGetElemIRef();
        inst.setReferentType((Array) makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstShiftIRef visitInstShiftIRef(InstShiftIRefContext ctx) {
        InstShiftIRef inst = new InstShiftIRef();
        inst.setReferentType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstGetFixedPartIRef visitInstGetFixedPartIRef(
            InstGetFixedPartIRefContext ctx) {
        InstGetFixedPartIRef inst = new InstGetFixedPartIRef();
        inst.setReferentType((Hybrid) makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstGetVarPartIRef visitInstGetVarPartIRef(
            InstGetVarPartIRefContext ctx) {
        InstGetVarPartIRef inst = new InstGetVarPartIRef();
        inst.setReferentType((Hybrid) makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstLoad visitInstLoad(InstLoadContext ctx) {
        InstLoad inst = new InstLoad();
        AtomicordContext atomicord = ctx.atomicord();
        AtomicOrdering ordering = atomicord != null ? AtomicOrdering
                .valueOf(atomicord.getText()) : AtomicOrdering.NOT_ATOMIC;
        inst.setOrdering(ordering);
        inst.setReferentType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstStore visitInstStore(InstStoreContext ctx) {
        InstStore inst = new InstStore();
        AtomicordContext atomicord = ctx.atomicord();
        AtomicOrdering ordering = atomicord != null ? AtomicOrdering
                .valueOf(atomicord.getText()) : AtomicOrdering.NOT_ATOMIC;
        inst.setOrdering(ordering);
        inst.setReferentType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstCmpXchg visitInstCmpXchg(InstCmpXchgContext ctx) {
        InstCmpXchg inst = new InstCmpXchg();
        inst.setOrderingSucc(AtomicOrdering.valueOf(ctx.atomicord(0).getText()));
        inst.setOrderingFail(AtomicOrdering.valueOf(ctx.atomicord(1).getText()));
        inst.setReferentType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstAtomicRMW visitInstAtomicRMW(InstAtomicRMWContext ctx) {
        InstAtomicRMW inst = new InstAtomicRMW();
        inst.setOrdering(AtomicOrdering.valueOf(ctx.atomicord().getText()));
        inst.setOptr(AtomicRMWOp.valueOf(ctx.atomicrmwop().getText()));
        inst.setReferentType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstFence visitInstFence(InstFenceContext ctx) {
        InstFence inst = new InstFence();
        inst.setOrdering(AtomicOrdering.valueOf(ctx.atomicord().getText()));
        return inst;
    }

    @Override
    public InstTrap visitInstTrap(InstTrapContext ctx) {
        InstTrap inst = new InstTrap();
        inst.setType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstWatchPoint visitInstWatchPoint(InstWatchPointContext ctx) {
        InstWatchPoint inst = new InstWatchPoint();
        inst.setWatchPointId(intLitToInt(ctx.intLiteral()));
        inst.setType(makeType(ctx.type()));
        return inst;
    }

    @Override
    public InstCCall visitInstCCall(InstCCallContext ctx) {
        InstCCall inst = new InstCCall();
        inst.setCallConv(CallConv.valueOf(ctx.callconv().getText()));
        inst.setSig(makeFuncSig(ctx.funcCallBody().funcSig()));
        return inst;
    }

    @Override
    public InstNewStack visitInstNewStack(InstNewStackContext ctx) {
        InstNewStack inst = new InstNewStack();
        inst.setSig(makeFuncSig(ctx.funcCallBody().funcSig()));
        return inst;
    }

    @Override
    public InstICall visitInstICall(InstICallContext ctx) {
        InstICall inst = new InstICall();
        inst.setIntrinsicFunction(IntrinsicFunctionFactory
                .getIntrinsicFunctionByName(ctx.IDENTIFIER().getText()));
        return inst;
    }

    @Override
    public InstIInvoke visitInstIInvoke(InstIInvokeContext ctx) {
        InstIInvoke inst = new InstIInvoke();
        inst.setIntrinsicFunction(IntrinsicFunctionFactory
                .getIntrinsicFunctionByName(ctx.IDENTIFIER(0).getText()));
        return inst;
    }

    // Delegated helper methods.

    private Type makeType(TypeContext type) {
        return fb.rbb.deepTypeMaker.visit(type);
    }

    private FunctionSignature makeFuncSig(FuncSigContext funcSig) {
        return fb.rbb.deepFuncSigMaker.visit(funcSig);
    }

    private int intLitToInt(IntLiteralContext intLiteral) {
        return fb.rbb.intLitToInt(intLiteral);
    }

}
