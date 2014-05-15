package uvm.ir.textinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uvm.type.AbstractReferenceType;
import uvm.type.Array;
import uvm.type.Double;
import uvm.type.Float;
import uvm.type.Func;
import uvm.type.Hybrid;
import uvm.type.IRef;
import uvm.type.Ref;
import uvm.type.Stack;
import uvm.type.Struct;
import uvm.type.TagRef64;
import uvm.type.Thread;
import uvm.type.Void;
import uvm.type.WeakRef;

public class TypeParsingTest extends BundleTester {

    @Override
    protected String bundleName() {
        return "tests/uvm-parsing-test/types.uir";
    }

    @Test
    public void testParsing() {
        assertIntSize(1, type("@i1"));
        assertIntSize(8, type("@i8"));
        assertIntSize(16, type("@i16"));
        assertIntSize(32, type("@i32"));
        assertIntSize(64, type("@i64"));

        assertType(Float.class, type("@f"));
        assertType(Double.class, type("@d"));

        assertType(Ref.class, type("@rv"));
        assertType(IRef.class, type("@irv"));
        assertType(WeakRef.class, type("@wrv"));

        AbstractReferenceType ri16 = assertType(Ref.class, type("@ri16"));
        assertIntSize(16, ri16.getReferenced());

        AbstractReferenceType ri16_2 = assertType(Ref.class, type("@ri16"));
        assertIntSize(16, ri16_2.getReferenced());

        assertType(Struct.class, type("@s0"));

        Struct s1 = assertType(Struct.class, type("@s1"));
        assertIntSize(8, s1.getFieldTypes().get(0));
        assertIntSize(16, s1.getFieldTypes().get(1));
        assertIntSize(32, s1.getFieldTypes().get(2));
        assertIntSize(64, s1.getFieldTypes().get(3));
        assertType(Float.class, s1.getFieldTypes().get(4));
        assertType(Double.class, s1.getFieldTypes().get(5));
        Ref s1_6 = assertType(Ref.class, s1.getFieldTypes().get(6));
        assertType(Void.class, s1_6.getReferenced());
        IRef s1_7 = assertType(IRef.class, s1.getFieldTypes().get(7));
        assertType(Void.class, s1_7.getReferenced());
        WeakRef s1_8 = assertType(WeakRef.class, s1.getFieldTypes().get(8));
        assertType(Void.class, s1_8.getReferenced());
        Ref s1_9 = assertType(Ref.class, s1.getFieldTypes().get(9));
        assertIntSize(16, s1_9.getReferenced());
        Ref s1_10 = assertType(Ref.class, s1.getFieldTypes().get(10));
        assertIntSize(16, s1_10.getReferenced());

        Struct cons = assertType(Struct.class, type("@cons"));

        Struct curCons = cons;
        for (int n = 0; n < 10; n++) { // Only check for 10 iterations.
            assertIntSize(64, curCons.getFieldTypes().get(0));
            Ref cons_1 = assertType(Ref.class, curCons.getFieldTypes().get(1));
            Struct consBack = assertType(Struct.class, cons_1.getReferenced());
            curCons = consBack;
        }

        Array a0 = assertType(Array.class, type("@a0"));
        assertIntSize(8, a0.getElemType());
        assertEquals(100, a0.getLength());

        Array a1 = assertType(Array.class, type("@a1"));
        Struct a1_0 = assertType(Struct.class, a1.getElemType());
        assertType(Double.class, a1_0.getFieldTypes().get(0));
        assertIntSize(64, a1_0.getFieldTypes().get(1));
        assertEquals(10, a1.getLength());

        Array a2 = assertType(Array.class, type("@a2"));
        Array a2_0 = assertType(Array.class, a2.getElemType());
        assertType(Struct.class, a2_0.getElemType());
        assertEquals(a2.getLength(), 10);

        Hybrid h0 = assertType(Hybrid.class, type("@h0"));
        assertType(Void.class, h0.getFixedPart());
        assertIntSize(8, h0.getVarPart());

        Hybrid h1 = assertType(Hybrid.class, type("@h1"));
        assertType(Struct.class, h1.getFixedPart());
        assertIntSize(64, h1.getVarPart());

        assertType(Void.class, type("@v"));

        Func f0 = assertType(Func.class, type("@f0"));
        assertType(Void.class, f0.getSig().getReturnType());
        assertTrue(f0.getSig().getParamTypes().isEmpty());

        Func f1 = assertType(Func.class, type("@f1"));
        assertIntSize(32, f1.getSig().getReturnType());
        assertIntSize(32, f1.getSig().getParamTypes().get(0));
        IRef argv = assertType(IRef.class, f1.getSig().getParamTypes().get(1));
        IRef argvs = assertType(IRef.class, argv.getReferenced());
        assertIntSize(8, argvs.getReferenced());

        assertType(Thread.class, type("@th"));
        assertType(Stack.class, type("@st"));
        assertType(TagRef64.class, type("@tr64"));

    }

}
