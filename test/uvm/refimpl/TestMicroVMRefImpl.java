package uvm.refimpl;

import static org.junit.Assert.*;
import static uvm.refimpl.MicroVMHelper.*;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uvm.Bundle;
import uvm.CFG;
import uvm.Function;
import uvm.ir.text.input.TestingHelper;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.itpr.IntBox;
import uvm.refimpl.itpr.InterpreterFrame;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.TrapHandler;
import uvm.refimpl.itpr.ValueBox;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.Value;

public class TestMicroVMRefImpl {

    public static MicroVM microVM;
    public static MicroVMHelper h;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        try {
            microVM = new MicroVM();
            h = new MicroVMHelper(microVM);
            Bundle bundle = TestingHelper
                    .parseUir("tests/uvm-refimpl-test/basic-tests.uir");
            microVM.addBundle(bundle);
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
        return ((IntBox) box).getValue();
    }

    @Test
    public void testBinops() throws Exception {
        Function binops = h.func("@binops");
        assertNotNull(binops);
        InterpreterStack stack = h.makeStack(binops, IntBox(25), IntBox(7));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                InterpreterFrame curTop = thread.getStack().getTop();
                List<ValueBox> keepAlives = curTop.dumpKeepAlives();
                long[] expecteds = new long[] { 32, 18, 175, 3, 3, 4, 4, 3200,
                        0, 0, 1, 31, 30 };
                long[] actuals = new long[expecteds.length];

                for (int i = 0; i < expecteds.length; i++) {
                    ValueBox vb = keepAlives.get(i);
                    IntBox ib = (IntBox) vb;
                    long actual = ib.getValue();
                    actuals[i] = actual;
                }

                assertArrayEquals(expecteds, actuals);

                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();
    }

    @Test
    //@Ignore
    public void testSimplesum() throws InterruptedException {
        Function simplesum = h.func("@simplesum");
        assertNotNull(simplesum);
        InterpreterStack stack = h.makeStack(simplesum, IntBox(1L),
                IntBox(1000000L));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            private long timestampStart;
            private long timestampEnd;

            @Override
            public Long onTrap(InterpreterThread thread) {
                InterpreterFrame curTop = thread.getStack().getTop();
                Instruction curInst = curTop.getCurInst();

                if (curInst.getName().equals("%starttrap")) {
                    timestampStart = System.currentTimeMillis();
                } else if (curInst.getName().equals("%montrap")) {
                    List<ValueBox> keepAlives = curTop.dumpKeepAlives();

                    for (ValueBox vb : keepAlives) {
                        IntBox ib = (IntBox) vb;
                        long actual = ib.getValue();
                        System.out.print(actual);
                        System.out.print(" ");
                    }
                    System.out.println();

                } else {
                    timestampEnd = System.currentTimeMillis();
                    long duration = timestampEnd - timestampStart;

                    System.out.format("Time: %d ms\n", duration);
                    List<ValueBox> keepAlives = curTop.dumpKeepAlives();
                    long[] expecteds = new long[] { 500000500000L };
                    long[] actuals = new long[expecteds.length];

                    for (int i = 0; i < expecteds.length; i++) {
                        ValueBox vb = keepAlives.get(i);
                        IntBox ib = (IntBox) vb;
                        long actual = ib.getValue();
                        actuals[i] = actual;
                    }
                    System.out.println(actuals[0]);

                    assertArrayEquals(expecteds, actuals);
                    thread.exit();
                }

                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();
    }
}
