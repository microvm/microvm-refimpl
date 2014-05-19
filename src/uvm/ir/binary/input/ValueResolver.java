package uvm.ir.binary.input;

import uvm.BasicBlock;
import uvm.ssavalue.DoubleConstant;
import uvm.ssavalue.FloatConstant;
import uvm.ssavalue.FunctionConstant;
import uvm.ssavalue.GlobalDataConstant;
import uvm.ssavalue.HasArgs;
import uvm.ssavalue.HasKeepAlives;
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
import uvm.ssavalue.Value;
import uvm.ssavalue.ValueVisitor;
import uvm.type.Array;
import uvm.type.Hybrid;
import uvm.type.Int;
import uvm.type.Struct;

public class ValueResolver extends AbstractResolver implements
        ValueVisitor<Void> {

    private ToResolve<Value> tr;

    public ValueResolver(IRBinaryReader br, ToResolve<Value> tr) {
        super(br);
        this.tr = tr;
    }

    @Override
    public Void visitIntConstant(IntConstant constant) {
        constant.setType((Int) type(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitFloatConstant(FloatConstant constant) {
        constant.setType((uvm.type.Float) type(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitDoubleConstant(DoubleConstant constant) {
        constant.setType((uvm.type.Double) type(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitStructConstant(StructConstant constant) {
        constant.setType((Struct) type(tr.ids[0]));
        for (int id : tr.ids2) {
            constant.getValues().add(constant(id));
        }
        return null;
    }

    @Override
    public Void visitNullConstant(NullConstant constant) {
        constant.setType(type(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitGlobalDataConstant(GlobalDataConstant constant) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitFunctionConstant(FunctionConstant constant) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitParameter(Parameter parameter) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitBinOp(InstBinOp inst) {
        inst.setType(type(tr.ids[0]));
        inst.setOp1(value(tr.ids[1]));
        inst.setOp2(value(tr.ids[2]));
        return null;
    }

    @Override
    public Void visitCmp(InstCmp inst) {
        inst.setOpndType(type(tr.ids[0]));
        inst.setOp1(value(tr.ids[1]));
        inst.setOp2(value(tr.ids[2]));
        return null;
    }

    @Override
    public Void visitConversion(InstConversion inst) {
        inst.setFromType(type(tr.ids[0]));
        inst.setToType(type(tr.ids[1]));
        inst.setOpnd(value(tr.ids[2]));
        return null;
    }

    @Override
    public Void visitSelect(InstSelect inst) {
        inst.setType(type(tr.ids[0]));
        inst.setCond(value(tr.ids[1]));
        inst.setIfTrue(value(tr.ids[2]));
        inst.setIfFalse(value(tr.ids[3]));
        return null;
    }

    @Override
    public Void visitBranch(InstBranch inst) {
        inst.setDest(bb(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitBranch2(InstBranch2 inst) {
        inst.setCond(value(tr.ids[0]));
        inst.setIfTrue(bb(tr.ids[1]));
        inst.setIfFalse(bb(tr.ids[2]));
        return null;
    }

    @Override
    public Void visitSwitch(InstSwitch inst) {
        inst.setOpndType(type(tr.ids[0]));
        inst.setOpnd(value(tr.ids[1]));
        inst.setDefaultDest(bb(tr.ids[2]));
        for (int i = 0; i < tr.ids2.length; i++) {
            Value cas = value(tr.ids2[i]);
            BasicBlock dst = bb(tr.ids3[i]);
            inst.setDestFor(cas, dst);
        }
        return null;
    }

    @Override
    public Void visitPhi(InstPhi inst) {
        inst.setType(type(tr.ids[0]));
        for (int i = 0; i < tr.ids2.length; i++) {
            BasicBlock bb = bb(tr.ids2[i]);
            Value val = value(tr.ids3[i]);
            inst.setValueFrom(bb, val);
        }
        return null;
    }

    void addArgs(HasArgs inst, int[] ids) {
        for (int id : ids) {
            inst.addArg(value(id));
        }
    }

    void addKeepAlives(HasKeepAlives inst, int[] ids) {
        for (int id : ids) {
            inst.addKeepAlive(value(id));
        }
    }

    @Override
    public Void visitCall(InstCall inst) {
        inst.setSig(sig(tr.ids[0]));
        inst.setFunc(value(tr.ids[1]));
        addArgs(inst, tr.ids2);
        addKeepAlives(inst, tr.ids3);
        return null;
    }

    @Override
    public Void visitInvoke(InstInvoke inst) {
        inst.setSig(sig(tr.ids[0]));
        inst.setFunc(value(tr.ids[1]));
        inst.setNor(bb(tr.ids[2]));
        inst.setExc(bb(tr.ids[3]));
        addArgs(inst, tr.ids2);
        addKeepAlives(inst, tr.ids3);
        return null;
    }

    @Override
    public Void visitTailCall(InstTailCall inst) {
        inst.setSig(sig(tr.ids[0]));
        inst.setFunc(value(tr.ids[1]));
        addArgs(inst, tr.ids2);
        return null;
    }

    @Override
    public Void visitRet(InstRet inst) {
        inst.setRetType(type(tr.ids[0]));
        inst.setRetVal(value(tr.ids[1]));
        return null;
    }

    @Override
    public Void visitRetVoid(InstRetVoid inst) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitThrow(InstThrow inst) {
        inst.setException(value(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitLandingPad(InstLandingPad inst) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitExtractValue(InstExtractValue inst) {
        inst.setStructType((Struct) type(tr.ids[0]));
        inst.setOpnd(value(tr.ids[1]));
        return null;
    }

    @Override
    public Void visitInsertValue(InstInsertValue inst) {
        inst.setStructType((Struct) type(tr.ids[0]));
        inst.setOpnd(value(tr.ids[1]));
        inst.setNewVal(value(tr.ids[2]));
        return null;
    }

    @Override
    public Void visitNew(InstNew inst) {
        inst.setAllocType(type(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitNewHybrid(InstNewHybrid inst) {
        inst.setAllocType((Hybrid) type(tr.ids[0]));
        inst.setLength(value(tr.ids[1]));
        return null;
    }

    @Override
    public Void visitAlloca(InstAlloca inst) {
        inst.setAllocType(type(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitAllocaHybrid(InstAllocaHybrid inst) {
        inst.setAllocType((Hybrid) type(tr.ids[0]));
        inst.setLength(value(tr.ids[1]));
        return null;
    }

    @Override
    public Void visitGetIRef(InstGetIRef inst) {
        inst.setReferentType(type(tr.ids[0]));
        inst.setOpnd(value(tr.ids[1]));
        return null;
    }

    @Override
    public Void visitGetFieldIRef(InstGetFieldIRef inst) {
        inst.setReferentType((Struct) type(tr.ids[0]));
        inst.setOpnd(value(tr.ids[1]));
        return null;
    }

    @Override
    public Void visitGetElemIRef(InstGetElemIRef inst) {
        inst.setReferentType((Array) type(tr.ids[0]));
        inst.setOpnd(value(tr.ids[1]));
        inst.setIndex(value(tr.ids[2]));
        return null;
    }

    @Override
    public Void visitShiftIRef(InstShiftIRef inst) {
        inst.setReferentType((Array) type(tr.ids[0]));
        inst.setOpnd(value(tr.ids[1]));
        inst.setOffset(value(tr.ids[2]));
        return null;
    }

    @Override
    public Void visitGetFixedPartIRef(InstGetFixedPartIRef inst) {
        inst.setReferentType((Hybrid) type(tr.ids[0]));
        inst.setOpnd(value(tr.ids[1]));
        return null;
    }

    @Override
    public Void visitGetVarPartIRef(InstGetVarPartIRef inst) {
        inst.setReferentType((Hybrid) type(tr.ids[0]));
        inst.setOpnd(value(tr.ids[1]));
        return null;
    }

    @Override
    public Void visitLoad(InstLoad inst) {
        inst.setReferentType(type(tr.ids[0]));
        inst.setLocation(value(tr.ids[1]));
        return null;
    }

    @Override
    public Void visitStore(InstStore inst) {
        inst.setReferentType(type(tr.ids[0]));
        inst.setLocation(value(tr.ids[1]));
        inst.setNewVal(value(tr.ids[2]));
        return null;
    }

    @Override
    public Void visitCmpXchg(InstCmpXchg inst) {
        inst.setReferentType(type(tr.ids[0]));
        inst.setLocation(value(tr.ids[1]));
        inst.setExpected(value(tr.ids[2]));
        inst.setDesired(value(tr.ids[3]));
        return null;
    }

    @Override
    public Void visitAtomicRMW(InstAtomicRMW inst) {
        inst.setReferentType(type(tr.ids[0]));
        inst.setLocation(value(tr.ids[1]));
        inst.setOpnd(value(tr.ids[2]));
        return null;
    }

    @Override
    public Void visitFence(InstFence inst) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitTrap(InstTrap inst) {
        inst.setType(type(tr.ids[0]));
        inst.setNor(bb(tr.ids[1]));
        inst.setExc(bb(tr.ids[2]));
        addKeepAlives(inst, tr.ids2);
        return null;
    }

    @Override
    public Void visitWatchPoint(InstWatchPoint inst) {
        inst.setType(type(tr.ids[0]));
        inst.setDisabled(bb(tr.ids[1]));
        inst.setNor(bb(tr.ids[2]));
        inst.setExc(bb(tr.ids[3]));
        addKeepAlives(inst, tr.ids2);
        return null;
    }

    @Override
    public Void visitCCall(InstCCall inst) {
        inst.setSig(sig(tr.ids[0]));
        inst.setFunc(value(tr.ids[1]));
        addArgs(inst, tr.ids2);
        return null;
    }

    @Override
    public Void visitNewStack(InstNewStack inst) {
        inst.setSig(sig(tr.ids[0]));
        inst.setFunc(value(tr.ids[1]));
        addArgs(inst, tr.ids2);
        return null;
    }

    @Override
    public Void visitICall(InstICall inst) {
        inst.setIFunc(ifunc(tr.ids[0]));
        addArgs(inst, tr.ids2);
        addKeepAlives(inst, tr.ids3);
        return null;
    }

    @Override
    public Void visitIInvoke(InstIInvoke inst) {
        inst.setIFunc(ifunc(tr.ids[0]));
        inst.setNor(bb(tr.ids[1]));
        inst.setExc(bb(tr.ids[2]));
        addArgs(inst, tr.ids2);
        addKeepAlives(inst, tr.ids3);
        return null;
    }

}
