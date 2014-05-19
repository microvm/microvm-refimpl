package uvm.ir.binary.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uvm.BasicBlock;
import uvm.Bundle;
import uvm.CFG;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
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

/**
 * Read a bundle in the binary form.
 * <p>
 * This takes the DOM-like approach. An abstract syntax tree-like structure is
 * composed using "AbstractModel".
 */
public class IRBinaryReader {
    private static final String UTF8 = "UTF-8";

    private BinaryInputStream bis;

    Bundle bundle = new Bundle();

    // To resolve later

    private List<ToResolve<Type>> pendingTypes = new ArrayList<ToResolve<Type>>();
    private List<ToResolve<FunctionSignature>> pendingFuncSigs = new ArrayList<ToResolve<FunctionSignature>>();
    private List<ToResolve<GlobalData>> pendingGlobals = new ArrayList<ToResolve<GlobalData>>();
    private List<ToResolve<Function>> pendingFuncs = new ArrayList<ToResolve<Function>>();
    private List<ToResolve<Value>> pendingValues = new ArrayList<ToResolve<Value>>();

    // For name binding

    private Map<Integer, String> bindings = new HashMap<Integer, String>();

    Map<Integer, Value> allValues = new HashMap<Integer, Value>();
    Map<Integer, BasicBlock> allBBs = new HashMap<Integer, BasicBlock>();

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

        resolveTypes();
        resolveFuncSigs();
        resolveGlobals();
        resolveFuncs();
        resolveValues();
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

    private void resolveValueLater(Value obj, int... ids) {
        pendingValues.add(new ToResolve<Value>(obj, ids));
    }

    private void resolveValueLater(Value obj, int[] ids2, int[] ids3,
            int... ids) {
        pendingValues.add(new ToResolve<Value>(obj, ids2, ids3, ids));
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
            byte length = bis.readByte();
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
            resolveValueLater(constant, t);
            break;
        }
        case TopLevelOpCodes.FLOATCC: {
            float num = bis.readFloat();
            constant = new FloatConstant(null, num);
            resolveValueLater(constant, t);
            break;
        }
        case TopLevelOpCodes.DOUBLECC: {
            double num = bis.readDouble();
            constant = new DoubleConstant(null, num);
            resolveValueLater(constant, t);
            break;
        }
        case TopLevelOpCodes.STRUCTCC: {
            int[] ids = readIDList();
            constant = new StructConstant();
            resolveValueLater(constant, ids, null, t);
            break;
        }
        case TopLevelOpCodes.NULLCC: {
            constant = new NullConstant();
            resolveValueLater(constant, t);
            break;
        }
        }

        constant.setID(id);
        bundle.getDeclaredConstNs().put(id, null, constant);
        bundle.getGlobalValueNs().put(id, null, constant);
        allValues.put(id, constant);
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
        resolveFuncLater(func, sig);
    }

    private void readFuncDef(int opc) {
        int id = bis.readID();
        int sig = bis.readID();

        Function func = new Function();
        func.setID(id);
        resolveFuncLater(func, sig);

        CFG cfg = new CFG();
        cfg.setFunc(func);
        func.setCFG(cfg);

        int[] params = readIDList();
        for (int i = 0; i < params.length; i++) {
            Parameter param = new Parameter();
            param.setParamIndex(i);
            param.setID(params[i]);
            resolveValueLater(param, sig);
            cfg.getParams().add(param);
        }

        int nBBs = bis.readInt();

        for (int i = 0; i < nBBs; i++) {
            int bbID = bis.readID();

            BasicBlock bb = new BasicBlock(cfg);
            bb.setID(bbID);
            allBBs.put(bbID, bb);

            int nInsts = bis.readInt();
            for (int j = 0; j < nInsts; j++) {
                Instruction inst = readInst();
                bb.addInstruction(inst);
                allValues.put(inst.getID(), inst);
            }
        }
    }

    private Instruction readInst() {
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
            resolveValueLater(inst, t, op1, op2);
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
            resolveValueLater(inst, t, op1, op2);
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
            resolveValueLater(inst, t1, t2, opnd);
        }
        case OpCode.SELECT: {
            inst = new InstSelect();
            int t = bis.readID();
            int cond = bis.readID();
            int iftrue = bis.readID();
            int iffalse = bis.readID();
            resolveValueLater(inst, t, cond, iftrue, iffalse);
        }
        case OpCode.BRANCH: {
            inst = new InstBranch();
            int dest = bis.readID();
            resolveValueLater(inst, dest);
        }
        case OpCode.BRANCH2: {
            inst = new InstBranch2();
            int cond = bis.readID();
            int iftrue = bis.readID();
            int iffalse = bis.readID();
            resolveValueLater(inst, cond, iftrue, iffalse);
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
            resolveValueLater(inst, cases, dests, t, opnd, def);
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
            resolveValueLater(inst, bbs, vals, t);
        }
        case OpCode.CALL: {
            inst = new InstCall();
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            resolveValueLater(inst, args, kas, sig, func);
        }
        case OpCode.INVOKE: {
            inst = new InstInvoke();
            int sig = bis.readID();
            int func = bis.readID();
            int nor = bis.readID();
            int exc = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            resolveValueLater(inst, args, kas, sig, func, nor, exc);
        }
        case OpCode.TAILCALL: {
            inst = new InstTailCall();
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            resolveValueLater(inst, args, null, sig, func);
        }
        case OpCode.RET: {
            inst = new InstRet();
            int t = bis.readID();
            int rv = bis.readID();
            resolveValueLater(inst, t, rv);
        }
        case OpCode.RETVOID: {
            inst = new InstRetVoid();
        }
        case OpCode.THROW: {
            inst = new InstThrow();
            int exc = bis.readID();
            resolveValueLater(inst, exc);
        }
        case OpCode.LANDINGPAD: {
            inst = new InstLandingPad();
        }
        case OpCode.EXTRACTVALUE: {
            InstExtractValue theInst = new InstExtractValue();
            inst = theInst;
            int t = bis.readID();
            int index = bis.readLen();
            int opnd = bis.readID();
            theInst.setIndex(index);
            resolveValueLater(inst, t, opnd);
        }
        case OpCode.INSERTVALUE: {
            InstInsertValue theInst = new InstInsertValue();
            inst = theInst;
            int t = bis.readID();
            int index = bis.readLen();
            int opnd = bis.readID();
            int newVal = bis.readID();
            theInst.setIndex(index);
            resolveValueLater(inst, t, opnd, newVal);
        }
        case OpCode.NEW: {
            inst = new InstNew();
            int t = bis.readID();
            resolveValueLater(inst, t);
        }
        case OpCode.NEWHYBRID: {
            inst = new InstNewHybrid();
            int t = bis.readID();
            int len = bis.readID();
            resolveValueLater(inst, t, len);
        }
        case OpCode.ALLOCA: {
            inst = new InstAlloca();
            int t = bis.readID();
            resolveValueLater(inst, t);
        }
        case OpCode.ALLOCAHYBRID: {
            inst = new InstAllocaHybrid();
            int t = bis.readID();
            int len = bis.readID();
            resolveValueLater(inst, t, len);
        }
        case OpCode.GETIREF: {
            inst = new InstGetIRef();
            int t = bis.readID();
            int opnd = bis.readID();
            resolveValueLater(inst, t, opnd);
        }
        case OpCode.GETFIELDIREF: {
            InstGetFieldIRef theInst = new InstGetFieldIRef();
            inst = theInst;
            int t = bis.readID();
            int index = bis.readLen();
            int opnd = bis.readID();
            theInst.setIndex(index);
            resolveValueLater(inst, t, opnd);
        }
        case OpCode.GETELEMIREF: {
            inst = new InstGetElemIRef();
            int t = bis.readID();
            int opnd = bis.readID();
            int index = bis.readID();
            resolveValueLater(inst, t, opnd, index);

        }
        case OpCode.SHIFTIREF: {
            inst = new InstShiftIRef();
            int t = bis.readID();
            int opnd = bis.readID();
            int offset = bis.readID();
            resolveValueLater(inst, t, opnd, offset);
        }
        case OpCode.GETFIXEDPARTIREF: {
            inst = new InstGetFixedPartIRef();
            int t = bis.readID();
            int opnd = bis.readID();
            resolveValueLater(inst, t, opnd);

        }
        case OpCode.GETVARPARTIREF: {
            inst = new InstGetVarPartIRef();
            int t = bis.readID();
            int opnd = bis.readID();
            resolveValueLater(inst, t, opnd);
        }
        case OpCode.LOAD: {
            InstLoad theInst = new InstLoad();
            inst = theInst;
            int ord = bis.readOpc();
            int t = bis.readID();
            int loc = bis.readID();
            theInst.setOrdering(AtomicOrdering.valueByOpcode(ord));
            resolveValueLater(inst, t, loc);
        }
        case OpCode.STORE: {
            InstStore theInst = new InstStore();
            inst = theInst;
            int ord = bis.readOpc();
            int t = bis.readID();
            int loc = bis.readID();
            int newVal = bis.readID();
            theInst.setOrdering(AtomicOrdering.valueByOpcode(ord));
            resolveValueLater(inst, t, loc, newVal);
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
            resolveValueLater(inst, t, loc, expected, desired);
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
            resolveValueLater(inst, t, loc, opnd);
        }
        case OpCode.FENCE: {
            InstFence theInst = new InstFence();
            inst = theInst;
            int ord = bis.readOpc();
            theInst.setOrdering(AtomicOrdering.valueByOpcode(ord));
        }
        case OpCode.TRAP: {
            inst = new InstTrap();
            int t = bis.readID();
            int nor = bis.readID();
            int exc = bis.readID();
            int[] kas = readIDList();
            resolveValueLater(inst, kas, null, t, nor, exc);
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
            resolveValueLater(inst, kas, null, t, dis, nor, exc);
        }
        case OpCode.CCALL: {
            InstCCall theInst = new InstCCall();
            inst = theInst;
            int callconv = bis.readOpc();
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            theInst.setCallConv(CallConv.valueByOpcode(callconv));
            resolveValueLater(inst, args, null, sig, func);
        }
        case OpCode.NEWSTACK: {
            inst = new InstNewStack();
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            resolveValueLater(inst, args, null, sig, func);
        }
        case OpCode.ICALL: {
            inst = new InstICall();
            int func = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            resolveValueLater(inst, args, kas, func);
        }
        case OpCode.IINVOKE: {
            inst = new InstInvoke();
            int func = bis.readID();
            int nor = bis.readID();
            int exc = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            resolveValueLater(inst, args, kas, func, nor, exc);
        }
        }

        inst.setID(id);
        allValues.put(id, inst);

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
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    // Resolve items

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
            constant.setGlobalData(gd);
            bundle.getGlobalValueNs().put(constant.getID(), null, constant);
            allValues.put(constant.getID(), constant);
        }
    }

    private void resolveFuncs() {
        for (ToResolve<Function> tr : pendingFuncs) {
            Function func = tr.resultObj;
            func.setSig(bundle.getFuncSigNs().getByID(tr.ids[0]));

            FunctionConstant constant = new FunctionConstant();
            constant.setFunction(func);
            bundle.getGlobalValueNs().put(constant.getID(), null, constant);
            allValues.put(constant.getID(), constant);
        }
    }

    private void resolveValues() {
        for (ToResolve<Value> tr : pendingValues) {
            tr.resultObj.accept(new ValueResolver(this, tr));
        }

    }
}
