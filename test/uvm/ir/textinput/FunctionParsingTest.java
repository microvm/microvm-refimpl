package uvm.ir.textinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uvm.Function;
import uvm.FunctionSignature;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.Parameter;
import uvm.type.Func;
import uvm.type.IRef;
import uvm.type.Type;
import uvm.type.Void;

public class FunctionParsingTest extends BundleTester {
    @Override
    protected String bundleName() {
        return "tests/uvm-parsing-test/functions.uir";
    }

    public void assertIsSigT(Type type) {
        Func sig_t = assertType(Func.class, type);
        FunctionSignature sig_fs = sig_t.getSig();
        assertType(Void.class, sig_fs.getReturnType());
        assertIntSize(32, sig_fs.getParamTypes().get(0));
    }

    @Test
    public void testParsing() {
        FunctionSignature foo = assertType(FunctionSignature.class,
                funcSig("@foo"));
        assertType(Void.class, foo.getReturnType());
        assertTrue(foo.getParamTypes().isEmpty());

        FunctionSignature bar = assertType(FunctionSignature.class,
                funcSig("@bar"));
        assertIntSize(64, bar.getReturnType());
        assertIntSize(32, bar.getParamTypes().get(0));
        assertIntSize(16, bar.getParamTypes().get(1));

        FunctionSignature baz = assertType(FunctionSignature.class,
                funcSig("@baz"));
        assertIsBaz(baz);

        FunctionSignature sig_fs = assertType(FunctionSignature.class,
                funcSig("@sig_fs"));
        assertType(Void.class, sig_fs.getReturnType());
        assertIntSize(32, sig_fs.getParamTypes().get(0));

        Func sig_t = assertType(Func.class, type("@sig_t"));

        Function signal = assertType(Function.class, func("@signal"));

        assertIsSigT(signal.getSig().getReturnType());
        assertIntSize(32, signal.getSig().getParamTypes().get(0));
        assertIsSigT(signal.getSig().getParamTypes().get(1));
        assertIsSigT(sig_t);

        IntConstant zero = assertType(IntConstant.class, constant("@zero"));
        assertIntSize(32, zero.getType());
        assertEquals(0L, zero.getValue());

        Function main = assertType(Function.class, func("@main"));
        FunctionSignature main_sig = main.getSig();
        assertIsBaz(main_sig);

        Parameter argc = assertType(Parameter.class, main.getCFG().getInstNs()
                .getByName("%argc"));
        assertEquals(main.getCFG().getParams().get(0), argc);
        
        Parameter argv = assertType(Parameter.class, main.getCFG().getInstNs()
                .getByName("%argv"));
        assertEquals(main.getCFG().getParams().get(1), argv);

    }

    private void assertIsBaz(FunctionSignature baz) {
        assertIntSize(32, baz.getReturnType());
        assertIntSize(32, baz.getParamTypes().get(0));
        IRef baz_1 = assertType(IRef.class, baz.getParamTypes().get(1));
        IRef baz_1r = assertType(IRef.class, baz_1.getReferenced());
        assertIntSize(8, baz_1r.getReferenced());
    }
}
