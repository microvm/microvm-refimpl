package uvm.ir.binary.input;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uvm.BasicBlock;
import uvm.Bundle;
import uvm.CFG;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
import uvm.IdentifiedSettable;
import uvm.Namespace;
import uvm.OpCode;
import uvm.TopLevelOpCodes;
import uvm.ir.io.NestedIOException;
import uvm.ssavalue.AtomicOrdering;
import uvm.ssavalue.AtomicRMWOp;
import uvm.ssavalue.BinOptr;
import uvm.ssavalue.CallConv;
import uvm.ssavalue.CmpOptr;
import uvm.ssavalue.Constant;
import uvm.ssavalue.ConvOptr;
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
import uvm.ssavalue.Instruction;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.Parameter;
import uvm.ssavalue.StructConstant;
import uvm.ssavalue.Value;
import uvm.type.Array;
import uvm.type.Func;
import uvm.type.Hybrid;
import uvm.type.IRef;
import uvm.type.Int;
import uvm.type.Ref;
import uvm.type.Struct;
import uvm.type.Type;
import uvm.type.WeakRef;
import uvm.util.LogUtil;

/**
 * Read a bundle in the binary form.
 * <p>
 * This takes the DOM-like approach. An abstract syntax tree-like structure is
 * composed using "AbstractModel".
 */
public class IRBinaryReader implements Closeable {
    private static final String UTF8 = "UTF-8";

    private BinaryInputStream bis;

    Bundle bundle = new Bundle();

    // To resolve later

    private List<ToResolve<Type>> pendingTypes = new ArrayList<ToResolve<Type>>();
    private List<ToResolve<FunctionSignature>> pendingFuncSigs = new ArrayList<ToResolve<FunctionSignature>>();
    private List<ToResolve<GlobalData>> pendingGlobals = new ArrayList<ToResolve<GlobalData>>();
    private List<ToResolve<Function>> pendingFuncs = new ArrayList<ToResolve<Function>>();
    private List<ToResolve<Value>> pendingGlobalValues = new ArrayList<ToResolve<Value>>();
    private Map<CFG, List<ToResolve<Value>>> pendingLocalValues = new HashMap<CFG, List<ToResolve<Value>>>();

    // For name binding

    private Map<Integer, String> bindings = new HashMap<Integer, String>();

    // Public interfaces

    public IRBinaryReader(InputStream is) {
        bis = new BinaryInputStream(is);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void readBundle() {
        while (true) {
            int maybeOpCode = bis.maybeReadOpc();
            if (maybeOpCode == -1) {
                break;
            }

            readTopLevel(maybeOpCode);
        }

        resolveIDs();

        bindNames();
    }

    // Resolve later

    private void resolveTypeLater(Type obj, int... ids) {
        pendingTypes.add(new ToResolve<Type>(obj, ids));
    }

    private void resolveFuncSigLater(FunctionSignature obj, int[] ids2,
            int[] ids3, int... ids) {
        pendingFuncSigs.add(new ToResolve<FunctionSignature>(obj, ids2, ids3,
                ids));
    }

    private void resolveGlobalLater(GlobalData obj, int... ids) {
        pendingGlobals.add(new ToResolve<GlobalData>(obj, ids));
    }

    private void resolveFuncLater(Function obj, int... ids) {
        pendingFuncs.add(new ToResolve<Function>(obj, ids));
    }

    private void resolveGlobalValueLater(Value obj, int... ids) {
        pendingGlobalValues.add(new ToResolve<Value>(obj, ids));
    }

    private void resolveGlobalValueLater(Value obj, int[] ids2, int[] ids3,
            int... ids) {
        pendingGlobalValues.add(new ToResolve<Value>(obj, ids2, ids3, ids));
    }

    private void resolveLocalValueLater(List<ToResolve<Value>> trList,
            Value obj, int... ids) {
        trList.add(new ToResolve<Value>(obj, ids));
    }

    private void resolveLocalValueLater(List<ToResolve<Value>> trList,
            Value obj, int[] ids2, int[] ids3, int... ids) {
        trList.add(new ToResolve<Value>(obj, ids2, ids3, ids));
    }

    // Read from binary.

    private void readTopLevel(int opc) {
        switch (opc) {
        case TopLevelOpCodes.TYPEDEF:
            readTypeDef(opc);
            break;
        case TopLevelOpCodes.FUNCSIG:
            readFuncSig(opc);
            break;
        case TopLevelOpCodes.CONST:
            readConstDef(opc);
            break;
        case TopLevelOpCodes.GLOBAL:
            readGlobalData(opc);
            break;
        case TopLevelOpCodes.FUNCDECL:
            readFuncDecl(opc);
            break;
        case TopLevelOpCodes.FUNCDEF:
            readFuncDef(opc);
            break;
        case TopLevelOpCodes.NAMEBIND:
            readNameBind(opc);
            break;
        }
    }

    private int[] readIDList() {
        int len = bis.readLen();
        int[] ids = new int[len];
        for (int i = 0; i < len; i++) {
            int id = bis.readID();
            ids[i] = id;
        }
        return ids;
    }

    private void readTypeDef(int opc) {
        int id = bis.readID();

        int typeOpc = bis.readOpc();

        Type type = null;

        switch (typeOpc) {
        case TopLevelOpCodes.INT: {
            int length = bis.readByte();
            type = new Int(length);
            break;
        }
        case TopLevelOpCodes.FLOAT: {
            type = new uvm.type.Float();
            break;
        }
        case TopLevelOpCodes.DOUBLE: {
            type = new uvm.type.Double();
            break;
        }
        case TopLevelOpCodes.REF: {
            int referentType = bis.readID();
            type = new Ref();
            resolveTypeLater(type, referentType);
            break;
        }
        case TopLevelOpCodes.IREF: {
            int referentType = bis.readID();
            type = new IRef();
            resolveTypeLater(type, referentType);
            break;
        }
        case TopLevelOpCodes.WEAKREF: {
            int referentType = bis.readID();
            type = new WeakRef();
            resolveTypeLater(type, referentType);
            break;
        }
        case TopLevelOpCodes.STRUCT: {
            int[] fields = readIDList();
            type = new Struct();
            resolveTypeLater(type, fields);
            break;
        }
        case TopLevelOpCodes.ARRAY: {
            int elemType = bis.readID();
            long len = bis.readArySz();
            type = new Array(null, (int) len);
            resolveTypeLater(type, elemType);
            break;
        }
        case TopLevelOpCodes.HYBRID: {
            int fType = bis.readID();
            int vType = bis.readID();
            type = new Hybrid();
            resolveTypeLater(type, fType, vType);
            break;
        }
        case TopLevelOpCodes.VOID: {
            type = new uvm.type.Void();
            break;
        }
        case TopLevelOpCodes.FUNC: {
            int sig = bis.readID();
            type = new Func();
            resolveTypeLater(type, sig);
            break;
        }
        case TopLevelOpCodes.THREAD: {
            type = new uvm.type.Thread();
            break;
        }
        case TopLevelOpCodes.STACK: {
            type = new uvm.type.Stack();
            break;
        }
        case TopLevelOpCodes.TAGREF64: {
            type = new uvm.type.TagRef64();
            break;
        }
        default: {
            throw new RuntimeException("Unknown type code " + typeOpc);
        }
        }

        type.setID(id);
        bundle.getTypeNs().put(id, null, type);
    }

    private void readFuncSig(int opc) {
        int id = bis.readID();
        int retTy = bis.readID();
        int[] paramTys = readIDList();
        FunctionSignature sig = new FunctionSignature();
        sig.setID(id);
        bundle.getFuncSigNs().put(id, null, sig);
        resolveFuncSigLater(sig, paramTys, null, retTy);
    }

    private void readConstDef(int opc) {
        int id = bis.readID();
        int t = bis.readID();
        int constOpc = bis.readOpc();

        Constant constant = null;

        switch (constOpc) {
        case TopLevelOpCodes.INTCC: {
            long num = bis.readLong();
            constant = new IntConstant(null, num);
            resolveGlobalValueLater(constant, t);
            break;
        }
        case TopLevelOpCodes.FLOATCC: {
            float num = bis.readFloat();
            constant = new FloatConstant(null, num);
            resolveGlobalValueLater(constant, t);
            break;
        }
        case TopLevelOpCodes.DOUBLECC: {
            double num = bis.readDouble();
            constant = new DoubleConstant(null, num);
            resolveGlobalValueLater(constant, t);
            break;
        }
        case TopLevelOpCodes.STRUCTCC: {
            int[] ids = readIDList();
            constant = new StructConstant();
            resolveGlobalValueLater(constant, ids, null, t);
            break;
        }
        case TopLevelOpCodes.NULLCC: {
            constant = new NullConstant();
            resolveGlobalValueLater(constant, t);
            break;
        }
        }

        constant.setID(id);
        bundle.getDeclaredConstNs().put(id, null, constant);
        bundle.getGlobalValueNs().put(id, null, constant);
    }

    private void readGlobalData(int opc) {
        int id = bis.readID();
        int ty = bis.readID();
        GlobalData globalData = new GlobalData();
        globalData.setID(id);
        bundle.getGlobalDataNs().put(id, null, globalData);
        resolveGlobalLater(globalData, ty);
    }

    private void readFuncDecl(int opc) {
        int id = bis.readID();
        int sig = bis.readID();
        Function func = new Function();
        func.setID(id);
        bundle.getFuncNs().put(id, null, func);
        resolveFuncLater(func, sig);
    }

    private void readFuncDef(int opc) {
        int id = bis.readID();
        int sig = bis.readID();

        Function func = new Function();
        func.setID(id);
        bundle.getFuncNs().put(id, null, func);
        resolveFuncLater(func, sig);

        CFG cfg = new CFG();
        cfg.setFunc(func);
        func.setCFG(cfg);

        List<ToResolve<Value>> localTrList = new ArrayList<ToResolve<Value>>();
        pendingLocalValues.put(cfg, localTrList);

        int[] params = readIDList();
        for (int i = 0; i < params.length; i++) {
            Parameter param = new Parameter();
            param.setParamIndex(i);
            param.setID(params[i]);
            resolveLocalValueLater(localTrList, param, sig);
            cfg.getParams().add(param);
            cfg.getInstNs().put(param.getID(), null, param);
        }

        int nBBs = bis.readInt();

        for (int i = 0; i < nBBs; i++) {
            int bbID = bis.readID();

            BasicBlock bb = new BasicBlock(cfg);
            bb.setID(bbID);
            cfg.getBBNs().put(bbID, null, bb);

            int nInsts = bis.readInt();
            for (int j = 0; j < nInsts; j++) {
                Instruction inst = readInst(localTrList);
                bb.addInstruction(inst);
                cfg.getInstNs().put(inst.getID(), null, inst);
            }
        }
    }

    private Instruction readInst(List<ToResolve<Value>> trs) {
        int id = bis.readID();
        int opc = bis.readOpc();

        Instruction inst = null;

        switch (opc) {
        case OpCode.ADD:
        case OpCode.SUB:
        case OpCode.MUL:
        case OpCode.UDIV:
        case OpCode.SDIV:
        case OpCode.UREM:
        case OpCode.SREM:
        case OpCode.SHL:
        case OpCode.LSHR:
        case OpCode.ASHR:
        case OpCode.AND:
        case OpCode.OR:
        case OpCode.XOR:
        case OpCode.FADD:
        case OpCode.FSUB:
        case OpCode.FMUL:
        case OpCode.FDIV:
        case OpCode.FREM: {
            InstBinOp theInst = new InstBinOp();
            inst = theInst;
            int t = bis.readID();
            int op1 = bis.readID();
            int op2 = bis.readID();
            theInst.setOptr(BinOptr.valueByOpcode(opc));
            resolveLocalValueLater(trs, inst, t, op1, op2);
            break;
        }
        case OpCode.EQ:
        case OpCode.NE:
        case OpCode.SGE:
        case OpCode.SGT:
        case OpCode.SLE:
        case OpCode.SLT:
        case OpCode.UGE:
        case OpCode.UGT:
        case OpCode.ULE:
        case OpCode.ULT:
        case OpCode.FFALSE:
        case OpCode.FTRUE:
        case OpCode.FUNO:
        case OpCode.FUEQ:
        case OpCode.FUNE:
        case OpCode.FUGT:
        case OpCode.FUGE:
        case OpCode.FULT:
        case OpCode.FULE:
        case OpCode.FORD:
        case OpCode.FOEQ:
        case OpCode.FONE:
        case OpCode.FOGT:
        case OpCode.FOGE:
        case OpCode.FOLT:
        case OpCode.FOLE: {
            InstCmp theInst = new InstCmp();
            inst = theInst;
            int t = bis.readID();
            int op1 = bis.readID();
            int op2 = bis.readID();
            theInst.setOptr(CmpOptr.valueByOpcode(opc));
            resolveLocalValueLater(trs, inst, t, op1, op2);
            break;
        }
        case OpCode.TRUNC:
        case OpCode.ZEXT:
        case OpCode.SEXT:
        case OpCode.FPTRUNC:
        case OpCode.FPEXT:
        case OpCode.FPTOUI:
        case OpCode.FPTOSI:
        case OpCode.UITOFP:
        case OpCode.SITOFP:
        case OpCode.BITCAST:
        case OpCode.REFCAST:
        case OpCode.IREFCAST:
        case OpCode.FUNCCAST: {
            InstConversion theInst = new InstConversion();
            inst = theInst;
            int t1 = bis.readID();
            int t2 = bis.readID();
            int opnd = bis.readID();
            theInst.setOptr(ConvOptr.valueByOpcode(opc));
            resolveLocalValueLater(trs, inst, t1, t2, opnd);
            break;
        }
        case OpCode.SELECT: {
            inst = new InstSelect();
            int t = bis.readID();
            int cond = bis.readID();
            int iftrue = bis.readID();
            int iffalse = bis.readID();
            resolveLocalValueLater(trs, inst, t, cond, iftrue, iffalse);
            break;
        }
        case OpCode.BRANCH: {
            inst = new InstBranch();
            int dest = bis.readID();
            resolveLocalValueLater(trs, inst, dest);
            break;
        }
        case OpCode.BRANCH2: {
            inst = new InstBranch2();
            int cond = bis.readID();
            int iftrue = bis.readID();
            int iffalse = bis.readID();
            resolveLocalValueLater(trs, inst, cond, iftrue, iffalse);
            break;
        }
        case OpCode.SWITCH: {
            inst = new InstSwitch();
            int t = bis.readID();
            int opnd = bis.readID();
            int def = bis.readID();
            int ncases = bis.readLen();
            int[] cases = new int[ncases];
            int[] dests = new int[ncases];
            for (int i = 0; i < ncases; i++) {
                int cas = bis.readID();
                int dst = bis.readID();
                cases[i] = cas;
                dests[i] = dst;
            }
            resolveLocalValueLater(trs, inst, cases, dests, t, opnd, def);
            break;
        }
        case OpCode.PHI: {
            inst = new InstPhi();
            int t = bis.readID();
            int nbbs = bis.readLen();
            int[] bbs = new int[nbbs];
            int[] vals = new int[nbbs];
            for (int i = 0; i < nbbs; i++) {
                int bb = bis.readID();
                int val = bis.readID();
                bbs[i] = bb;
                vals[i] = val;
            }
            resolveLocalValueLater(trs, inst, bbs, vals, t);
            break;
        }
        case OpCode.CALL: {
            inst = new InstCall();
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            resolveLocalValueLater(trs, inst, args, kas, sig, func);
            break;
        }
        case OpCode.INVOKE: {
            inst = new InstInvoke();
            int sig = bis.readID();
            int func = bis.readID();
            int nor = bis.readID();
            int exc = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            resolveLocalValueLater(trs, inst, args, kas, sig, func, nor, exc);
            break;
        }
        case OpCode.TAILCALL: {
            inst = new InstTailCall();
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            resolveLocalValueLater(trs, inst, args, null, sig, func);
            break;
        }
        case OpCode.RET: {
            inst = new InstRet();
            int t = bis.readID();
            int rv = bis.readID();
            resolveLocalValueLater(trs, inst, t, rv);
            break;
        }
        case OpCode.RETVOID: {
            inst = new InstRetVoid();
            break;
        }
        case OpCode.THROW: {
            inst = new InstThrow();
            int exc = bis.readID();
            resolveLocalValueLater(trs, inst, exc);
            break;
        }
        case OpCode.LANDINGPAD: {
            inst = new InstLandingPad();
            break;
        }
        case OpCode.EXTRACTVALUE: {
            InstExtractValue theInst = new InstExtractValue();
            inst = theInst;
            int t = bis.readID();
            int index = bis.readLen();
            int opnd = bis.readID();
            theInst.setIndex(index);
            resolveLocalValueLater(trs, inst, t, opnd);
            break;
        }
        case OpCode.INSERTVALUE: {
            InstInsertValue theInst = new InstInsertValue();
            inst = theInst;
            int t = bis.readID();
            int index = bis.readLen();
            int opnd = bis.readID();
            int newVal = bis.readID();
            theInst.setIndex(index);
            resolveLocalValueLater(trs, inst, t, opnd, newVal);
            break;
        }
        case OpCode.NEW: {
            inst = new InstNew();
            int t = bis.readID();
            resolveLocalValueLater(trs, inst, t);
            break;
        }
        case OpCode.NEWHYBRID: {
            inst = new InstNewHybrid();
            int t = bis.readID();
            int len = bis.readID();
            resolveLocalValueLater(trs, inst, t, len);
            break;
        }
        case OpCode.ALLOCA: {
            inst = new InstAlloca();
            int t = bis.readID();
            resolveLocalValueLater(trs, inst, t);
            break;
        }
        case OpCode.ALLOCAHYBRID: {
            inst = new InstAllocaHybrid();
            int t = bis.readID();
            int len = bis.readID();
            resolveLocalValueLater(trs, inst, t, len);
            break;
        }
        case OpCode.GETIREF: {
            inst = new InstGetIRef();
            int t = bis.readID();
            int opnd = bis.readID();
            resolveLocalValueLater(trs, inst, t, opnd);
            break;
        }
        case OpCode.GETFIELDIREF: {
            InstGetFieldIRef theInst = new InstGetFieldIRef();
            inst = theInst;
            int t = bis.readID();
            int index = bis.readLen();
            int opnd = bis.readID();
            theInst.setIndex(index);
            resolveLocalValueLater(trs, inst, t, opnd);
            break;
        }
        case OpCode.GETELEMIREF: {
            inst = new InstGetElemIRef();
            int t = bis.readID();
            int opnd = bis.readID();
            int index = bis.readID();
            resolveLocalValueLater(trs, inst, t, opnd, index);
            break;
        }
        case OpCode.SHIFTIREF: {
            inst = new InstShiftIRef();
            int t = bis.readID();
            int opnd = bis.readID();
            int offset = bis.readID();
            resolveLocalValueLater(trs, inst, t, opnd, offset);
            break;
        }
        case OpCode.GETFIXEDPARTIREF: {
            inst = new InstGetFixedPartIRef();
            int t = bis.readID();
            int opnd = bis.readID();
            resolveLocalValueLater(trs, inst, t, opnd);
            break;
        }
        case OpCode.GETVARPARTIREF: {
            inst = new InstGetVarPartIRef();
            int t = bis.readID();
            int opnd = bis.readID();
            resolveLocalValueLater(trs, inst, t, opnd);
            break;
        }
        case OpCode.LOAD: {
            InstLoad theInst = new InstLoad();
            inst = theInst;
            int ord = bis.readOpc();
            int t = bis.readID();
            int loc = bis.readID();
            theInst.setOrdering(AtomicOrdering.valueByOpcode(ord));
            resolveLocalValueLater(trs, inst, t, loc);
            break;
        }
        case OpCode.STORE: {
            InstStore theInst = new InstStore();
            inst = theInst;
            int ord = bis.readOpc();
            int t = bis.readID();
            int loc = bis.readID();
            int newVal = bis.readID();
            theInst.setOrdering(AtomicOrdering.valueByOpcode(ord));
            resolveLocalValueLater(trs, inst, t, loc, newVal);
            break;
        }
        case OpCode.CMPXCHG: {
            InstCmpXchg theInst = new InstCmpXchg();
            inst = theInst;
            int ordSucc = bis.readOpc();
            int ordFail = bis.readOpc();
            int t = bis.readID();
            int loc = bis.readID();
            int expected = bis.readID();
            int desired = bis.readID();
            theInst.setOrderingSucc(AtomicOrdering.valueByOpcode(ordSucc));
            theInst.setOrderingFail(AtomicOrdering.valueByOpcode(ordFail));
            resolveLocalValueLater(trs, inst, t, loc, expected, desired);
            break;
        }
        case OpCode.ATOMICRMW: {
            InstAtomicRMW theInst = new InstAtomicRMW();
            inst = theInst;
            int ord = bis.readOpc();
            int optr = bis.readOpc();
            int t = bis.readID();
            int loc = bis.readID();
            int opnd = bis.readID();
            theInst.setOrdering(AtomicOrdering.valueByOpcode(ord));
            theInst.setOptr(AtomicRMWOp.valueByOpcode(optr));
            resolveLocalValueLater(trs, inst, t, loc, opnd);
            break;
        }
        case OpCode.FENCE: {
            InstFence theInst = new InstFence();
            inst = theInst;
            int ord = bis.readOpc();
            theInst.setOrdering(AtomicOrdering.valueByOpcode(ord));
            break;
        }
        case OpCode.TRAP: {
            inst = new InstTrap();
            int t = bis.readID();
            int nor = bis.readID();
            int exc = bis.readID();
            int[] kas = readIDList();
            resolveLocalValueLater(trs, inst, kas, null, t, nor, exc);
            break;
        }
        case OpCode.WATCHPOINT: {
            InstWatchPoint theInst = new InstWatchPoint();
            inst = theInst;
            int wpid = bis.readID();
            int t = bis.readID();
            int dis = bis.readID();
            int nor = bis.readID();
            int exc = bis.readID();
            int[] kas = readIDList();
            theInst.setWatchPointId(wpid);
            resolveLocalValueLater(trs, inst, kas, null, t, dis, nor, exc);
            break;
        }
        case OpCode.CCALL: {
            InstCCall theInst = new InstCCall();
            inst = theInst;
            int callconv = bis.readOpc();
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            theInst.setCallConv(CallConv.valueByOpcode(callconv));
            resolveLocalValueLater(trs, inst, args, null, sig, func);
            break;
        }
        case OpCode.NEWSTACK: {
            inst = new InstNewStack();
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            resolveLocalValueLater(trs, inst, args, null, sig, func);
            break;
        }
        case OpCode.ICALL: {
            inst = new InstICall();
            int func = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            resolveLocalValueLater(trs, inst, args, kas, func);
            break;
        }
        case OpCode.IINVOKE: {
            inst = new InstIInvoke();
            int func = bis.readID();
            int nor = bis.readID();
            int exc = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            resolveLocalValueLater(trs, inst, args, kas, func, nor, exc);
            break;
        }
        default: {
            throw new RuntimeException("Unknown instruction opcode " + opc);
        }
        }

        inst.setID(id);

        return inst;
    }

    private void readNameBind(int opc) {
        try {
            int id = bis.readID();
            int len = bis.readLen();
            byte[] buf = new byte[len];
            bis.read(buf);
            String name = new String(buf, UTF8);
            bindings.put(id, name);
            LogUtil.log("Received binding %d to %s\n", id, name);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    // Resolve items

    private void resolveIDs() {
        resolveTypes();
        resolveFuncSigs();
        resolveGlobals();
        resolveFuncs();
        resolveValues();
    }

    private void resolveTypes() {
        for (ToResolve<Type> tr : pendingTypes) {
            tr.resultObj.accept(new TypeResolver(this, tr));
        }
    }

    private void resolveFuncSigs() {
        for (ToResolve<FunctionSignature> tr : pendingFuncSigs) {
            FunctionSignature sig = tr.resultObj;
            sig.setReturnType(bundle.getTypeNs().getByID(tr.ids[0]));
            for (int i = 0; i < tr.ids2.length; i++) {
                sig.getParamTypes().add(bundle.getTypeNs().getByID(tr.ids2[i]));
            }
        }
    }

    private void resolveGlobals() {
        for (ToResolve<GlobalData> tr : pendingGlobals) {
            GlobalData gd = tr.resultObj;
            gd.setType(bundle.getTypeNs().getByID(tr.ids[0]));

            GlobalDataConstant constant = new GlobalDataConstant();
            constant.setID(gd.getID());
            constant.setGlobalData(gd);
            bundle.getGlobalValueNs().put(constant.getID(), null, constant);
        }
    }

    private void resolveFuncs() {
        for (ToResolve<Function> tr : pendingFuncs) {
            Function func = tr.resultObj;
            func.setSig(bundle.getFuncSigNs().getByID(tr.ids[0]));

            FunctionConstant constant = new FunctionConstant();
            constant.setID(func.getID());
            constant.setFunction(func);
            bundle.getGlobalValueNs().put(constant.getID(), null, constant);
        }
    }

    private void resolveValues() {
        for (ToResolve<Value> tr : pendingGlobalValues) {
            tr.resultObj.accept(new ValueResolver(this, null, tr));
        }

        for (Entry<CFG, List<ToResolve<Value>>> e : pendingLocalValues
                .entrySet()) {
            CFG cfg = e.getKey();
            for (ToResolve<Value> tr : e.getValue()) {
                tr.resultObj.accept(new ValueResolver(this, cfg, tr));
            }
        }

    }

    // Name binding

    private void bindNames() {
        bindAll(bundle.getTypeNs());
        bindAll(bundle.getFuncSigNs());
        bindAll(bundle.getGlobalDataNs());
        bindAll(bundle.getDeclaredConstNs());
        bindAll(bundle.getGlobalValueNs());
        bindAll(bundle.getFuncNs());
        for (Function func : bundle.getFuncNs().getObjects()) {
            CFG cfg = func.getCFG();
            if (cfg != null) {
                bindAll(cfg.getBBNs());
                bindAll(cfg.getInstNs());
            }
        }
    }

    // The Closeable interface

    private void bindAll(Namespace<? extends IdentifiedSettable> ns) {
        for (int id : ns.getIDSet()) {
            String name = bindings.get(id);
            if (name != null) {
                ns.bind(id, name);
                ns.getByID(id).setName(name);
                LogUtil.log("Using (ns) binding %d to %s\n", id, name);
            }
        }
    }

    @Override
    public void close() throws IOException {
        bis.close();
    }
}
