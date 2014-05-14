package uvm.ir.textinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
import uvm.ssavalue.DoubleConstant;
import uvm.ssavalue.FloatConstant;
import uvm.ssavalue.FunctionConstant;
import uvm.ssavalue.GlobalDataConstant;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.StructConstant;
import uvm.type.Func;
import uvm.type.IRef;
import uvm.type.Ref;
import uvm.type.Struct;
import uvm.type.Void;
import uvm.type.WeakRef;

public class ConstantParsingTest extends BundleTester {
    @Override
    protected String bundleName() {
        return "tests/uvm-parsing-test/constants.uir";
    }

    @Test
    public void testParsing() {
        IntConstant ci8 = assertType(IntConstant.class, constant("@ci8"));
        assertIntSize(8, ci8.getType());
        assertEquals(127L, ci8.getValue());
        IntConstant ci16 = assertType(IntConstant.class, constant("@ci16"));
        assertIntSize(16, ci16.getType());
        assertEquals(32767L, ci16.getValue());
        IntConstant ci32 = assertType(IntConstant.class, constant("@ci32"));
        assertIntSize(32, ci32.getType());
        assertEquals(2147483647L, ci32.getValue());
        IntConstant ci64 = assertType(IntConstant.class, constant("@ci64"));
        assertIntSize(64, ci64.getType());
        assertEquals(9223372036854775807L, ci64.getValue());

        IntConstant cio64 = assertType(IntConstant.class, constant("@cio64"));
        assertEquals(0777L, cio64.getValue());

        IntConstant cix64 = assertType(IntConstant.class, constant("@cix64"));
        assertEquals(0x123456789abcdef0L, cix64.getValue());

        IntConstant cixovf = assertType(IntConstant.class, constant("@cixovf"));
        assertEquals(0xffffffffffffffffL, cixovf.getValue());

        IntConstant cixovf2 = assertType(IntConstant.class,
                constant("@cixovf2"));
        assertEquals(0x8000000000000000L, cixovf2.getValue());

        FloatConstant cf = assertType(FloatConstant.class, constant("@cf"));
        assertType(uvm.type.Float.class, cf.getType());
        assertEquals(3.14f, cf.getValue(), 0.001f);

        FloatConstant cfnan = assertType(FloatConstant.class,
                constant("@cfnan"));
        assertTrue(java.lang.Float.isNaN(cfnan.getValue()));

        FloatConstant cfninf = assertType(FloatConstant.class,
                constant("@cfninf"));
        assertTrue(java.lang.Float.isInfinite(cfninf.getValue())
                && cfninf.getValue() < 0.0f);

        FloatConstant cfpinf = assertType(FloatConstant.class,
                constant("@cfpinf"));
        assertTrue(java.lang.Float.isInfinite(cfpinf.getValue())
                && cfpinf.getValue() > 0.0f);

        FloatConstant cfbits = assertType(FloatConstant.class,
                constant("@cfbits"));
        assertEquals(0x12345678,
                java.lang.Float.floatToRawIntBits(cfbits.getValue()));

        DoubleConstant cd = assertType(DoubleConstant.class, constant("@cd"));
        assertType(uvm.type.Double.class, cd.getType());
        assertEquals(6.28d, cd.getValue(), 0.001d);

        DoubleConstant cdninf = assertType(DoubleConstant.class,
                constant("@cdninf"));
        assertTrue(java.lang.Double.isInfinite(cdninf.getValue())
                && cdninf.getValue() < 0.0d);

        DoubleConstant cdpinf = assertType(DoubleConstant.class,
                constant("@cdpinf"));
        assertTrue(java.lang.Double.isInfinite(cdpinf.getValue())
                && cdpinf.getValue() > 0.0d);

        DoubleConstant cdbits = assertType(DoubleConstant.class,
                constant("@cdbits"));
        assertEquals(0xfedcba9876543210L,
                java.lang.Double.doubleToRawLongBits(cdbits.getValue()));

        StructConstant cs1 = assertType(StructConstant.class, constant("@cs1"));
        Struct cs1t = assertType(Struct.class, cs1.getType());
        assertIntSize(64, cs1t.getFieldTypes().get(0));
        assertType(uvm.type.Double.class, cs1t.getFieldTypes().get(1));
        IntConstant cs10 = assertType(IntConstant.class, cs1.getValues().get(0));
        assertEquals(100L, cs10.getValue());
        DoubleConstant cs11 = assertType(DoubleConstant.class, cs1.getValues()
                .get(1));
        assertEquals(200.0d, cs11.getValue(), 0.01d);

        StructConstant cs2 = assertType(StructConstant.class, constant("@cs2"));
        Struct cs2t = assertType(Struct.class, cs2.getType());
        assertType(uvm.type.Double.class, cs2t.getFieldTypes().get(0));
        Struct cs2t1 = assertType(Struct.class, cs2t.getFieldTypes().get(1));
        assertType(uvm.type.Float.class, cs2t1.getFieldTypes().get(0));
        assertIntSize(64, cs2t1.getFieldTypes().get(1));
        assertIntSize(32, cs2t.getFieldTypes().get(2));
        DoubleConstant cs20 = assertType(DoubleConstant.class, cs2.getValues()
                .get(0));
        assertEquals(1.0d, cs20.getValue(), 0.01d);
        StructConstant cs21 = assertType(StructConstant.class, cs2.getValues()
                .get(1));
        FloatConstant cs210 = assertType(FloatConstant.class, cs21.getValues()
                .get(0));
        assertEquals(2.0f, cs210.getValue(), 0.01f);
        IntConstant cs211 = assertType(IntConstant.class,
                cs21.getValues().get(1));
        assertEquals(3, cs211.getValue());
        IntConstant cs22 = assertType(IntConstant.class, cs2.getValues().get(2));
        assertEquals(4, cs22.getValue());

        StructConstant cons = assertType(StructConstant.class,
                constant("@cons"));
        Struct cons_t = assertType(Struct.class, cons.getType());
        assertIntSize(64, cons_t.getFieldTypes().get(0));
        assertType(Ref.class, cons_t.getFieldTypes().get(1));
        IntConstant cons0 = assertType(IntConstant.class,
                cons.getValues().get(0));
        assertEquals(42, cons0.getValue());
        assertType(NullConstant.class, cons.getValues().get(1));

        NullConstant cr = assertType(NullConstant.class, constant("@cr"));
        assertType(Ref.class, cr.getType());
        NullConstant cir = assertType(NullConstant.class, constant("@cir"));
        assertType(IRef.class, cir.getType());
        NullConstant cwr = assertType(NullConstant.class, constant("@cwr"));
        assertType(WeakRef.class, cwr.getType());
        NullConstant cfu = assertType(NullConstant.class, constant("@cfu"));
        assertType(Func.class, cfu.getType());
        NullConstant cth = assertType(NullConstant.class, constant("@cth"));
        assertType(uvm.type.Thread.class, cth.getType());
        NullConstant cst = assertType(NullConstant.class, constant("@cst"));
        assertType(uvm.type.Stack.class, cst.getType());

        GlobalDataConstant gi64 = assertType(GlobalDataConstant.class,
                constant("@gi64"));
        IRef gi64_t = assertType(IRef.class, gi64.getType());
        assertIntSize(64, gi64_t.getReferenced());

        GlobalData gd_gi64 = assertType(GlobalData.class, globalData("@gi64"));
        assertIntSize(64, gd_gi64.getType());

        FunctionConstant fdummy = assertType(FunctionConstant.class,
                constant("@fdummy"));
        Func fdummy_type = assertType(Func.class, fdummy.getType());
        FunctionSignature fdummy_sig = fdummy_type.getSig();
        assertType(Void.class, fdummy_sig.getReturnType());
        assertTrue(fdummy_sig.getParamTypes().isEmpty());

        Function f_fdummy = assertType(Function.class, func("@fdummy"));
        FunctionSignature f_fdummy_sig = f_fdummy.getSig();
        assertType(Void.class, f_fdummy_sig.getReturnType());
        assertTrue(f_fdummy_sig.getParamTypes().isEmpty());

        StructConstant sgf = assertType(StructConstant.class, constant("@sgf"));
        assertType(GlobalDataConstant.class, sgf.getValues().get(0));
        assertType(FunctionConstant.class, sgf.getValues().get(1));
    }
}
