package uvm.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uvm.type.TestingHelper.parseUir;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uvm.BasicBlock;
import uvm.Bundle;
import uvm.CFG;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
import uvm.intrinsicfunc.IntrinsicFunction;
import uvm.intrinsicfunc.IntrinsicFunctionFactory;
import uvm.ssavalue.AtomicOrdering;
import uvm.ssavalue.AtomicRMWOp;
import uvm.ssavalue.BinOptr;
import uvm.ssavalue.CallConv;
import uvm.ssavalue.CmpOptr;
import uvm.ssavalue.Constant;
import uvm.ssavalue.ConvOptr;
import uvm.ssavalue.FPConstant;
import uvm.ssavalue.HasArgs;
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
import uvm.ssavalue.UseBox;
import uvm.ssavalue.Value;

public class InstructionParsingTest {

    private static Bundle bundle;

    @BeforeClass
    public static void setUpClass() throws Exception {
        String file = "tests/uvm-parsing-test/instructions.uir";
        bundle = parseUir(file);
    }

    private static Type type(String name) {
        Type type = bundle.getTypeByName(name);
        if (type == null) {
            fail("No such type " + name);
        }
        return type;
    }

    private static Constant constant(String name) {
        Constant constant = bundle.getConstantByName(name);
        if (constant == null) {
            fail("No such constant " + name);
        }
        return constant;
    }

    private static GlobalData globalData(String name) {
        GlobalData global = bundle.getGlobalDataByName(name);
        if (global == null) {
            fail("No such global " + name);
        }
        return global;
    }

    private static Function func(String name) {
        Function func = bundle.getFuncByName(name);
        if (func == null) {
            fail("No such func " + name);
        }
        return func;
    }

    private static FunctionSignature funcSig(String name) {
        FunctionSignature sig = bundle.getFuncSigByName(name);
        if (sig == null) {
            fail("No such sig " + name);
        }
        return sig;
    }

    private static <T> T assertType(Object obj, Class<T> cls) {
        if (!cls.isAssignableFrom(obj.getClass())) {
            fail("Found " + obj.getClass().getName() + ", expect "
                    + cls.getName());
        }
        return cls.cast(obj);
    }

    private static void assertIntSize(Type type, int size) {
        if (!(type instanceof Int)) {
            fail("Found " + type.getClass().getName() + ", expect Int");
        }
        assertEquals(size, ((Int) type).getSize());
    }

    private Function curFunc;
    private CFG curCFG;

    private BasicBlock bb(String name) {
        BasicBlock bb = curCFG.getNameToBB().get(name);
        if (bb == null) {
            fail("No such bb " + name);
        }
        return bb;
    }

    private Instruction inst(String name) {
        Instruction inst = curCFG.getNameToInst().get(name);
        if (inst == null) {
            fail("No such inst " + name);
        }
        return inst;
    }

    private void loadFunc(String name) {
        curFunc = func(name);
        curCFG = curFunc.getCFG();
    }

    @Before
    public void setUp() {
        curFunc = null;
        curCFG = null;
    }

    private Parameter assertParameter(Instruction inst, int index) {
        Parameter param = assertType(inst, Parameter.class);
        assertEquals(index, param.getParamIndex());
        return param;
    }

    private InstBinOp assertBinOp(Instruction inst, BinOptr optr, Value op1,
            Value op2) {
        InstBinOp binOp = assertType(inst, InstBinOp.class);
        assertEquals(optr, binOp.getOptr());
        assertEquals(op1, binOp.getOp1());
        assertEquals(op2, binOp.getOp2());
        return binOp;
    }

    @Test
    public void testIntBinOp() {
        loadFunc("@intBinOpTest");
        Parameter p0 = assertParameter(inst("%p0"), 0);
        Parameter p1 = assertParameter(inst("%p1"), 1);
        InstBinOp add = assertBinOp(inst("%add"), BinOptr.ADD, p0, p1);
        InstBinOp sub = assertBinOp(inst("%sub"), BinOptr.SUB, p0, p1);
        InstBinOp mul = assertBinOp(inst("%mul"), BinOptr.MUL, p0, p1);
        InstBinOp udiv = assertBinOp(inst("%udiv"), BinOptr.UDIV, p0, p1);
        InstBinOp sdiv = assertBinOp(inst("%sdiv"), BinOptr.SDIV, p0, p1);
        InstBinOp urem = assertBinOp(inst("%urem"), BinOptr.UREM, p0, p1);
        InstBinOp srem = assertBinOp(inst("%srem"), BinOptr.SREM, p0, p1);
        InstBinOp shl = assertBinOp(inst("%shl"), BinOptr.SHL, p0, p1);
        InstBinOp lshr = assertBinOp(inst("%lshr"), BinOptr.LSHR, p0, p1);
        InstBinOp ashr = assertBinOp(inst("%ashr"), BinOptr.ASHR, p0, p1);
        InstBinOp and = assertBinOp(inst("%and"), BinOptr.AND, p0, p1);
        InstBinOp or = assertBinOp(inst("%or"), BinOptr.OR, p0, p1);
        InstBinOp xor = assertBinOp(inst("%xor"), BinOptr.XOR, p0, p1);

        for (InstBinOp binOp : new InstBinOp[] { add, sub, mul, udiv, sdiv,
                urem, srem, shl, lshr, ashr, and, or, xor }) {
            assertIntSize(binOp.getType(), 32);
        }
    }

    @Test
    public void testFPBinOp() {
        loadFunc("@fpBinOpTest");
        Parameter p0 = assertParameter(inst("%p0"), 0);
        Parameter p1 = assertParameter(inst("%p1"), 1);
        InstBinOp fadd = assertBinOp(inst("%fadd"), BinOptr.FADD, p0, p1);
        InstBinOp fsub = assertBinOp(inst("%fsub"), BinOptr.FSUB, p0, p1);
        InstBinOp fmul = assertBinOp(inst("%fmul"), BinOptr.FMUL, p0, p1);
        InstBinOp fdiv = assertBinOp(inst("%fdiv"), BinOptr.FDIV, p0, p1);
        InstBinOp frem = assertBinOp(inst("%frem"), BinOptr.FREM, p0, p1);

        for (InstBinOp binOp : new InstBinOp[] { fadd, fsub, fmul, fdiv, frem }) {
            assertType(binOp.getType(), uvm.type.Double.class);
        }
    }

    private InstCmp assertCmp(Instruction inst, CmpOptr optr, Value op1,
            Value op2) {
        InstCmp cmp = assertType(inst, InstCmp.class);
        assertEquals(optr, cmp.getOptr());
        assertEquals(op1, cmp.getOp1());
        assertEquals(op2, cmp.getOp2());
        return cmp;
    }

    @Test
    public void testIntCmp() {
        loadFunc("@intCmpTest");
        Parameter p0 = assertParameter(inst("%p0"), 0);
        Parameter p1 = assertParameter(inst("%p1"), 1);
        InstCmp eq = assertCmp(inst("%eq"), CmpOptr.EQ, p0, p1);
        InstCmp ne = assertCmp(inst("%ne"), CmpOptr.NE, p0, p1);
        InstCmp ult = assertCmp(inst("%ult"), CmpOptr.ULT, p0, p1);
        InstCmp ule = assertCmp(inst("%ule"), CmpOptr.ULE, p0, p1);
        InstCmp ugt = assertCmp(inst("%ugt"), CmpOptr.UGT, p0, p1);
        InstCmp uge = assertCmp(inst("%uge"), CmpOptr.UGE, p0, p1);
        InstCmp slt = assertCmp(inst("%slt"), CmpOptr.SLT, p0, p1);
        InstCmp sle = assertCmp(inst("%sle"), CmpOptr.SLE, p0, p1);
        InstCmp sgt = assertCmp(inst("%sgt"), CmpOptr.SGT, p0, p1);
        InstCmp sge = assertCmp(inst("%sge"), CmpOptr.SGE, p0, p1);

        for (InstCmp cmp : new InstCmp[] { eq, ne, ult, ule, ugt, uge, slt,
                sle, sgt, sge }) {
            assertIntSize(cmp.getOpndType(), 64);
            assertIntSize(cmp.getType(), 1);
        }
    }

    @Test
    public void testFPCmp() {
        loadFunc("@fpCmpTest");
        Parameter p0 = assertParameter(inst("%p0"), 0);
        Parameter p1 = assertParameter(inst("%p1"), 1);
        InstCmp ftrue = assertCmp(inst("%ftrue"), CmpOptr.FTRUE, p0, p1);
        InstCmp ffalse = assertCmp(inst("%ffalse"), CmpOptr.FFALSE, p0, p1);
        InstCmp foeq = assertCmp(inst("%foeq"), CmpOptr.FOEQ, p0, p1);
        InstCmp fone = assertCmp(inst("%fone"), CmpOptr.FONE, p0, p1);
        InstCmp folt = assertCmp(inst("%folt"), CmpOptr.FOLT, p0, p1);
        InstCmp fole = assertCmp(inst("%fole"), CmpOptr.FOLE, p0, p1);
        InstCmp fogt = assertCmp(inst("%fogt"), CmpOptr.FOGT, p0, p1);
        InstCmp foge = assertCmp(inst("%foge"), CmpOptr.FOGE, p0, p1);
        InstCmp fueq = assertCmp(inst("%fueq"), CmpOptr.FUEQ, p0, p1);
        InstCmp fune = assertCmp(inst("%fune"), CmpOptr.FUNE, p0, p1);
        InstCmp fult = assertCmp(inst("%fult"), CmpOptr.FULT, p0, p1);
        InstCmp fule = assertCmp(inst("%fule"), CmpOptr.FULE, p0, p1);
        InstCmp fugt = assertCmp(inst("%fugt"), CmpOptr.FUGT, p0, p1);
        InstCmp fuge = assertCmp(inst("%fuge"), CmpOptr.FUGE, p0, p1);

        for (InstCmp cmp : new InstCmp[] { ftrue, ffalse, foeq, fone, folt,
                fole, fogt, foge, fueq, fune, fult, fule, fugt, fuge }) {
            assertType(cmp.getOpndType(), uvm.type.Float.class);
            assertIntSize(cmp.getType(), 1);
        }
    }

    private InstConversion assertConversion(Instruction inst, ConvOptr optr,
            Object f, Object t, Value opnd) {
        InstConversion conv = assertType(inst, InstConversion.class);
        assertEquals(optr, conv.getOptr());
        if (f instanceof Integer) {
            assertIntSize(conv.getFromType(), (Integer) f);
        } else {
            assertType(conv.getFromType(), (Class<?>) f);
        }
        if (t instanceof Integer) {
            assertIntSize(conv.getToType(), (Integer) t);
        } else {
            assertType(conv.getToType(), (Class<?>) t);
        }
        assertEquals(opnd, conv.getOpnd());
        return conv;
    }

    @Test
    public void testConv() {
        loadFunc("@convTest");
        Parameter p0 = assertParameter(inst("%p0"), 0);
        Parameter p1 = assertParameter(inst("%p1"), 1);
        Parameter p2 = assertParameter(inst("%p2"), 2);
        Parameter p3 = assertParameter(inst("%p3"), 3);

        InstConversion trunc = assertConversion(inst("%trunc"), ConvOptr.TRUNC,
                64, 32, p1);
        assertIntSize(trunc.getType(), 32);
        InstConversion zext = assertConversion(inst("%zext"), ConvOptr.ZEXT,
                32, 64, p0);
        assertIntSize(zext.getType(), 64);
        InstConversion sext = assertConversion(inst("%sext"), ConvOptr.SEXT,
                32, 64, p0);
        assertIntSize(sext.getType(), 64);

        InstConversion fptrunc = assertConversion(inst("%fptrunc"),
                ConvOptr.FPTRUNC, uvm.type.Double.class, uvm.type.Float.class,
                p3);
        assertType(fptrunc.getType(), uvm.type.Float.class);
        InstConversion fpext = assertConversion(inst("%fpext"), ConvOptr.FPEXT,
                uvm.type.Float.class, uvm.type.Double.class, p2);
        assertType(fpext.getType(), uvm.type.Double.class);

        InstConversion fptoui = assertConversion(inst("%fptoui"),
                ConvOptr.FPTOUI, uvm.type.Double.class, 64, p3);
        assertIntSize(fptoui.getType(), 64);
        InstConversion fptosi = assertConversion(inst("%fptosi"),
                ConvOptr.FPTOSI, uvm.type.Double.class, 64, p3);
        assertIntSize(fptosi.getType(), 64);
        InstConversion uitofp = assertConversion(inst("%uitofp"),
                ConvOptr.UITOFP, 64, uvm.type.Double.class, p1);
        assertType(uitofp.getType(), uvm.type.Double.class);
        InstConversion sitofp = assertConversion(inst("%sitofp"),
                ConvOptr.SITOFP, 64, uvm.type.Double.class, p1);
        assertType(sitofp.getType(), uvm.type.Double.class);

        InstConversion bitcast0 = assertConversion(inst("%bitcast0"),
                ConvOptr.BITCAST, 32, uvm.type.Float.class, p0);
        assertType(bitcast0.getType(), uvm.type.Float.class);
        InstConversion bitcast1 = assertConversion(inst("%bitcast1"),
                ConvOptr.BITCAST, 64, uvm.type.Double.class, p1);
        assertType(bitcast1.getType(), uvm.type.Double.class);
        InstConversion bitcast2 = assertConversion(inst("%bitcast2"),
                ConvOptr.BITCAST, uvm.type.Float.class, 32, p2);
        assertIntSize(bitcast2.getType(), 32);
        InstConversion bitcast3 = assertConversion(inst("%bitcast3"),
                ConvOptr.BITCAST, uvm.type.Double.class, 64, p3);
        assertIntSize(bitcast3.getType(), 64);
    }

    @Test
    public void testRefCast() {
        loadFunc("@refCastTest");
        Parameter p0 = assertParameter(inst("%p0"), 0);
        Parameter p1 = assertParameter(inst("%p1"), 1);

        InstConversion refcast = assertConversion(inst("%refcast"),
                ConvOptr.REFCAST, Ref.class, Ref.class, p0);
        assertType(((Ref) refcast.getFromType()).getReferenced(),
                uvm.type.Void.class);
        assertIntSize(((Ref) refcast.getToType()).getReferenced(), 32);
        assertIntSize(((Ref) refcast.getType()).getReferenced(), 32);

        InstConversion irefcast = assertConversion(inst("%irefcast"),
                ConvOptr.IREFCAST, IRef.class, IRef.class, p1);
        assertType(((IRef) irefcast.getFromType()).getReferenced(),
                uvm.type.Void.class);
        assertIntSize(((IRef) irefcast.getToType()).getReferenced(), 64);
        assertIntSize(((IRef) irefcast.getType()).getReferenced(), 64);
    }

    private IntConstant assertIntConstant(Value val, long value) {
        IntConstant c = assertType(val, IntConstant.class);
        assertEquals(value, c.getValue());
        return c;
    }

    @Test
    public void testCtrlFlow() {
        loadFunc("@ctrlFlow");

        InstBranch br1 = assertType(inst("%br1"), InstBranch.class);
        assertEquals(bb("%head"), br1.getDest());
        assertNull(br1.getType());

        InstBranch2 br2 = assertType(inst("%br2"), InstBranch2.class);
        assertEquals(bb("%body"), br2.getIfTrue());
        assertNull(br2.getType());

        InstSwitch sw = assertType(inst("%switch"), InstSwitch.class);
        assertIntSize(sw.getOpndType(), 32);
        assertEquals(inst("%phi"), sw.getOpnd());
        assertEquals(bb("%other"), sw.getDefaultDest());

        for (Map.Entry<UseBox, BasicBlock> e : sw.getCases().entrySet()) {
            if (e.getKey().getDst() instanceof IntConstant) {
                IntConstant c = (IntConstant) e.getKey().getDst();
                if (c.getValue() == 1) {
                    assertEquals(bb("%one"), e.getValue());
                } else if (c.getValue() == 2) {
                    assertEquals(bb("%two"), e.getValue());
                } else {
                    fail("Unexpected case " + c.getValue());
                }
            } else {
                fail("Unexpected non-int-constant case");
            }
        }

        assertNull(sw.getType());

        InstPhi phi = assertType(inst("%phi"), InstPhi.class);
        assertIntSize(phi.getType(), 32);

        for (Map.Entry<BasicBlock, UseBox> e : phi.getValueMap().entrySet()) {
            if (e.getKey() == bb("%entry")) {
                assertIntConstant(e.getValue().getDst(), 0);
            } else if (e.getKey() == bb("%next")) {
                assertEquals(e.getValue().getDst(), inst("%i2"));
            } else {
                fail("Unexpected source");
            }
        }
    }

    @Test
    public void testCallee2() {
        loadFunc("@callee2");

        InstRet ret = assertType(inst("%ret"), InstRet.class);
        assertIntSize(ret.getRetType(), 64);
        assertEquals(inst("%rv"), ret.getRetVal());
        assertNull(ret.getType());
    }

    @Test
    public void testCallee3() {
        loadFunc("@callee3");

        InstThrow th = assertType(inst("%throw"), InstThrow.class);
        assertEquals(inst("%exc"), th.getException());
        assertNull(th.getType());
    }

    private void assertVoidVoidSig(FunctionSignature sig) {
        assertType(sig.getReturnType(), uvm.type.Void.class);
        assertTrue(sig.getParamTypes().isEmpty());
    }

    private void assertIIISig(FunctionSignature sig) {
        assertIntSize(sig.getReturnType(), 64);
        assertIntSize(sig.getParamTypes().get(0), 64);
        assertIntSize(sig.getParamTypes().get(1), 64);
    }

    @Test
    public void testCaller1() {
        loadFunc("@caller1");

        InstCall v1 = assertType(inst("%v1"), InstCall.class);
        assertVoidVoidSig(v1.getSig());
        assertEquals(constant("@callee1"), v1.getFunc());
        assertTrue(v1.getArgs().isEmpty());
        assertTrue(v1.getKeepAlives().isEmpty());
        assertType(v1.getType(), uvm.type.Void.class);

        InstCall v2 = assertType(inst("%v2"), InstCall.class);
        assertIIISig(v2.getSig());
        assertTrue(v2.getKeepAlives().isEmpty());
        assertIntSize(v2.getType(), 64);

        // Postpone argument tests to the last test case "testInference"

        InstInvoke v3 = assertType(inst("%v3"), InstInvoke.class);
        assertIIISig(v3.getSig());
        assertEquals(bb("%cont"), v3.getNor());
        assertEquals(bb("%catch"), v3.getExc());
        assertTrue(v3.getKeepAlives().isEmpty());
        assertIntSize(v3.getType(), 64);

        InstCall v4 = assertType(inst("%v4"), InstCall.class);
        assertVoidVoidSig(v4.getSig());
        assertEquals(inst("%v2"), v4.getKeepAlives().get(0).getDst());
        assertEquals(inst("%v3"), v4.getKeepAlives().get(1).getDst());
        assertType(v4.getType(), uvm.type.Void.class);

        InstInvoke v5 = assertType(inst("%v5"), InstInvoke.class);
        assertIIISig(v5.getSig());
        assertEquals(inst("%v3"), v5.getArgs().get(0).getDst());
        assertEquals(inst("%v3"), v5.getArgs().get(1).getDst());
        assertEquals(inst("%v2"), v5.getKeepAlives().get(0).getDst());
        assertIntSize(v5.getType(), 64);

        InstRetVoid retv = assertType(inst("%retv"), InstRetVoid.class);
        assertNull(retv.getType());
    }

    @Test
    public void testCaller2() {
        loadFunc("@caller2");
        InstTailCall tc = assertType(inst("%tc"), InstTailCall.class);
        assertIIISig(tc.getSig());
        assertEquals(constant("@callee2"), tc.getFunc());
        assertEquals(inst("%p0"), tc.getArgs().get(0).getDst());
        assertEquals(inst("%p1"), tc.getArgs().get(1).getDst());
        assertNull(tc.getType());
    }

    private FPConstant assertFPConstant(Value val, double value) {
        FPConstant c = assertType(val, FPConstant.class);
        assertEquals(value, c.getValue(), 0.001);
        return c;
    }

    @Test
    public void testAggregate() {
        loadFunc("@aggregate");
        InstExtractValue e0 = assertType(inst("%e0"), InstExtractValue.class);
        assertEquals(type("@sid"), e0.getStructType());
        assertEquals(0, e0.getIndex());
        assertEquals(constant("@sid1"), e0.getOpnd());
        assertIntSize(e0.getType(), 64);

        InstExtractValue e1 = assertType(inst("%e1"), InstExtractValue.class);
        assertEquals(type("@sid"), e1.getStructType());
        assertEquals(1, e1.getIndex());
        assertEquals(constant("@sid1"), e1.getOpnd());
        assertType(e1.getType(), uvm.type.Double.class);

        InstInsertValue i0 = assertType(inst("%i0"), InstInsertValue.class);
        assertEquals(type("@sid"), i0.getStructType());
        assertEquals(0, i0.getIndex());
        assertEquals(constant("@sid1"), i0.getOpnd());
        assertIntConstant(i0.getNewVal(), 40);
        assertType(i0.getType(), Struct.class);

        InstInsertValue i1 = assertType(inst("%i1"), InstInsertValue.class);
        assertEquals(type("@sid"), i1.getStructType());
        assertEquals(1, i1.getIndex());
        assertEquals(constant("@sid1"), i1.getOpnd());
        assertFPConstant(i1.getNewVal(), 40.0);
        assertType(i1.getType(), Struct.class);
    }

    @Test
    public void testMemOps() {
        loadFunc("@memops");
        Parameter p0 = assertParameter(inst("%p0"), 0);
        Parameter p1 = assertParameter(inst("%p1"), 1);

        InstNew new_ = assertType(inst("%new"), InstNew.class);
        assertIntSize(new_.getAllocType(), 64);
        Ref newType = assertType(new_.getType(), Ref.class);
        assertIntSize(newType.getReferenced(), 64);

        InstNewHybrid newhybrid = assertType(inst("%newhybrid"),
                InstNewHybrid.class);
        assertEquals(type("@hic"), newhybrid.getAllocType());
        assertEquals(inst("%p0"), newhybrid.getLength());
        Ref newHybridType = assertType(newhybrid.getType(), Ref.class);
        assertEquals(type("@hic"), newHybridType.getReferenced());

        InstAlloca alloca = assertType(inst("%alloca"), InstAlloca.class);
        assertIntSize(alloca.getAllocType(), 64);
        IRef allocaType = assertType(alloca.getType(), IRef.class);
        assertIntSize(allocaType.getReferenced(), 64);

        InstAllocaHybrid allocahybrid = assertType(inst("%allocahybrid"),
                InstAllocaHybrid.class);
        assertEquals(type("@hic"), allocahybrid.getAllocType());
        assertEquals(inst("%p0"), allocahybrid.getLength());
        IRef allocaHybridType = assertType(allocahybrid.getType(), IRef.class);
        assertEquals(type("@hic"), allocaHybridType.getReferenced());

        InstGetIRef getiref = assertType(inst("%getiref"), InstGetIRef.class);
        assertEquals(type("@sid"), getiref.getReferentType());
        assertEquals(inst("%new2"), getiref.getOpnd());
        IRef getirefType = assertType(getiref.getType(), IRef.class);
        assertEquals(type("@sid"), getirefType.getReferenced());

        InstGetFieldIRef getfieldiref = assertType(inst("%getfieldiref"),
                InstGetFieldIRef.class);
        assertEquals(type("@sid"), getfieldiref.getReferentType());
        assertEquals(0, getfieldiref.getIndex());
        assertEquals(inst("%getiref"), getfieldiref.getOpnd());
        IRef getfieldirefType = assertType(getfieldiref.getType(), IRef.class);
        assertIntSize(getfieldirefType.getReferenced(), 64);

        InstGetElemIRef getelemiref = assertType(inst("%getelemiref"),
                InstGetElemIRef.class);
        assertEquals(type("@al"), getelemiref.getReferentType());
        assertEquals(inst("%alloca2"), getelemiref.getOpnd());
        assertEquals(inst("%p1"), getelemiref.getIndex());
        IRef getelemirefType = assertType(getelemiref.getType(), IRef.class);
        assertIntSize(getelemirefType.getReferenced(), 64);

        InstShiftIRef shiftiref = assertType(inst("%shiftiref"),
                InstShiftIRef.class);
        assertIntSize(shiftiref.getReferentType(), 8);
        assertEquals(inst("%getvarpartiref"), shiftiref.getOpnd());
        assertEquals(inst("%p1"), shiftiref.getOffset());
        IRef shiftirefType = assertType(shiftiref.getType(), IRef.class);
        assertIntSize(shiftirefType.getReferenced(), 8);

        InstGetFixedPartIRef getfixedpartiref = assertType(
                inst("%getfixedpartiref"), InstGetFixedPartIRef.class);
        assertEquals(type("@hic"), getfixedpartiref.getReferentType());
        assertEquals(inst("%allocahybrid"), getfixedpartiref.getOpnd());
        IRef getfixedpartirefType = assertType(getfixedpartiref.getType(),
                IRef.class);
        assertIntSize(getfixedpartirefType.getReferenced(), 64);

        InstGetVarPartIRef getvarpartiref = assertType(inst("%getvarpartiref"),
                InstGetVarPartIRef.class);
        assertEquals(type("@hic"), getvarpartiref.getReferentType());
        assertEquals(inst("%allocahybrid"), getvarpartiref.getOpnd());
        IRef getvarpartirefType = assertType(getvarpartiref.getType(),
                IRef.class);
        assertIntSize(getvarpartirefType.getReferenced(), 8);

        InstLoad load = assertType(inst("%load"), InstLoad.class);
        assertEquals(AtomicOrdering.NOT_ATOMIC, load.getOrdering());
        assertIntSize(load.getReferentType(), 64);
        assertEquals(inst("%alloca"), load.getLocation());
        assertIntSize(load.getType(), 64);

        InstStore store = assertType(inst("%store"), InstStore.class);
        assertEquals(AtomicOrdering.NOT_ATOMIC, store.getOrdering());
        assertIntSize(store.getReferentType(), 64);
        assertEquals(inst("%alloca"), store.getLocation());
        assertIntConstant(store.getNewVal(), 42);
        assertNull(store.getType());

        InstCmpXchg cmpxchg = assertType(inst("%cmpxchg"), InstCmpXchg.class);
        assertEquals(AtomicOrdering.ACQUIRE, cmpxchg.getOrderingSucc());
        assertEquals(AtomicOrdering.MONOTONIC, cmpxchg.getOrderingFail());
        assertIntSize(cmpxchg.getReferentType(), 64);
        assertEquals(inst("%alloca"), cmpxchg.getLocation());
        assertIntConstant(cmpxchg.getExpected(), 42);
        assertIntConstant(cmpxchg.getDesired(), 0);
        assertIntSize(cmpxchg.getType(), 64);

        InstAtomicRMW atomicrmw = assertType(inst("%atomicrmw"),
                InstAtomicRMW.class);
        assertEquals(AtomicOrdering.ACQ_REL, atomicrmw.getOrdering());
        assertEquals(AtomicRMWOp.ADD, atomicrmw.getOptr());
        assertIntSize(atomicrmw.getReferentType(), 64);
        assertEquals(inst("%alloca"), atomicrmw.getLocation());
        assertIntConstant(atomicrmw.getOpnd(), 50);
        assertIntSize(atomicrmw.getType(), 64);

        InstFence fence = assertType(inst("%fence"), InstFence.class);
        assertEquals(AtomicOrdering.MONOTONIC, fence.getOrdering());
        assertNull(fence.getType());
    }

    @Test
    public void testMemOrder() {
        loadFunc("@memorder");

        assertEquals(AtomicOrdering.NOT_ATOMIC,
                ((InstLoad) inst("%l0")).getOrdering());
        assertEquals(AtomicOrdering.UNORDERED,
                ((InstLoad) inst("%l1")).getOrdering());
        assertEquals(AtomicOrdering.MONOTONIC,
                ((InstLoad) inst("%l2")).getOrdering());
        assertEquals(AtomicOrdering.ACQUIRE,
                ((InstFence) inst("%f3")).getOrdering());
        assertEquals(AtomicOrdering.RELEASE,
                ((InstFence) inst("%f4")).getOrdering());
        assertEquals(AtomicOrdering.ACQ_REL,
                ((InstFence) inst("%f5")).getOrdering());
        assertEquals(AtomicOrdering.SEQ_CST,
                ((InstLoad) inst("%l6")).getOrdering());
    }

    @Test
    public void testAtmicRMWOps() {
        loadFunc("@atomicrmwops");

        assertEquals(AtomicRMWOp.XCHG,
                ((InstAtomicRMW) inst("%old0")).getOptr());
        assertEquals(AtomicRMWOp.ADD, ((InstAtomicRMW) inst("%old1")).getOptr());
        assertEquals(AtomicRMWOp.SUB, ((InstAtomicRMW) inst("%old2")).getOptr());
        assertEquals(AtomicRMWOp.AND, ((InstAtomicRMW) inst("%old3")).getOptr());
        assertEquals(AtomicRMWOp.NAND,
                ((InstAtomicRMW) inst("%old4")).getOptr());
        assertEquals(AtomicRMWOp.OR, ((InstAtomicRMW) inst("%old5")).getOptr());
        assertEquals(AtomicRMWOp.XOR, ((InstAtomicRMW) inst("%old6")).getOptr());
        assertEquals(AtomicRMWOp.MAX, ((InstAtomicRMW) inst("%old7")).getOptr());
        assertEquals(AtomicRMWOp.MIN, ((InstAtomicRMW) inst("%old8")).getOptr());
        assertEquals(AtomicRMWOp.UMAX,
                ((InstAtomicRMW) inst("%old9")).getOptr());
        assertEquals(AtomicRMWOp.UMIN,
                ((InstAtomicRMW) inst("%olda")).getOptr());
    }

    @Test
    public void testTraps() {
        loadFunc("@traps");

        InstTrap tp = assertType(inst("%tp"), InstTrap.class);
        assertIntSize(tp.getType(), 32);
        assertEquals(bb("%trapcont"), tp.getNor());
        assertEquals(bb("%trapexc"), tp.getExc());
        assertEquals(inst("%b"), tp.getKeepAlives().get(0).getDst());
        assertEquals(inst("%wp"), tp.getKeepAlives().get(1).getDst());

        InstWatchPoint wp = assertType(inst("%wp"), InstWatchPoint.class);
        assertIntSize(wp.getType(), 64);
        assertEquals(bb("%body"), wp.getDisabled());
        assertEquals(bb("%wpcont"), wp.getNor());
        assertEquals(bb("%wpexc"), wp.getExc());
        assertEquals(inst("%a"), wp.getKeepAlives().get(0).getDst());

    }

    @Test
    public void testCCall() {
        loadFunc("@ccall");

        InstCCall ccall = assertType(inst("%rv"), InstCCall.class);
        assertEquals(CallConv.DEFAULT, ccall.getCallConv());
        assertType(ccall.getSig().getReturnType(), uvm.type.Void.class);
        assertType(ccall.getSig().getParamTypes().get(0), uvm.type.Double.class);
        assertEquals(inst("%p0"), ccall.getFunc());
        assertFPConstant(ccall.getArgs().get(0).getDst(), 3.14);
        assertType(ccall.getType(), uvm.type.Void.class);
    }

    @Test
    public void testStackAndIntrinsics() {
        loadFunc("@stack_and_intrinsic");

        InstNewStack ns = assertType(inst("%ns"), InstNewStack.class);
        assertIIISig(ns.getSig());
        assertEquals(constant("@callee2"), ns.getFunc());
        assertIntConstant(ns.getArgs().get(0).getDst(), 5);
        assertIntConstant(ns.getArgs().get(1).getDst(), 6);
        assertType(ns.getType(), Stack.class);

        IntrinsicFunction uvmSwapStack = IntrinsicFunctionFactory
                .getIntrinsicFunctionByName("@uvm.swap_stack");

        InstICall icall = assertType(inst("%i"), InstICall.class);
        assertEquals(uvmSwapStack, icall.getIntrinsicFunction());
        assertEquals(inst("%ns"), icall.getArgs().get(0).getDst());
        assertEquals(inst("%b"), icall.getKeepAlives().get(0).getDst());
        assertEquals(uvmSwapStack.getType(), icall.getType());

        IntrinsicFunction uvmKillStack = IntrinsicFunctionFactory
                .getIntrinsicFunctionByName("@uvm.kill_stack");

        InstIInvoke iinvoke = assertType(inst("%j"), InstIInvoke.class);
        assertEquals(uvmSwapStack, iinvoke.getIntrinsicFunction());
        assertEquals(inst("%ns"), iinvoke.getArgs().get(0).getDst());
        assertEquals(inst("%b"), iinvoke.getKeepAlives().get(0).getDst());
        assertEquals(inst("%c"), iinvoke.getKeepAlives().get(1).getDst());
        assertEquals(uvmKillStack.getType(), iinvoke.getType());
    }

    public void assertIntConstOf(Value val, int len, long value) {
        IntConstant c = assertType(val, IntConstant.class);
        assertEquals(len, c.getType().getSize());
        assertEquals(value, c.getValue());
    }

    public void assertFloatConstOf(Value val, double value) {
        FPConstant c = assertType(val, FPConstant.class);
        assertType(c.getType(), uvm.type.Float.class);
        assertEquals(value, c.getValue(), 0.001);
    }

    public void assertDoubleConstOf(Value val, double value) {
        FPConstant c = assertType(val, FPConstant.class);
        assertType(c.getType(), uvm.type.Double.class);
        assertEquals(value, c.getValue(), 0.001);
    }

    public void assertRainbow(List<UseBox> args, long v0, long v1, long v2,
            long v3, double v4, double v5) {
        assertIntConstOf(args.get(0).getDst(), 8, v0);
        assertIntConstOf(args.get(1).getDst(), 16, v1);
        assertIntConstOf(args.get(2).getDst(), 32, v2);
        assertIntConstOf(args.get(3).getDst(), 64, v3);
        assertFloatConstOf(args.get(4).getDst(), v4);
        assertDoubleConstOf(args.get(5).getDst(), v5);
    }

    public void assertRainbows(Value val, long v0, long v1, long v2, long v3,
            double v4, double v5) {
        StructConstant sc = assertType(val, StructConstant.class);
        assertIntConstOf(sc.getValues().get(0), 8, v0);
        assertIntConstOf(sc.getValues().get(1), 16, v1);
        assertIntConstOf(sc.getValues().get(2), 32, v2);
        assertIntConstOf(sc.getValues().get(3), 64, v3);
        assertFloatConstOf(sc.getValues().get(4), v4);
        assertDoubleConstOf(sc.getValues().get(5), v5);
    }

    @Test
    public void testInference() {
        loadFunc("@inference");

        assertIntConstOf(((InstBinOp) inst("%add")).getOp1(), 8, 41);
        assertIntConstOf(((InstBinOp) inst("%add")).getOp2(), 8, 42);
        assertIntConstOf(((InstBinOp) inst("%sub")).getOp1(), 16, 43);
        assertIntConstOf(((InstBinOp) inst("%sub")).getOp2(), 16, 44);
        assertIntConstOf(((InstBinOp) inst("%mul")).getOp1(), 32, 45);
        assertIntConstOf(((InstBinOp) inst("%mul")).getOp2(), 32, 46);
        assertIntConstOf(((InstBinOp) inst("%udiv")).getOp1(), 64, 47);
        assertIntConstOf(((InstBinOp) inst("%udiv")).getOp2(), 64, 48);
        assertFloatConstOf(((InstBinOp) inst("%fadd")).getOp1(), 49.0);
        assertFloatConstOf(((InstBinOp) inst("%fadd")).getOp2(), 50.0);
        assertDoubleConstOf(((InstBinOp) inst("%fsub")).getOp1(), 51.0);
        assertDoubleConstOf(((InstBinOp) inst("%fsub")).getOp2(), 52.0);

        assertIntConstOf(((InstCmp) inst("%eq")).getOp1(), 64, 53);
        assertIntConstOf(((InstCmp) inst("%eq")).getOp2(), 64, 54);

        assertDoubleConstOf(((InstCmp) inst("%fueq")).getOp1(), 55.0);
        assertDoubleConstOf(((InstCmp) inst("%fueq")).getOp2(), 56.0);

        assertIntConstOf(((InstConversion) inst("%trunc")).getOpnd(), 64, 57);
        assertDoubleConstOf(((InstConversion) inst("%fptrunc")).getOpnd(), 58.0);

        NullConstant refcastc = assertType(
                ((InstConversion) inst("%refcast")).getOpnd(),
                NullConstant.class);
        assertType(((Ref) refcastc.getType()).getReferenced(),
                uvm.type.Void.class);
        NullConstant irefcastc = assertType(
                ((InstConversion) inst("%irefcast")).getOpnd(),
                NullConstant.class);
        assertType(((IRef) irefcastc.getType()).getReferenced(),
                uvm.type.Void.class);

        assertIntConstOf(((InstSelect) inst("%select")).getCond(), 1, 1);
        assertDoubleConstOf(((InstSelect) inst("%select")).getIfTrue(), 59.0);
        assertDoubleConstOf(((InstSelect) inst("%select")).getIfFalse(), 60.0);

        assertIntConstOf(((InstSwitch) inst("%switch")).getOpnd(), 32, 61);
        for (UseBox ub : ((InstSwitch) inst("%switch")).getCases().keySet()) {
            assertIntConstOf(ub.getDst(), 32, 62);
        }

        for (UseBox ub : ((InstPhi) inst("%phi")).getValueMap().values()) {
            assertIntConstOf(ub.getDst(), 32, 63);
        }

        assertRainbow(((HasArgs) inst("%call")).getArgs(), 64, 65, 66, 67,
                68.0, 69.0);
        assertRainbow(((HasArgs) inst("%invoke")).getArgs(), 70, 71, 72, 73,
                74.0, 75.0);
        assertRainbow(((HasArgs) inst("%tailcall")).getArgs(), 76, 77, 78, 79,
                80.0, 81.0);

        assertRainbows(((InstExtractValue) inst("%extractvalue")).getOpnd(),
                82, 83, 84, 85, 86.0, 87.0);
        assertRainbows(((InstInsertValue) inst("%insertvalue")).getOpnd(), 88,
                89, 90, 91, 92.0, 93.0);
        assertIntConstOf(((InstInsertValue) inst("%insertvalue")).getNewVal(),
                8, 94);

        assertIntConstOf(((InstNewHybrid) inst("%newhybrid")).getLength(), 64,
                95);
        assertIntConstOf(
                ((InstAllocaHybrid) inst("%allocahybrid")).getLength(), 64, 96);
        assertIntConstOf(((InstGetElemIRef) inst("%getelemiref")).getIndex(),
                64, 97);
        assertIntConstOf(((InstShiftIRef) inst("%shiftiref")).getOffset(), 64,
                98);

        assertIntConstOf(((InstStore) inst("%store")).getNewVal(), 32, 99);
        assertIntConstOf(((InstCmpXchg) inst("%cmpxchg")).getExpected(), 32,
                100);
        assertIntConstOf(((InstCmpXchg) inst("%cmpxchg")).getDesired(), 32, 101);
        assertIntConstOf(((InstAtomicRMW) inst("%atomicrmw")).getOpnd(), 32,
                102);

        assertRainbow(((HasArgs) inst("%ccall")).getArgs(), 103, 104, 105, 106,
                107.0, 108.0);
        assertRainbow(((HasArgs) inst("%newstack")).getArgs(), 109, 110, 111,
                112, 113.0, 114.0);

    }
}
