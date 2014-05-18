package uvm.ir.binary.output;

import java.util.List;
import java.util.Map.Entry;

import uvm.BasicBlock;
import uvm.TopLevelOpCodes;
import uvm.ssavalue.Constant;
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
import uvm.ssavalue.UseBox;
import uvm.ssavalue.ValueVisitor;

/**
 * Write value constructors in the binary form. For constants, write constant
 * constructors. For instructions, write the instruction body.
 */
public class ValueWriter implements ValueVisitor<Void> {

    @SuppressWarnings("unused")
    private IRBinaryWriter irBinaryWriter;
    private BinaryOutputStream bos;

    public ValueWriter(IRBinaryWriter irBinaryWriter) {
        this.irBinaryWriter = irBinaryWriter;
        this.bos = irBinaryWriter.bos;
    }

    @Override
    public Void visitIntConstant(IntConstant constant) {
        bos.writeOpc(TopLevelOpCodes.INTCC);
        bos.writeLong(constant.getValue());
        return null;
    }

    @Override
    public Void visitFloatConstant(FloatConstant constant) {
        bos.writeOpc(TopLevelOpCodes.FLOATCC);
        bos.writeFloat(constant.getValue());
        return null;
    }

    @Override
    public Void visitDoubleConstant(DoubleConstant constant) {
        bos.writeOpc(TopLevelOpCodes.DOUBLECC);
        bos.writeDouble(constant.getValue());
        return null;
    }

    @Override
    public Void visitStructConstant(StructConstant constant) {
        bos.writeOpc(TopLevelOpCodes.STRUCTCC);
        bos.writeLen(constant.getValues());
        for (Constant subConst : constant.getValues()) {
            bos.writeID(subConst);
        }
        return null;
    }

    @Override
    public Void visitNullConstant(NullConstant constant) {
        bos.writeOpc(TopLevelOpCodes.NULLCC);
        return null;
    }

    @Override
    public Void visitGlobalDataConstant(GlobalDataConstant constant) {
        // Not explicitly constructed.
        return null;
    }

    @Override
    public Void visitFunctionConstant(FunctionConstant constant) {
        // Not explicitly constructed.
        return null;
    }

    @Override
    public Void visitParameter(Parameter parameter) {
        // Not explicitly constructed.
        return null;
    }

    @Override
    public Void visitBinOp(InstBinOp inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getType());
        bos.writeID(inst.getOp1());
        bos.writeID(inst.getOp2());
        return null;
    }

    @Override
    public Void visitCmp(InstCmp inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getOpndType());
        bos.writeID(inst.getOp1());
        bos.writeID(inst.getOp2());
        return null;
    }

    @Override
    public Void visitConversion(InstConversion inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getFromType());
        bos.writeID(inst.getToType());
        bos.writeID(inst.getOpnd());
        return null;
    }

    @Override
    public Void visitSelect(InstSelect inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getType());
        bos.writeID(inst.getCond());
        bos.writeID(inst.getIfTrue());
        bos.writeID(inst.getIfFalse());
        return null;
    }

    @Override
    public Void visitBranch(InstBranch inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getDest());
        return null;
    }

    @Override
    public Void visitBranch2(InstBranch2 inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getCond());
        bos.writeID(inst.getIfTrue());
        bos.writeID(inst.getIfFalse());
        return null;
    }

    @Override
    public Void visitSwitch(InstSwitch inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getOpndType());
        bos.writeID(inst.getOpnd());
        bos.writeID(inst.getDefaultDest());
        bos.writeLen(inst.getCases().entrySet());
        for (Entry<UseBox, BasicBlock> e : inst.getCases().entrySet()) {
            bos.writeID(e.getKey().getDst());
            bos.writeID(e.getValue());
        }
        return null;
    }

    @Override
    public Void visitPhi(InstPhi inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getType());
        bos.writeLen(inst.getValueMap().entrySet());
        for (Entry<BasicBlock, UseBox> e : inst.getValueMap().entrySet()) {
            bos.writeID(e.getKey());
            bos.writeID(e.getValue().getDst());
        }
        return null;
    }

    private void writeUseBoxIDList(List<UseBox> boxes) {
        bos.writeLen(boxes);
        for (UseBox ub : boxes) {
            bos.writeID(ub.getDst());
        }
    }

    @Override
    public Void visitCall(InstCall inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getSig());
        bos.writeID(inst.getFunc());
        writeUseBoxIDList(inst.getArgs());
        writeUseBoxIDList(inst.getKeepAlives());
        return null;
    }

    @Override
    public Void visitInvoke(InstInvoke inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getSig());
        bos.writeID(inst.getFunc());
        bos.writeID(inst.getNor());
        bos.writeID(inst.getExc());
        writeUseBoxIDList(inst.getArgs());
        writeUseBoxIDList(inst.getKeepAlives());
        return null;
    }

    @Override
    public Void visitTailCall(InstTailCall inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getSig());
        bos.writeID(inst.getFunc());
        writeUseBoxIDList(inst.getArgs());
        return null;
    }

    @Override
    public Void visitRet(InstRet inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getRetType());
        bos.writeID(inst.getRetVal());
        return null;
    }

    @Override
    public Void visitRetVoid(InstRetVoid inst) {
        bos.writeOpc(inst.opcode());
        return null;
    }

    @Override
    public Void visitThrow(InstThrow inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getException());
        return null;
    }

    @Override
    public Void visitLandingPad(InstLandingPad inst) {
        bos.writeOpc(inst.opcode());
        return null;
    }

    @Override
    public Void visitExtractValue(InstExtractValue inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getStructType());
        bos.writeLen(inst.getIndex());
        bos.writeID(inst.getOpnd());
        return null;
    }

    @Override
    public Void visitInsertValue(InstInsertValue inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getStructType());
        bos.writeLen(inst.getIndex());
        bos.writeID(inst.getOpnd());
        bos.writeID(inst.getNewVal());
        return null;
    }

    @Override
    public Void visitNew(InstNew inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getAllocType());
        return null;
    }

    @Override
    public Void visitNewHybrid(InstNewHybrid inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getAllocType());
        bos.writeID(inst.getLength());
        return null;
    }

    @Override
    public Void visitAlloca(InstAlloca inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getAllocType());
        return null;
    }

    @Override
    public Void visitAllocaHybrid(InstAllocaHybrid inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getAllocType());
        bos.writeID(inst.getLength());
        return null;
    }

    @Override
    public Void visitGetIRef(InstGetIRef inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getReferentType());
        bos.writeID(inst.getOpnd());
        return null;
    }

    @Override
    public Void visitGetFieldIRef(InstGetFieldIRef inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getReferentType());
        bos.writeLen(inst.getIndex());
        bos.writeID(inst.getOpnd());
        return null;
    }

    @Override
    public Void visitGetElemIRef(InstGetElemIRef inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getReferentType());
        bos.writeID(inst.getOpnd());
        bos.writeID(inst.getIndex());
        return null;
    }

    @Override
    public Void visitShiftIRef(InstShiftIRef inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getReferentType());
        bos.writeID(inst.getOpnd());
        bos.writeID(inst.getOffset());
        return null;
    }

    @Override
    public Void visitGetFixedPartIRef(InstGetFixedPartIRef inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getReferentType());
        bos.writeID(inst.getOpnd());
        return null;
    }

    @Override
    public Void visitGetVarPartIRef(InstGetVarPartIRef inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getReferentType());
        bos.writeID(inst.getOpnd());
        return null;
    }

    @Override
    public Void visitLoad(InstLoad inst) {
        bos.writeOpc(inst.opcode());
        bos.writeOpc(inst.getOrdering().getOpCode());
        bos.writeID(inst.getReferentType());
        bos.writeID(inst.getLocation());
        return null;
    }

    @Override
    public Void visitStore(InstStore inst) {
        bos.writeOpc(inst.opcode());
        bos.writeOpc(inst.getOrdering().getOpCode());
        bos.writeID(inst.getReferentType());
        bos.writeID(inst.getLocation());
        bos.writeID(inst.getNewVal());
        return null;
    }

    @Override
    public Void visitCmpXchg(InstCmpXchg inst) {
        bos.writeOpc(inst.opcode());
        bos.writeOpc(inst.getOrderingSucc().getOpCode());
        bos.writeOpc(inst.getOrderingFail().getOpCode());
        bos.writeID(inst.getReferentType());
        bos.writeID(inst.getLocation());
        bos.writeID(inst.getExpected());
        bos.writeID(inst.getDesired());
        return null;
    }

    @Override
    public Void visitAtomicRMW(InstAtomicRMW inst) {
        bos.writeOpc(inst.opcode());
        bos.writeOpc(inst.getOrdering().getOpCode());
        bos.writeOpc(inst.getOptr().getOpCode());
        bos.writeID(inst.getReferentType());
        bos.writeID(inst.getLocation());
        bos.writeID(inst.getOpnd());
        return null;
    }

    @Override
    public Void visitFence(InstFence inst) {
        bos.writeOpc(inst.opcode());
        bos.writeOpc(inst.getOrdering().getOpCode());
        return null;
    }

    @Override
    public Void visitTrap(InstTrap inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getType());
        bos.writeID(inst.getNor());
        bos.writeID(inst.getExc());
        writeUseBoxIDList(inst.getKeepAlives());
        return null;
    }

    @Override
    public Void visitWatchPoint(InstWatchPoint inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getWatchPointId());
        bos.writeID(inst.getType());
        bos.writeID(inst.getDisabled());
        bos.writeID(inst.getNor());
        bos.writeID(inst.getExc());
        writeUseBoxIDList(inst.getKeepAlives());
        return null;
    }

    @Override
    public Void visitCCall(InstCCall inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getCallConv().getOpCode());
        bos.writeID(inst.getSig());
        bos.writeID(inst.getFunc());
        writeUseBoxIDList(inst.getArgs());
        return null;
    }

    @Override
    public Void visitNewStack(InstNewStack inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getSig());
        bos.writeID(inst.getFunc());
        writeUseBoxIDList(inst.getArgs());
        return null;
    }

    @Override
    public Void visitICall(InstICall inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getIFunc());
        writeUseBoxIDList(inst.getArgs());
        writeUseBoxIDList(inst.getKeepAlives());
        return null;
    }

    @Override
    public Void visitIInvoke(InstIInvoke inst) {
        bos.writeOpc(inst.opcode());
        bos.writeID(inst.getIFunc());
        bos.writeID(inst.getNor());
        bos.writeID(inst.getExc());
        writeUseBoxIDList(inst.getArgs());
        writeUseBoxIDList(inst.getKeepAlives());
        return null;
    }
            
}
