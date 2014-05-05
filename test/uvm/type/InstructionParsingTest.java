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

public class InstructionParsingTest {

    private static Bundle bundle;

    @BeforeClass
    public static void setUpClass() throws Exception {
        String file = "tests/uvm-parsing-test/instructions.uir";
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

    @Test
    public void testParsing() {
    }
}
