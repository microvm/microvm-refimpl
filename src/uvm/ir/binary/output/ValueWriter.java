package uvm.ir.binary.output;

import uvm.ssavalue.DoubleConstant;
import uvm.ssavalue.FloatConstant;
import uvm.ssavalue.FunctionConstant;
import uvm.ssavalue.GlobalDataConstant;
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
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.Parameter;
import uvm.ssavalue.StructConstant;
import uvm.ssavalue.ValueVisitor;

public class ValueWriter implements ValueVisitor<Void> {

    private IRBinaryWriter irBinaryWriter;

    public ValueWriter(IRBinaryWriter irBinaryWriter) {
        this.irBinaryWriter = irBinaryWriter;
    }

    @Override
    public Void visitIntConstant(IntConstant constant) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFloatConstant(FloatConstant constant) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitDoubleConstant(DoubleConstant constant) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitStructConstant(StructConstant constant) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitNullConstant(NullConstant constant) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitGlobalDataConstant(GlobalDataConstant constant) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFunctionConstant(FunctionConstant constant) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitParameter(Parameter parameter) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitBinOp(InstBinOp inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCmp(InstCmp inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitConversion(InstConversion inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSelect(InstSelect inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitBranch(InstBranch inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitBranch2(InstBranch2 inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSwitch(InstSwitch inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitPhi(InstPhi inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCall(InstCall inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitInvoke(InstInvoke inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTailCall(InstTailCall inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitRet(InstRet inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitRetVoid(InstRetVoid inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitThrow(InstThrow inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLandingPad(InstLandingPad inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitExtractValue(InstExtractValue inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitInsertValue(InstInsertValue inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitNew(InstNew inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitNewHybrid(InstNewHybrid inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAlloca(InstAlloca inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAllocaHybrid(InstAllocaHybrid inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitGetIRef(InstGetIRef inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitGetFieldIRef(InstGetFieldIRef inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitGetElemIRef(InstGetElemIRef inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitShiftIRef(InstShiftIRef inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitGetFixedPartIRef(InstGetFixedPartIRef inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitGetVarPartIRef(InstGetVarPartIRef inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLoad(InstLoad inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitStore(InstStore inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCmpXchg(InstCmpXchg inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAtomicRMW(InstAtomicRMW inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFence(InstFence inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTrap(InstTrap inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitWatchPoint(InstWatchPoint inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCCall(InstCCall inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitNewStack(InstNewStack inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitICall(InstICall inst) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIInvoke(InstIInvoke inst) {
        // TODO Auto-generated method stub
        return null;
    }

}
