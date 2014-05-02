package uvm.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uvm.type.TestingHelper.parseUir;

import org.junit.BeforeClass;
import org.junit.Test;

import uvm.Bundle;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
import uvm.ssavalue.Constant;
import uvm.ssavalue.IntConstant;

public class FunctionParsingTest {

    private static Bundle bundle;

    @BeforeClass
    public static void setUpClass() throws Exception {
        String file = "tests/uvm-parsing-test/functions.uir";
        bundle = parseUir(file);
    }

    private static Type type(String name) {
        return bundle.getTypeByName(name);
    }

    private static Constant constant(String name) {
        return bundle.getConstantByName(name);
    }

    private static GlobalData globalData(String name) {
        return bundle.getGlobalDataByName(name);
    }

    private static Function func(String name) {
        return bundle.getFuncByName(name);
    }

    private static FunctionSignature funcSig(String name) {
        return bundle.getFuncSigByName(name);
    }

    private static <T> T assertType(Object obj, Class<T> cls) {
        if (!cls.isAssignableFrom(obj.getClass())) {
            fail("Found " + obj.getClass().getName() + ", expect "
                    + cls.getName());
        }
        return cls.cast(obj);
    }

    private void assertIntSize(Type type, int size) {
        if (!(type instanceof Int)) {
            fail("Found " + type.getClass().getName() + ", expect Int");
        }
        assertEquals(((Int) type).getSize(), size);
    }

    public void assertIsSigT(Type type) {
        Func sig_t = assertType(type, Func.class);
        FunctionSignature sig_fs = sig_t.getSig();
        assertType(sig_fs.getReturnType(), Void.class);
        assertIntSize(sig_fs.getParamTypes().get(0), 32);
    }

    @Test
    public void testParsing() {
        FunctionSignature foo = assertType(funcSig("@foo"),
                FunctionSignature.class);
        assertType(foo.getReturnType(), Void.class);
        assertTrue(foo.getParamTypes().isEmpty());

        FunctionSignature bar = assertType(funcSig("@bar"),
                FunctionSignature.class);
        assertIntSize(bar.getReturnType(), 64);
        assertIntSize(bar.getParamTypes().get(0), 32);
        assertIntSize(bar.getParamTypes().get(1), 16);

        FunctionSignature baz = assertType(funcSig("@baz"),
                FunctionSignature.class);
        assertIsBaz(baz);

        FunctionSignature sig_fs = assertType(funcSig("@sig_fs"),
                FunctionSignature.class);
        assertType(sig_fs.getReturnType(), Void.class);
        assertIntSize(sig_fs.getParamTypes().get(0), 32);

        Func sig_t = assertType(type("@sig_t"), Func.class);

        Function signal = assertType(func("@signal"), Function.class);

        assertIsSigT(signal.getSig().getReturnType());
        assertIntSize(signal.getSig().getParamTypes().get(0), 32);
        assertIsSigT(signal.getSig().getParamTypes().get(1));
        assertIsSigT(sig_t);

        IntConstant zero = assertType(constant("@zero"), IntConstant.class);
        assertIntSize(zero.getType(), 32);
        assertEquals(zero.getValue(), 0L);

        Function main = assertType(func("@main"), Function.class);
        FunctionSignature main_sig = main.getSig();
        assertIsBaz(main_sig);
    }

    private void assertIsBaz(FunctionSignature baz) {
        assertIntSize(baz.getReturnType(), 32);
        assertIntSize(baz.getParamTypes().get(0), 32);
        IRef baz_1 = assertType(baz.getParamTypes().get(1), IRef.class);
        IRef baz_1r = assertType(baz_1.getReferenced(), IRef.class);
        assertIntSize(baz_1r.getReferenced(), 8);
    }
}
