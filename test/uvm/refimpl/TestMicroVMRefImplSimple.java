package uvm.refimpl;

import static org.junit.Assert.*;
import static uvm.platformsupport.Config.*;
import static uvm.refimpl.MicroVMHelper.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uvm.Bundle;
import uvm.CFG;
import uvm.Function;
import uvm.IdentifiedHelper;
import uvm.ir.text.input.TestingHelper;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.facade.MicroVMClient;
import uvm.refimpl.itpr.DoubleBox;
import uvm.refimpl.itpr.FloatBox;
import uvm.refimpl.itpr.FuncBox;
import uvm.refimpl.itpr.IRefBox;
import uvm.refimpl.itpr.IntBox;
import uvm.refimpl.itpr.InterpreterFrame;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.RefBox;
import uvm.refimpl.itpr.TagRef64Box;
import uvm.refimpl.itpr.ThreadStackManager;
import uvm.refimpl.itpr.ValueBox;
import uvm.refimpl.mem.TypeSizes;
import uvm.refimpl.mem.scanning.ObjectMarker;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.Value;

public class TestMicroVMRefImplSimple {

    private static MicroVM microVM;
    private static MicroVMHelper h;

    private static MicroVMClient client = new DumbClient() {
        public void markExternalRoots(ObjectMarker marker) {
            // No external roots.
            // GC may be triggered when doing too many tests and creating too
            // many stacks.
        };

        @Override
        public Long onTrap(InterpreterThread thread, ValueBox trapValue) {
            return trapHandler.onTrap(thread, trapValue);
        }

    };

    private static interface TrapHandler {
        public Long onTrap(InterpreterThread thread, ValueBox trapValue);
    }

    private static TrapHandler trapHandler;

    private static void setTrapHandler(TrapHandler trapHandler) {
        TestMicroVMRefImplSimple.trapHandler = trapHandler;
    }

    private static void loadTextBundle(String fileName) {
        try {
            Bundle bundle = TestingHelper.parseUir(fileName,
                    microVM.getGlobalBundle());
            microVM.addBundle(bundle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        try {
            microVM = new MicroVM();
            microVM.setClient(client);
            h = new MicroVMHelper(microVM);
            loadTextBundle("tests/uvm-refimpl-test/primitives.uir");
            loadTextBundle("tests/uvm-refimpl-test/simple-tests.uir");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private ValueBox getValueBox(InterpreterFrame frame, String name) {
        CFG cfg = frame.getCfg();
        Value val = cfg.getInstNs().getByName(name);
        return frame.getValueBox(val);
    }

    private long getInt(InterpreterFrame frame, String name) {
        ValueBox box = getValueBox(frame, name);
        return ((IntBox) box).getValue().longValue();
    }

    @Test
    public void testFac() throws InterruptedException {
        InterpreterStack stack = h.makeStack(h.func("@test_fac"));

        setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
                String trapName = thread.getStack().getTop().getCurInst()
                        .getName();
                if (trapName.equals("%checktrap")) {
                    List<ValueBox> kas = thread.getStack().getTop()
                            .dumpKeepAlives();
                    long r1 = ((IntBox) kas.get(0)).getValue().longValue();
                    long r2 = ((IntBox) kas.get(1)).getValue().longValue();
                    long r3 = ((IntBox) kas.get(2)).getValue().longValue();
                    assertEquals(3628800, r1);
                    assertEquals(3628800, r2);
                    assertEquals(3628800, r3);
                } else {
                    fail("Who set up this trap?");
                }
                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();

    }

    @Test
    public void testFib() throws InterruptedException {
        InterpreterStack stack = h.makeStack(h.func("@test_fib"));

        setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
                String trapName = thread.getStack().getTop().getCurInst()
                        .getName();
                if (trapName.equals("%checktrap")) {
                    List<ValueBox> kas = thread.getStack().getTop()
                            .dumpKeepAlives();
                    long r1 = ((IntBox) kas.get(0)).getValue().longValue();
                    long r2 = ((IntBox) kas.get(1)).getValue().longValue();
                    // long r3 = ((IntBox) kas.get(2)).getValue().longValue();
                    assertEquals(55, r1);
                    assertEquals(55, r2);
                    // assertEquals(3628800, r3);
                } else if (trapName.equals("%watch")) {
                    List<ValueBox> kas = thread.getStack().getTop()
                            .dumpKeepAlives();
                    long a = ((IntBox) kas.get(0)).getValue().longValue();
                    long b = ((IntBox) kas.get(1)).getValue().longValue();
                    long c = ((IntBox) kas.get(2)).getValue().longValue();
                    long d = ((IntBox) kas.get(3)).getValue().longValue();
                    long aa = ((IntBox) kas.get(4)).getValue().longValue();
                    long bb = ((IntBox) kas.get(5)).getValue().longValue();
                    long cc = ((IntBox) kas.get(6)).getValue().longValue();
                    long dd = ((IntBox) kas.get(7)).getValue().longValue();
                    long nn = ((IntBox) kas.get(8)).getValue().longValue();

                    System.out
                            .format("a=%d b=%d c=%d d=%d aa=%d bb=%d cc=%d dd=%d nn=%d\n",
                                    a, b, c, d, aa, bb, cc, dd, nn);

                } else {
                    fail("Who set up this trap?");
                }
                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();

    }
}
