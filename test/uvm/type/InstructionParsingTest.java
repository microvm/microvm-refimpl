package uvm.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uvm.type.TestingHelper.parseUir;

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
import uvm.ssavalue.BinOptr;
import uvm.ssavalue.CmpOptr;
import uvm.ssavalue.Constant;
import uvm.ssavalue.ConvOptr;
import uvm.ssavalue.FPConstant;
import uvm.ssavalue.InstBinOp;
import uvm.ssavalue.InstBranch;
import uvm.ssavalue.InstBranch2;
import uvm.ssavalue.InstCall;
import uvm.ssavalue.InstCmp;
import uvm.ssavalue.InstConversion;
import uvm.ssavalue.InstExtractValue;
import uvm.ssavalue.InstInsertValue;
import uvm.ssavalue.InstInvoke;
import uvm.ssavalue.InstPhi;
import uvm.ssavalue.InstRet;
import uvm.ssavalue.InstRetVoid;
import uvm.ssavalue.InstSwitch;
import uvm.ssavalue.InstTailCall;
import uvm.ssavalue.InstThrow;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.Parameter;
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

}
