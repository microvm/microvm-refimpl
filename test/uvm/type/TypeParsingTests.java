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

public class TypeParsingTests {

    private static Bundle bundle;

    @BeforeClass
    public static void setUpClass() throws Exception {
        String file = "tests/uvm-parsing-test/types.uir";
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

    private static <T> T assertType(Type type, Class<T> cls) {
        if (!cls.isAssignableFrom(type.getClass())) {
            fail("Found " + type.getClass().getName() + ", expect "
                    + cls.getName());
        }
        return cls.cast(type);
    }

    private void assertIntSize(Type type, int size) {
        if (!(type instanceof Int)) {
            fail("Found " + type.getClass().getName() + ", expect Int");
        }
        assertEquals(((Int) type).getSize(), size);
    }

    @Test
    public void testParsing() {

        assertIntSize(type("@i1"), 1);
        assertIntSize(type("@i8"), 8);
        assertIntSize(type("@i16"), 16);
        assertIntSize(type("@i32"), 32);
        assertIntSize(type("@i64"), 64);

        assertType(type("@f"), Float.class);
        assertType(type("@d"), Double.class);

        assertType(type("@rv"), Ref.class);
        assertType(type("@irv"), IRef.class);
        assertType(type("@wrv"), WeakRef.class);

        AbstractReferenceType ri16 = assertType(type("@ri16"), Ref.class);
        assertIntSize(ri16.getReferenced(), 16);

        AbstractReferenceType ri16_2 = assertType(type("@ri16"), Ref.class);
        assertIntSize(ri16_2.getReferenced(), 16);

        assertType(type("@s0"), Struct.class);

        Struct s1 = assertType(type("@s1"), Struct.class);
        assertIntSize(s1.getFieldTypes().get(0), 8);
        assertIntSize(s1.getFieldTypes().get(1), 16);
        assertIntSize(s1.getFieldTypes().get(2), 32);
        assertIntSize(s1.getFieldTypes().get(3), 64);
        assertType(s1.getFieldTypes().get(4), Float.class);
        assertType(s1.getFieldTypes().get(5), Double.class);
        Ref s1_6 = assertType(s1.getFieldTypes().get(6), Ref.class);
        assertType(s1_6.getReferenced(), Void.class);
        IRef s1_7 = assertType(s1.getFieldTypes().get(7), IRef.class);
        assertType(s1_7.getReferenced(), Void.class);
        WeakRef s1_8 = assertType(s1.getFieldTypes().get(8), WeakRef.class);
        assertType(s1_8.getReferenced(), Void.class);
        Ref s1_9 = assertType(s1.getFieldTypes().get(9), Ref.class);
        assertIntSize(s1_9.getReferenced(), 16);
        Ref s1_10 = assertType(s1.getFieldTypes().get(10), Ref.class);
        assertIntSize(s1_10.getReferenced(), 16);

        Struct cons = assertType(type("@cons"), Struct.class);

        Struct curCons = cons;
        for (int n = 0; n < 10; n++) { // Only check for 10 iterations.
            assertIntSize(curCons.getFieldTypes().get(0), 64);
            Ref cons_1 = assertType(curCons.getFieldTypes().get(1), Ref.class);
            Struct consBack = assertType(cons_1.getReferenced(), Struct.class);
            curCons = consBack;
        }

        Array a0 = assertType(type("@a0"), Array.class);
        assertIntSize(a0.getElemType(), 8);
        assertEquals(a0.getLength(), 100);

        Array a1 = assertType(type("@a1"), Array.class);
        Struct a1_0 = assertType(a1.getElemType(), Struct.class);
        assertType(a1_0.getFieldTypes().get(0), Double.class);
        assertIntSize(a1_0.getFieldTypes().get(1), 64);
        assertEquals(a1.getLength(), 10);

        Array a2 = assertType(type("@a2"), Array.class);
        Array a2_0 = assertType(a2.getElemType(), Array.class);
        assertType(a2_0.getElemType(), Struct.class);
        assertEquals(a2.getLength(), 10);

        Hybrid h0 = assertType(type("@h0"), Hybrid.class);
        assertType(h0.getFixedPart(), Void.class);
        assertIntSize(h0.getVarPart(), 8);

        Hybrid h1 = assertType(type("@h1"), Hybrid.class);
        assertType(h1.getFixedPart(), Struct.class);
        assertIntSize(h1.getVarPart(), 64);

        assertType(type("@v"), Void.class);

        Func f0 = assertType(type("@f0"), Func.class);
        assertType(f0.getSig().getReturnType(), Void.class);
        assertTrue(f0.getSig().getParamTypes().isEmpty());

        Func f1 = assertType(type("@f1"), Func.class);
        assertIntSize(f1.getSig().getReturnType(), 32);
        assertIntSize(f1.getSig().getParamTypes().get(0), 32);
        IRef argv = assertType(f1.getSig().getParamTypes().get(1), IRef.class);
        IRef argvs = assertType(argv.getReferenced(), IRef.class);
        assertIntSize(argvs.getReferenced(), 8);

        assertType(type("@th"), Thread.class);
        assertType(type("@st"), Stack.class);
        assertType(type("@tr64"), TagRef64.class);

    }

}
