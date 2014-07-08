package uvm.refimpl;

import static org.junit.Assert.*;
import static uvm.refimpl.MicroVMHelper.*;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uvm.Bundle;
import uvm.CFG;
import uvm.Function;
import uvm.ir.text.input.TestingHelper;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.itpr.DoubleBox;
import uvm.refimpl.itpr.FloatBox;
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
        return ((IntBox) box).getValue().longValue();
    }

    private void assertKeepalivesInt(InterpreterThread thread, long[] expecteds) {
        InterpreterFrame curTop = thread.getStack().getTop();
        List<ValueBox> keepAlives = curTop.dumpKeepAlives();
        long[] actuals = new long[keepAlives.size()];

        for (int i = 0; i < keepAlives.size(); i++) {
            ValueBox vb = keepAlives.get(i);
            IntBox ib = (IntBox) vb;
            long actual = ib.getValue().longValue();
            actuals[i] = actual;
        }

        assertArrayEquals(expecteds, actuals);
    }

    private void assertKeepalivesFloat(InterpreterThread thread,
            float[] expecteds) {
        InterpreterFrame curTop = thread.getStack().getTop();
        List<ValueBox> keepAlives = curTop.dumpKeepAlives();
        float[] actuals = new float[keepAlives.size()];

        for (int i = 0; i < keepAlives.size(); i++) {
            ValueBox vb = keepAlives.get(i);
            FloatBox ib = (FloatBox) vb;
            float actual = ib.getValue();
            actuals[i] = actual;
        }

        assertArrayEquals(expecteds, actuals, 0.01f);
    }

    private void assertKeepalivesDouble(InterpreterThread thread,
            double[] expecteds) {
        InterpreterFrame curTop = thread.getStack().getTop();
        List<ValueBox> keepAlives = curTop.dumpKeepAlives();
        double[] actuals = new double[keepAlives.size()];

        for (int i = 0; i < keepAlives.size(); i++) {
            ValueBox vb = keepAlives.get(i);
            DoubleBox ib = (DoubleBox) vb;
            double actual = ib.getValue();
            actuals[i] = actual;
        }

        assertArrayEquals(expecteds, actuals, 0.01);
    }

    @Test
    public void testBinops() throws Exception {
        InterpreterStack stack32 = h.makeStack(h.func("@binops32"), IntBox(25),
                IntBox(7));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                long[] expecteds = new long[] { 32, 18, 175, 3, 3, 4, 4, 3200,
                        0, 0, 1, 31, 30 };
                assertKeepalivesInt(thread, expecteds);

                return null;
            }

        });

        InterpreterThread thread32 = microVM.newThread(stack32);
        thread32.join();

        InterpreterStack stack64 = h.makeStack(h.func("@binops64"), IntBox(25),
                IntBox(7));
        InterpreterThread thread64 = microVM.newThread(stack64);
        thread64.join();

        InterpreterStack stackOvf = h.makeStack(h.func("@binops_ovf"));
        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                long[] expecteds = new long[] { 0x8000000000000000L, 1L,
                        2003764205206896640L };
                assertKeepalivesInt(thread, expecteds);

                return null;
            }
        });

        InterpreterThread threadOvf = microVM.newThread(stackOvf);
        threadOvf.join();

        InterpreterStack stackF = h.makeStack(h.func("@binops_f"),
                FloatBox(8.0f), FloatBox(2.0f));
        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                float[] expecteds = new float[] { 10.0f, 6.0f, 16.0f, 4.0f,
                        0.0f };
                assertKeepalivesFloat(thread, expecteds);

                return null;
            }
        });

        InterpreterThread threadF = microVM.newThread(stackF);
        threadF.join();

        InterpreterStack stackD = h.makeStack(h.func("@binops_d"),
                DoubleBox(8.0), DoubleBox(2.0));
        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                double[] expecteds = new double[] { 10.0, 6.0, 16.0, 4.0, 0.0 };
                assertKeepalivesDouble(thread, expecteds);

                return null;
            }
        });

        InterpreterThread threadD = microVM.newThread(stackD);
        threadD.join();
    }

    @Test
    // @Ignore
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
                        long actual = ib.getValue().longValue();
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
                        long actual = ib.getValue().longValue();
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
