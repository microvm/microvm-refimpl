package uvm.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.BeforeClass;
import org.junit.Test;

import parser.RecursiveBundleBuilder;
import parser.uIRLexer;
import parser.uIRParser;
import uvm.Bundle;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
import uvm.ssavalue.Constant;
import uvm.ssavalue.FPConstant;
import uvm.ssavalue.FunctionConstant;
import uvm.ssavalue.GlobalDataConstant;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.StructConstant;

public class ConstantParsingTest {

    private static Bundle bundle;

    @BeforeClass
    public static void setUpClass() throws Exception {
        String file = "tests/uvm-parsing-test/constants.uir";
        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(file));
        uIRLexer lexer = new uIRLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        uIRParser parser = new uIRParser(tokens);
        ParseTree tree = parser.ir();

        RecursiveBundleBuilder rbb = new RecursiveBundleBuilder();
        rbb.build(tree);

        bundle = rbb.getBundle();
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
        IntConstant ci8 = assertType(constant("@ci8"), IntConstant.class);
        assertIntSize(ci8.getType(), 8);
        assertEquals(ci8.getValue(), 127L);
        IntConstant ci16 = assertType(constant("@ci16"), IntConstant.class);
        assertIntSize(ci16.getType(), 16);
        assertEquals(ci16.getValue(), 32767L);
        IntConstant ci32 = assertType(constant("@ci32"), IntConstant.class);
        assertIntSize(ci32.getType(), 32);
        assertEquals(ci32.getValue(), 2147483647L);
        IntConstant ci64 = assertType(constant("@ci64"), IntConstant.class);
        assertIntSize(ci64.getType(), 64);
        assertEquals(ci64.getValue(), 9223372036854775807L);

        IntConstant cio64 = assertType(constant("@cio64"), IntConstant.class);
        assertEquals(cio64.getValue(), 0777L);

        IntConstant cix64 = assertType(constant("@cix64"), IntConstant.class);
        assertEquals(cix64.getValue(), 0x123456789abcdef0L);

        IntConstant cixovf = assertType(constant("@cixovf"), IntConstant.class);
        assertEquals(cixovf.getValue(), 0xfffffffffffffffL);

        FPConstant cf = assertType(constant("@cf"), FPConstant.class);
        assertType(cf.getType(), uvm.type.Float.class);
        assertEquals(cf.getValue(), 3.14, 0.001);

        FPConstant cd = assertType(constant("@cd"), FPConstant.class);
        assertType(cd.getType(), uvm.type.Double.class);
        assertEquals(cd.getValue(), 6.28, 0.001);

        StructConstant cs1 = assertType(constant("@cs1"), StructConstant.class);
        Struct cs1t = assertType(cs1.getType(), Struct.class);
        assertIntSize(cs1t.getFieldTypes().get(0), 64);
        assertType(cs1t.getFieldTypes().get(1), uvm.type.Double.class);
        IntConstant cs10 = assertType(cs1.getValues().get(0), IntConstant.class);
        assertEquals(cs10.getValue(), 100L);
        FPConstant cs11 = assertType(cs1.getValues().get(1), FPConstant.class);
        assertEquals(cs11.getValue(), 200.0, 0.01);

        StructConstant cs2 = assertType(constant("@cs2"), StructConstant.class);
        Struct cs2t = assertType(cs2.getType(), Struct.class);
        assertType(cs2t.getFieldTypes().get(0), uvm.type.Double.class);
        Struct cs2t1 = assertType(cs2t.getFieldTypes().get(1), Struct.class);
        assertType(cs2t1.getFieldTypes().get(0), uvm.type.Float.class);
        assertIntSize(cs2t1.getFieldTypes().get(1), 64);
        assertIntSize(cs2t.getFieldTypes().get(2), 32);
        FPConstant cs20 = assertType(cs2.getValues().get(0), FPConstant.class);
        assertEquals(cs20.getValue(), 1.0, 0.01);
        StructConstant cs21 = assertType(cs2.getValues().get(1),
                StructConstant.class);
        FPConstant cs210 = assertType(cs21.getValues().get(0), FPConstant.class);
        assertEquals(cs210.getValue(), 2.0, 0.01);
        IntConstant cs211 = assertType(cs21.getValues().get(1),
                IntConstant.class);
        assertEquals(cs211.getValue(), 3);
        IntConstant cs22 = assertType(cs2.getValues().get(2), IntConstant.class);
        assertEquals(cs22.getValue(), 4);

        StructConstant cons = assertType(constant("@cons"),
                StructConstant.class);
        Struct cons_t = assertType(cons.getType(), Struct.class);
        assertIntSize(cons_t.getFieldTypes().get(0), 64);
        assertType(cons_t.getFieldTypes().get(1), Ref.class);
        IntConstant cons0 = assertType(cons.getValues().get(0),
                IntConstant.class);
        assertEquals(cons0.getValue(), 42);
        assertType(cons.getValues().get(1), NullConstant.class);

        NullConstant cr = assertType(constant("@cr"), NullConstant.class);
        assertType(cr.getType(), Ref.class);
        NullConstant cir = assertType(constant("@cir"), NullConstant.class);
        assertType(cir.getType(), IRef.class);
        NullConstant cwr = assertType(constant("@cwr"), NullConstant.class);
        assertType(cwr.getType(), WeakRef.class);
        NullConstant cfu = assertType(constant("@cfu"), NullConstant.class);
        assertType(cfu.getType(), Func.class);
        NullConstant cth = assertType(constant("@cth"), NullConstant.class);
        assertType(cth.getType(), uvm.type.Thread.class);
        NullConstant cst = assertType(constant("@cst"), NullConstant.class);
        assertType(cst.getType(), uvm.type.Stack.class);

        GlobalDataConstant gi64 = assertType(constant("@gi64"),
                GlobalDataConstant.class);
        IRef gi64_t = assertType(gi64.getType(), IRef.class);
        assertIntSize(gi64_t.getReferenced(), 64);

        GlobalData gd_gi64 = assertType(globalData("@gi64"), GlobalData.class);
        assertIntSize(gd_gi64.getType(), 64);

        FunctionConstant fdummy = assertType(constant("@fdummy"),
                FunctionConstant.class);
        Func fdummy_type = assertType(fdummy.getType(), Func.class);
        FunctionSignature fdummy_sig = fdummy_type.getSig();
        assertType(fdummy_sig.getReturnType(), Void.class);
        assertTrue(fdummy_sig.getParamTypes().isEmpty());

        Function f_fdummy = assertType(func("@fdummy"), Function.class);
        FunctionSignature f_fdummy_sig = f_fdummy.getSig();
        assertType(f_fdummy_sig.getReturnType(), Void.class);
        assertTrue(f_fdummy_sig.getParamTypes().isEmpty());

        StructConstant sgf = assertType(constant("@sgf"), StructConstant.class);
        assertType(sgf.getValues().get(0), GlobalDataConstant.class);
        assertType(sgf.getValues().get(1), FunctionConstant.class);
    }
}
