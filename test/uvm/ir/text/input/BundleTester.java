package uvm.ir.text.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static uvm.ir.text.input.TestingHelper.parseUir;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;

import uvm.Bundle;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
import uvm.ssavalue.Constant;
import uvm.type.Int;
import uvm.type.Type;

public abstract class BundleTester {
    @Before
    public void setUp() throws Exception {
        if (bundle == null) {
            loadBundle();
        }
    }

    protected void loadBundle() throws IOException, FileNotFoundException {
        bundle = parseUir(bundleName());
    }

    protected abstract String bundleName();

    protected Bundle bundle;

    protected Type type(String name) {
        Type type = bundle.getTypeNs().getByName(name);
        if (type == null) {
            fail("No such type " + name);
        }
        return type;
    }

    protected Constant constant(String name) {
        Constant constant = bundle.getGlobalValueNs().getByName(name);
        if (constant == null) {
            fail("No such constant " + name);
        }
        return constant;
    }

    protected GlobalData globalData(String name) {
        GlobalData global = bundle.getGlobalDataNs().getByName(name);
        if (global == null) {
            fail("No such global " + name);
        }
        return global;
    }

    protected Function func(String name) {
        Function func = bundle.getFuncNs().getByName(name);
        if (func == null) {
            fail("No such func " + name);
        }
        return func;
    }

    protected FunctionSignature funcSig(String name) {
        FunctionSignature sig = bundle.getFuncSigNs().getByName(name);
        if (sig == null) {
            fail("No such sig " + name);
        }
        return sig;
    }

    protected <T> T assertType(Class<T> cls, Object obj) {
        if (!cls.isAssignableFrom(obj.getClass())) {
            fail("Found " + obj.getClass().getName() + ", expect "
                    + cls.getName());
        }
        return cls.cast(obj);
    }

    protected void assertIntSize(int size, Type type) {
        if (!(type instanceof Int)) {
            fail("Found " + type.getClass().getName() + ", expect Int");
        }
        assertEquals(((Int) type).getSize(), size);
    }

}
