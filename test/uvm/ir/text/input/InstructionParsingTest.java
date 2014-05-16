package uvm.ir.text.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import uvm.BasicBlock;
import uvm.CFG;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.ifunc.IFunc;
import uvm.ifunc.IFuncFactory;
import uvm.ssavalue.AtomicOrdering;
import uvm.ssavalue.AtomicRMWOp;
import uvm.ssavalue.BinOptr;
import uvm.ssavalue.CallConv;
import uvm.ssavalue.CmpOptr;
import uvm.ssavalue.ConvOptr;
import uvm.ssavalue.DoubleConstant;
import uvm.ssavalue.FloatConstant;
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
import uvm.type.Func;
import uvm.type.IRef;
import uvm.type.Int;
import uvm.type.Ref;
import uvm.type.Stack;
import uvm.type.Struct;
import uvm.type.Type;

public class InstructionParsingTest extends BundleTester {

    @Override
    protected String bundleName() {
        return "tests/uvm-parsing-test/instructions.uir";
    }

    private Function curFunc;
    private CFG curCFG;

    private BasicBlock bb(String name) {
        BasicBlock bb = curCFG.getBBNs().getByName(name);
        if (bb == null) {
            fail("No such bb " + name);
        }
        return bb;
    }

    private Instruction inst(String name) {
        Instruction inst = curCFG.getInstNs().getByName(name);
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
    public void setUp() throws Exception {
        super.setUp();
        curFunc = null;
        curCFG = null;
    }

    private Parameter assertParameter(Instruction inst, int index) {
        Parameter param = assertType(Parameter.class, inst);
        assertEquals(index, param.getParamIndex());
        return param;
    }

    private InstBinOp assertBinOp(Instruction inst, BinOptr optr, Value op1,
            Value op2) {
        InstBinOp binOp = assertType(InstBinOp.class, inst);
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
            assertIntSize(32, binOp.getType());
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
            assertType(uvm.type.Double.class, binOp.getType());
        }
    }

    private InstCmp assertCmp(Instruction inst, CmpOptr optr, Value op1,
            Value op2) {
        InstCmp cmp = assertType(InstCmp.class, inst);
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
            assertIntSize(64, cmp.getOpndType());
            assertIntSize(1, cmp.getType());
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
            assertType(uvm.type.Float.class, cmp.getOpndType());
            assertIntSize(1, cmp.getType());
        }
    }

    private InstConversion assertConversion(Instruction inst, ConvOptr optr,
            Object f, Object t, Value opnd) {
        InstConversion conv = assertType(InstConversion.class, inst);
        assertEquals(optr, conv.getOptr());
        if (f instanceof Integer) {
            assertIntSize((Integer) f, conv.getFromType());
        } else {
            assertType((Class<?>) f, conv.getFromType());
        }
        if (t instanceof Integer) {
            assertIntSize((Integer) t, conv.getToType());
        } else {
            assertType((Class<?>) t, conv.getToType());
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
        assertIntSize(32, trunc.getType());
        InstConversion zext = assertConversion(inst("%zext"), ConvOptr.ZEXT,
                32, 64, p0);
        assertIntSize(64, zext.getType());
        InstConversion sext = assertConversion(inst("%sext"), ConvOptr.SEXT,
                32, 64, p0);
        assertIntSize(64, sext.getType());

        InstConversion fptrunc = assertConversion(inst("%fptrunc"),
                ConvOptr.FPTRUNC, uvm.type.Double.class, uvm.type.Float.class,
                p3);
        assertType(uvm.type.Float.class, fptrunc.getType());
        InstConversion fpext = assertConversion(inst("%fpext"), ConvOptr.FPEXT,
                uvm.type.Float.class, uvm.type.Double.class, p2);
        assertType(uvm.type.Double.class, fpext.getType());

        InstConversion fptoui = assertConversion(inst("%fptoui"),
                ConvOptr.FPTOUI, uvm.type.Double.class, 64, p3);
        assertIntSize(64, fptoui.getType());
        InstConversion fptosi = assertConversion(inst("%fptosi"),
                ConvOptr.FPTOSI, uvm.type.Double.class, 64, p3);
        assertIntSize(64, fptosi.getType());
        InstConversion uitofp = assertConversion(inst("%uitofp"),
                ConvOptr.UITOFP, 64, uvm.type.Double.class, p1);
        assertType(uvm.type.Double.class, uitofp.getType());
        InstConversion sitofp = assertConversion(inst("%sitofp"),
                ConvOptr.SITOFP, 64, uvm.type.Double.class, p1);
        assertType(uvm.type.Double.class, sitofp.getType());

        InstConversion bitcast0 = assertConversion(inst("%bitcast0"),
                ConvOptr.BITCAST, 32, uvm.type.Float.class, p0);
        assertType(uvm.type.Float.class, bitcast0.getType());
        InstConversion bitcast1 = assertConversion(inst("%bitcast1"),
                ConvOptr.BITCAST, 64, uvm.type.Double.class, p1);
        assertType(uvm.type.Double.class, bitcast1.getType());
        InstConversion bitcast2 = assertConversion(inst("%bitcast2"),
                ConvOptr.BITCAST, uvm.type.Float.class, 32, p2);
        assertIntSize(32, bitcast2.getType());
        InstConversion bitcast3 = assertConversion(inst("%bitcast3"),
                ConvOptr.BITCAST, uvm.type.Double.class, 64, p3);
        assertIntSize(64, bitcast3.getType());
    }

    @Test
    public void testRefCast() {
        loadFunc("@refCastTest");
        Parameter p0 = assertParameter(inst("%p0"), 0);
        Parameter p1 = assertParameter(inst("%p1"), 1);
        Parameter p2 = assertParameter(inst("%p2"), 2);

        InstConversion refcast = assertConversion(inst("%refcast"),
                ConvOptr.REFCAST, Ref.class, Ref.class, p0);
        assertType(uvm.type.Void.class,
                ((Ref) refcast.getFromType()).getReferenced());
        assertIntSize(32, ((Ref) refcast.getToType()).getReferenced());
        assertIntSize(32, ((Ref) refcast.getType()).getReferenced());

        InstConversion irefcast = assertConversion(inst("%irefcast"),
                ConvOptr.IREFCAST, IRef.class, IRef.class, p1);
        assertType(uvm.type.Void.class,
                ((IRef) irefcast.getFromType()).getReferenced());
        assertIntSize(64, ((IRef) irefcast.getToType()).getReferenced());
        assertIntSize(64, ((IRef) irefcast.getType()).getReferenced());

        InstConversion funccast = assertConversion(inst("%funccast"),
                ConvOptr.FUNCCAST, Func.class, Func.class, p2);
        assertVVFunc(funccast.getFromType());
        assertIIIFunc(funccast.getToType());
    }

    private void assertIIIFunc(Type type) {
        Func fromFunc = assertType(Func.class, type);
        FunctionSignature sig = fromFunc.getSig();
        assertIIISig(sig);
    }

    private void assertVVFunc(Type type) {
        Func fromFunc = assertType(Func.class, type);
        FunctionSignature sig = fromFunc.getSig();
        assertVVSig(sig);
    }

    private void assertVVSig(FunctionSignature sig) {
        assertType(uvm.type.Void.class, sig.getReturnType());
        assertTrue(sig.getParamTypes().isEmpty());
    }

    private IntConstant assertIntConstant(Value val, long value) {
        IntConstant c = assertType(IntConstant.class, val);
        assertEquals(value, c.getValue());
        return c;
    }

    @Test
    public void testCtrlFlow() {
        loadFunc("@ctrlFlow");

        InstBranch br1 = assertType(InstBranch.class, inst("%br1"));
        assertEquals(bb("%head"), br1.getDest());
        assertNull(br1.getType());

        InstBranch2 br2 = assertType(InstBranch2.class, inst("%br2"));
        assertEquals(bb("%body"), br2.getIfTrue());
        assertNull(br2.getType());

        InstSwitch sw = assertType(InstSwitch.class, inst("%switch"));
        assertIntSize(32, sw.getOpndType());
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

        InstPhi phi = assertType(InstPhi.class, inst("%phi"));
        assertIntSize(32, phi.getType());

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

        InstRet ret = assertType(InstRet.class, inst("%ret"));
        assertIntSize(64, ret.getRetType());
        assertEquals(inst("%rv"), ret.getRetVal());
        assertNull(ret.getType());
    }

    @Test
    public void testCallee3() {
        loadFunc("@callee3");

        InstThrow th = assertType(InstThrow.class, inst("%throw"));
        assertEquals(inst("%exc"), th.getException());
        assertNull(th.getType());
    }

    private void assertVoidVoidSig(FunctionSignature sig) {
        assertType(uvm.type.Void.class, sig.getReturnType());
        assertTrue(sig.getParamTypes().isEmpty());
    }

    private void assertIIISig(FunctionSignature sig) {
        assertIntSize(64, sig.getReturnType());
        assertIntSize(64, sig.getParamTypes().get(0));
        assertIntSize(64, sig.getParamTypes().get(1));
    }

    @Test
    public void testCaller1() {
        loadFunc("@caller1");

        InstCall v1 = assertType(InstCall.class, inst("%v1"));
        assertVoidVoidSig(v1.getSig());
        assertEquals(constant("@callee1"), v1.getFunc());
        assertTrue(v1.getArgs().isEmpty());
        assertTrue(v1.getKeepAlives().isEmpty());
        assertType(uvm.type.Void.class, v1.getType());

        InstCall v2 = assertType(InstCall.class, inst("%v2"));
        assertIIISig(v2.getSig());
        assertTrue(v2.getKeepAlives().isEmpty());
        assertIntSize(64, v2.getType());

        // Postpone argument tests to the last test case "testInference"

        InstInvoke v3 = assertType(InstInvoke.class, inst("%v3"));
        assertIIISig(v3.getSig());
        assertEquals(bb("%cont"), v3.getNor());
        assertEquals(bb("%catch"), v3.getExc());
        assertTrue(v3.getKeepAlives().isEmpty());
        assertIntSize(64, v3.getType());

        InstCall v4 = assertType(InstCall.class, inst("%v4"));
        assertVoidVoidSig(v4.getSig());
        assertEquals(inst("%v2"), v4.getKeepAlives().get(0).getDst());
        assertEquals(inst("%v3"), v4.getKeepAlives().get(1).getDst());
        assertType(uvm.type.Void.class, v4.getType());

        InstInvoke v5 = assertType(InstInvoke.class, inst("%v5"));
        assertIIISig(v5.getSig());
        assertEquals(inst("%v3"), v5.getArgs().get(0).getDst());
        assertEquals(inst("%v3"), v5.getArgs().get(1).getDst());
        assertEquals(inst("%v2"), v5.getKeepAlives().get(0).getDst());
        assertIntSize(64, v5.getType());

        InstRetVoid retv = assertType(InstRetVoid.class, inst("%retv"));
        assertNull(retv.getType());
    }

    @Test
    public void testCaller2() {
        loadFunc("@caller2");
        InstTailCall tc = assertType(InstTailCall.class, inst("%tc"));
        assertIIISig(tc.getSig());
        assertEquals(constant("@callee2"), tc.getFunc());
        assertEquals(inst("%p0"), tc.getArgs().get(0).getDst());
        assertEquals(inst("%p1"), tc.getArgs().get(1).getDst());
        assertNull(tc.getType());
    }

    private FloatConstant assertFloatConstant(Value val, float value) {
        FloatConstant c = assertType(FloatConstant.class, val);
        assertEquals(value, c.getValue(), 0.001f);
        return c;
    }

    private DoubleConstant assertDoubleConstant(Value val, double value) {
        DoubleConstant c = assertType(DoubleConstant.class, val);
        assertEquals(value, c.getValue(), 0.001d);
        return c;
    }

    @Test
    public void testAggregate() {
        loadFunc("@aggregate");
        InstExtractValue e0 = assertType(InstExtractValue.class, inst("%e0"));
        assertEquals(type("@sid"), e0.getStructType());
        assertEquals(0, e0.getIndex());
        assertEquals(constant("@sid1"), e0.getOpnd());
        assertIntSize(64, e0.getType());

        InstExtractValue e1 = assertType(InstExtractValue.class, inst("%e1"));
        assertEquals(type("@sid"), e1.getStructType());
        assertEquals(1, e1.getIndex());
        assertEquals(constant("@sid1"), e1.getOpnd());
        assertType(uvm.type.Double.class, e1.getType());

        InstInsertValue i0 = assertType(InstInsertValue.class, inst("%i0"));
        assertEquals(type("@sid"), i0.getStructType());
        assertEquals(0, i0.getIndex());
        assertEquals(constant("@sid1"), i0.getOpnd());
        assertIntConstant(i0.getNewVal(), 40);
        assertType(Struct.class, i0.getType());

        InstInsertValue i1 = assertType(InstInsertValue.class, inst("%i1"));
        assertEquals(type("@sid"), i1.getStructType());
        assertEquals(1, i1.getIndex());
        assertEquals(constant("@sid1"), i1.getOpnd());
        assertDoubleConstant(i1.getNewVal(), 40.0d);
        assertType(Struct.class, i1.getType());
    }

    @Test
    public void testMemOps() {
        loadFunc("@memops");
        Parameter p0 = assertParameter(inst("%p0"), 0);
        Parameter p1 = assertParameter(inst("%p1"), 1);

        InstNew new_ = assertType(InstNew.class, inst("%new"));
        assertIntSize(64, new_.getAllocType());
        Ref newType = assertType(Ref.class, new_.getType());
        assertIntSize(64, newType.getReferenced());

        InstNewHybrid newhybrid = assertType(InstNewHybrid.class,
                inst("%newhybrid"));
        assertEquals(type("@hic"), newhybrid.getAllocType());
        assertEquals(inst("%p0"), newhybrid.getLength());
        Ref newHybridType = assertType(Ref.class, newhybrid.getType());
        assertEquals(type("@hic"), newHybridType.getReferenced());

        InstAlloca alloca = assertType(InstAlloca.class, inst("%alloca"));
        assertIntSize(64, alloca.getAllocType());
        IRef allocaType = assertType(IRef.class, alloca.getType());
        assertIntSize(64, allocaType.getReferenced());

        InstAllocaHybrid allocahybrid = assertType(InstAllocaHybrid.class,
                inst("%allocahybrid"));
        assertEquals(type("@hic"), allocahybrid.getAllocType());
        assertEquals(inst("%p0"), allocahybrid.getLength());
        IRef allocaHybridType = assertType(IRef.class, allocahybrid.getType());
        assertEquals(type("@hic"), allocaHybridType.getReferenced());

        InstGetIRef getiref = assertType(InstGetIRef.class, inst("%getiref"));
        assertEquals(type("@sid"), getiref.getReferentType());
        assertEquals(inst("%new2"), getiref.getOpnd());
        IRef getirefType = assertType(IRef.class, getiref.getType());
        assertEquals(type("@sid"), getirefType.getReferenced());

        InstGetFieldIRef getfieldiref = assertType(InstGetFieldIRef.class,
                inst("%getfieldiref"));
        assertEquals(type("@sid"), getfieldiref.getReferentType());
        assertEquals(0, getfieldiref.getIndex());
        assertEquals(inst("%getiref"), getfieldiref.getOpnd());
        IRef getfieldirefType = assertType(IRef.class, getfieldiref.getType());
        assertIntSize(64, getfieldirefType.getReferenced());

        InstGetElemIRef getelemiref = assertType(InstGetElemIRef.class,
                inst("%getelemiref"));
        assertEquals(type("@al"), getelemiref.getReferentType());
        assertEquals(inst("%alloca2"), getelemiref.getOpnd());
        assertEquals(inst("%p1"), getelemiref.getIndex());
        IRef getelemirefType = assertType(IRef.class, getelemiref.getType());
        assertIntSize(64, getelemirefType.getReferenced());

        InstShiftIRef shiftiref = assertType(InstShiftIRef.class,
                inst("%shiftiref"));
        assertIntSize(8, shiftiref.getReferentType());
        assertEquals(inst("%getvarpartiref"), shiftiref.getOpnd());
        assertEquals(inst("%p1"), shiftiref.getOffset());
        IRef shiftirefType = assertType(IRef.class, shiftiref.getType());
        assertIntSize(8, shiftirefType.getReferenced());

        InstGetFixedPartIRef getfixedpartiref = assertType(
                InstGetFixedPartIRef.class, inst("%getfixedpartiref"));
        assertEquals(type("@hic"), getfixedpartiref.getReferentType());
        assertEquals(inst("%allocahybrid"), getfixedpartiref.getOpnd());
        IRef getfixedpartirefType = assertType(IRef.class,
                getfixedpartiref.getType());
        assertIntSize(64, getfixedpartirefType.getReferenced());

        InstGetVarPartIRef getvarpartiref = assertType(
                InstGetVarPartIRef.class, inst("%getvarpartiref"));
        assertEquals(type("@hic"), getvarpartiref.getReferentType());
        assertEquals(inst("%allocahybrid"), getvarpartiref.getOpnd());
        IRef getvarpartirefType = assertType(IRef.class,
                getvarpartiref.getType());
        assertIntSize(8, getvarpartirefType.getReferenced());

        InstLoad load = assertType(InstLoad.class, inst("%load"));
        assertEquals(AtomicOrdering.NOT_ATOMIC, load.getOrdering());
        assertIntSize(64, load.getReferentType());
        assertEquals(inst("%alloca"), load.getLocation());
        assertIntSize(64, load.getType());

        InstStore store = assertType(InstStore.class, inst("%store"));
        assertEquals(AtomicOrdering.NOT_ATOMIC, store.getOrdering());
        assertIntSize(64, store.getReferentType());
        assertEquals(inst("%alloca"), store.getLocation());
        assertIntConstant(store.getNewVal(), 42);
        assertNull(store.getType());

        InstCmpXchg cmpxchg = assertType(InstCmpXchg.class, inst("%cmpxchg"));
        assertEquals(AtomicOrdering.ACQUIRE, cmpxchg.getOrderingSucc());
        assertEquals(AtomicOrdering.MONOTONIC, cmpxchg.getOrderingFail());
        assertIntSize(64, cmpxchg.getReferentType());
        assertEquals(inst("%alloca"), cmpxchg.getLocation());
        assertIntConstant(cmpxchg.getExpected(), 42);
        assertIntConstant(cmpxchg.getDesired(), 0);
        assertIntSize(64, cmpxchg.getType());

        InstAtomicRMW atomicrmw = assertType(InstAtomicRMW.class,
                inst("%atomicrmw"));
        assertEquals(AtomicOrdering.ACQ_REL, atomicrmw.getOrdering());
        assertEquals(AtomicRMWOp.ADD, atomicrmw.getOptr());
        assertIntSize(64, atomicrmw.getReferentType());
        assertEquals(inst("%alloca"), atomicrmw.getLocation());
        assertIntConstant(atomicrmw.getOpnd(), 50);
        assertIntSize(64, atomicrmw.getType());

        InstFence fence = assertType(InstFence.class, inst("%fence"));
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

        InstTrap tp = assertType(InstTrap.class, inst("%tp"));
        assertIntSize(32, tp.getType());
        assertEquals(bb("%trapcont"), tp.getNor());
        assertEquals(bb("%trapexc"), tp.getExc());
        assertEquals(inst("%b"), tp.getKeepAlives().get(0).getDst());
        assertEquals(inst("%wp"), tp.getKeepAlives().get(1).getDst());

        InstWatchPoint wp = assertType(InstWatchPoint.class, inst("%wp"));
        assertIntSize(64, wp.getType());
        assertEquals(bb("%body"), wp.getDisabled());
        assertEquals(bb("%wpcont"), wp.getNor());
        assertEquals(bb("%wpexc"), wp.getExc());
        assertEquals(inst("%a"), wp.getKeepAlives().get(0).getDst());

    }

    @Test
    public void testCCall() {
        loadFunc("@ccall");

        InstCCall ccall = assertType(InstCCall.class, inst("%rv"));
        assertEquals(CallConv.DEFAULT, ccall.getCallConv());
        assertType(uvm.type.Void.class, ccall.getSig().getReturnType());
        assertType(uvm.type.Double.class, ccall.getSig().getParamTypes().get(0));
        assertEquals(inst("%p0"), ccall.getFunc());
        assertDoubleConstant(ccall.getArgs().get(0).getDst(), 3.14);
        assertType(uvm.type.Void.class, ccall.getType());
    }

    @Test
    public void testStackAndIntrinsics() {
        loadFunc("@stack_and_intrinsic");

        InstNewStack ns = assertType(InstNewStack.class, inst("%ns"));
        assertIIISig(ns.getSig());
        assertEquals(constant("@callee2"), ns.getFunc());
        assertIntConstant(ns.getArgs().get(0).getDst(), 5);
        assertIntConstant(ns.getArgs().get(1).getDst(), 6);
        assertType(Stack.class, ns.getType());

        IFunc uvmSwapStack = IFuncFactory.getIFuncByName("@uvm.swap_stack");

        InstICall icall = assertType(InstICall.class, inst("%i"));
        assertEquals(uvmSwapStack, icall.getIFunc());
        assertEquals(inst("%ns"), icall.getArgs().get(0).getDst());
        assertEquals(inst("%b"), icall.getKeepAlives().get(0).getDst());
        assertEquals(uvmSwapStack.getType(), icall.getType());

        IFunc uvmKillStack = IFuncFactory.getIFuncByName("@uvm.kill_stack");

        InstIInvoke iinvoke = assertType(InstIInvoke.class, inst("%j"));
        assertEquals(uvmKillStack, iinvoke.getIFunc());
        assertEquals(inst("%ns"), iinvoke.getArgs().get(0).getDst());
        assertEquals(inst("%b"), iinvoke.getKeepAlives().get(0).getDst());
        assertEquals(inst("%c"), iinvoke.getKeepAlives().get(1).getDst());
        assertEquals(uvmKillStack.getType(), iinvoke.getType());
    }

    public void assertIntConstOf(Value val, int len, long value) {
        IntConstant c = assertType(IntConstant.class, val);
        assertEquals(len, c.getType().getSize());
        assertEquals(value, c.getValue());
    }

    public void assertFloatConstOf(Value val, double value) {
        FloatConstant c = assertType(FloatConstant.class, val);
        assertType(uvm.type.Float.class, c.getType());
        assertEquals(value, c.getValue(), 0.001f);
    }

    public void assertDoubleConstOf(Value val, double value) {
        DoubleConstant c = assertType(DoubleConstant.class, val);
        assertType(uvm.type.Double.class, c.getType());
        assertEquals(value, c.getValue(), 0.001d);
    }

    public void assertRainbow(List<UseBox> args, long v0, long v1, long v2,
            long v3, float v4, double v5) {
        assertIntConstOf(args.get(0).getDst(), 8, v0);
        assertIntConstOf(args.get(1).getDst(), 16, v1);
        assertIntConstOf(args.get(2).getDst(), 32, v2);
        assertIntConstOf(args.get(3).getDst(), 64, v3);
        assertFloatConstOf(args.get(4).getDst(), v4);
        assertDoubleConstOf(args.get(5).getDst(), v5);
    }

    public void assertRainbows(Value val, long v0, long v1, long v2, long v3,
            float v4, double v5) {
        StructConstant sc = assertType(StructConstant.class, val);
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
        assertFloatConstOf(((InstBinOp) inst("%fadd")).getOp1(), 49.0f);
        assertFloatConstOf(((InstBinOp) inst("%fadd")).getOp2(), 50.0f);
        assertDoubleConstOf(((InstBinOp) inst("%fsub")).getOp1(), 51.0d);
        assertDoubleConstOf(((InstBinOp) inst("%fsub")).getOp2(), 52.0d);

        assertIntConstOf(((InstCmp) inst("%eq")).getOp1(), 64, 53);
        assertIntConstOf(((InstCmp) inst("%eq")).getOp2(), 64, 54);

        assertDoubleConstOf(((InstCmp) inst("%fueq")).getOp1(), 55.0d);
        assertDoubleConstOf(((InstCmp) inst("%fueq")).getOp2(), 56.0d);

        assertIntConstOf(((InstConversion) inst("%trunc")).getOpnd(), 64, 57);
        assertDoubleConstOf(((InstConversion) inst("%fptrunc")).getOpnd(),
                58.0d);

        NullConstant refcastc = assertType(NullConstant.class,
                ((InstConversion) inst("%refcast")).getOpnd());
        assertType(uvm.type.Void.class,
                ((Ref) refcastc.getType()).getReferenced());
        NullConstant irefcastc = assertType(NullConstant.class,
                ((InstConversion) inst("%irefcast")).getOpnd());
        assertType(uvm.type.Void.class,
                ((IRef) irefcastc.getType()).getReferenced());

        assertIntConstOf(((InstSelect) inst("%select")).getCond(), 1, 1);
        assertDoubleConstOf(((InstSelect) inst("%select")).getIfTrue(), 59.0d);
        assertDoubleConstOf(((InstSelect) inst("%select")).getIfFalse(), 60.0d);

        assertIntConstOf(((InstSwitch) inst("%switch")).getOpnd(), 32, 61);
        for (UseBox ub : ((InstSwitch) inst("%switch")).getCases().keySet()) {
            assertIntConstOf(ub.getDst(), 32, 62);
        }

        for (UseBox ub : ((InstPhi) inst("%phi")).getValueMap().values()) {
            assertIntConstOf(ub.getDst(), 32, 63);
        }

        assertRainbow(((HasArgs) inst("%call")).getArgs(), 64, 65, 66, 67,
                68.0f, 69.0d);
        assertRainbow(((HasArgs) inst("%invoke")).getArgs(), 70, 71, 72, 73,
                74.0f, 75.0d);
        assertRainbow(((HasArgs) inst("%tailcall")).getArgs(), 76, 77, 78, 79,
                80.0f, 81.0d);

        assertRainbows(((InstExtractValue) inst("%extractvalue")).getOpnd(),
                82, 83, 84, 85, 86.0f, 87.0d);
        assertRainbows(((InstInsertValue) inst("%insertvalue")).getOpnd(), 88,
                89, 90, 91, 92.0f, 93.0d);
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
                107.0f, 108.0d);
        assertRainbow(((HasArgs) inst("%newstack")).getArgs(), 109, 110, 111,
                112, 113.0f, 114.0d);

    }
}
