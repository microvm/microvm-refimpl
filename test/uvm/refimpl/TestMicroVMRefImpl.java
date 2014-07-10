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
import uvm.refimpl.itpr.DoubleBox;
import uvm.refimpl.itpr.FloatBox;
import uvm.refimpl.itpr.IntBox;
import uvm.refimpl.itpr.InterpreterFrame;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.RefBox;
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
                        2003764205206896640L, 0x4000000000000000L, 2L };
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
    public void testCmp() throws Exception {

        InterpreterStack stack64 = h.makeStack(h.func("@cmp64"), IntBox(25),
                IntBox(7));
        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                long[] expecteds = new long[] { 0, 1, 0, 0, 1, 1, 0, 0, 1, 1 };
                assertKeepalivesInt(thread, expecteds);

                return null;
            }
        });
        InterpreterThread thread64 = microVM.newThread(stack64);
        thread64.join();

        InterpreterStack stack64_ovf = h.makeStack(h.func("@cmp64"),
                BigIntBox("8000000000000000", 16), IntBox(0x7fffffffffffffffL));
        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                long[] expecteds = new long[] { 0, 1, 0, 0, 1, 1, 1, 1, 0, 0 };
                assertKeepalivesInt(thread, expecteds);

                return null;
            }
        });
        InterpreterThread thread64_ovf = microVM.newThread(stack64_ovf);
        thread64_ovf.join();

        InterpreterStack stackF = h.makeStack(h.func("@cmp_f"),
                FloatBox(25.0F), FloatBox(7.0F));
        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                long[] expecteds = new long[] { 1, 0, 1, 0, 1, 0, 0, 1, 1, 0,
                        0, 1, 0, 0, 1, 1 };
                assertKeepalivesInt(thread, expecteds);

                return null;
            }
        });

        InterpreterThread threadF = microVM.newThread(stackF);
        threadF.join();

        InterpreterStack stackD = h.makeStack(h.func("@cmp_d"),
                DoubleBox(25.0), DoubleBox(7.0));
        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                long[] expecteds = new long[] { 1, 0, 1, 0, 1, 0, 0, 1, 1, 0,
                        0, 1, 0, 0, 1, 1 };
                assertKeepalivesInt(thread, expecteds);

                return null;
            }
        });

        InterpreterThread threadD = microVM.newThread(stackD);
        threadD.join();

        InterpreterStack stackDNan = h.makeStack(h.func("@cmp_d"),
                DoubleBox(Double.NaN), DoubleBox(7.0));
        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                long[] expecteds = new long[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                        1, 1, 1, 1, 1, 1 };
                assertKeepalivesInt(thread, expecteds);

                return null;
            }
        });

        InterpreterThread threadDNan = microVM.newThread(stackDNan);
        threadDNan.join();
    }

    @Test
    public void testSelect() throws InterruptedException {
        InterpreterStack stack = h.makeStack(h.func("@select"));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                long[] expecteds = new long[] { 2, 3 };
                assertKeepalivesInt(thread, expecteds);

                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();

    }

    @Test
    public void testConv() throws InterruptedException {
        InterpreterStack stack = h.makeStack(h.func("@conv"),
                IntBox(0x9abcdef0L), IntBox(0x123456789abcdef0L),
                FloatBox(42.0F), DoubleBox(42.0));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                List<ValueBox> kas = thread.getStack().getTop()
                        .dumpKeepAlives();

                assertEquals(0x9abcdef0L, ((IntBox) kas.get(0)).getValue()
                        .longValue());
                assertEquals(0x9abcdef0L, ((IntBox) kas.get(1)).getValue()
                        .longValue());
                assertEquals(0xffffffff9abcdef0L, ((IntBox) kas.get(2))
                        .getValue().longValue());

                assertEquals(42.0F, ((FloatBox) kas.get(3)).getValue(), 0.01F);
                assertEquals(42.0, ((DoubleBox) kas.get(4)).getValue(), 0.01);

                assertEquals(42L, ((IntBox) kas.get(5)).getValue().longValue());
                assertEquals(42L, ((IntBox) kas.get(6)).getValue().longValue());
                assertEquals(1.3117684674637903e+18,
                        ((DoubleBox) kas.get(7)).getValue(), 0.00001);
                assertEquals(1.3117684674637903e+18,
                        ((DoubleBox) kas.get(8)).getValue(), 0.00001);

                assertEquals(0x4045000000000000L, ((IntBox) kas.get(9))
                        .getValue().longValue());

                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();

    }

    @Test
    public void testBranch() throws InterruptedException {
        InterpreterStack stack = h.makeStack(h.func("@branch"), IntBox(0));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                assertEquals("%traptrue", thread.getStack().getTop()
                        .getCurInst().getName());
                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();

        InterpreterStack stack2 = h.makeStack(h.func("@branch"), IntBox(44));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {
                assertEquals("%trapfalse", thread.getStack().getTop()
                        .getCurInst().getName());
                return null;
            }
        });

        InterpreterThread thread2 = microVM.newThread(stack2);
        thread2.join();
    }

    @Test
    public void testSwitchPhi() throws InterruptedException {
        long[] vals = { 0, 1, 2, 3 };
        String[] insts = { "%trapdef", "%trapone", "%traptwo", "%trapthree" };
        long[] pvs = { 10, 11, 12, 13 };

        for (int i = 0; i < 3; i++) {

            final long val = vals[i];
            final String inst = insts[i];
            final long pv = pvs[i];

            InterpreterStack stack = h.makeStack(h.func("@switch_phi"),
                    IntBox(val));

            microVM.getTrapManager().setTrapHandler(new TrapHandler() {
                @Override
                public Long onTrap(InterpreterThread thread) {

                    String instName = thread.getStack().getTop().getCurInst()
                            .getName();

                    if (instName.equals("%trapend")) {
                        List<ValueBox> kas = thread.getStack().getTop()
                                .dumpKeepAlives();

                        assertEquals(pv, ((IntBox) kas.get(0)).getValue()
                                .longValue());

                    } else {
                        assertEquals(inst, instName);
                    }
                    return null;
                }
            });

            InterpreterThread thread = microVM.newThread(stack);
            thread.join();
        }

    }

    @Test
    public void testCallRet() throws InterruptedException {

        InterpreterStack stack = h.makeStack(h.func("@call_ret"), IntBox(3),
                IntBox(4));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {

                List<ValueBox> kas = thread.getStack().getTop()
                        .dumpKeepAlives();

                assertEquals(25L, ((IntBox) kas.get(0)).getValue().longValue());

                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();
    }

    @Test
    public void testInvokeLandingpad() throws InterruptedException {

        InterpreterStack stack = h.makeStack(h.func("@invoke_landingpad"));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {

                String instName = thread.getStack().getTop().getCurInst()
                        .getName();

                if (instName.equals("%trapexc")) {
                    List<ValueBox> kas = thread.getStack().getTop()
                            .dumpKeepAlives();

                    assertEquals(0L, ((RefBox) kas.get(0)).getAddr());

                } else {
                    fail("Reached normal destination.");
                }
                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();
    }

    @Test
    public void testAggregate() throws InterruptedException {

        InterpreterStack stack = h.makeStack(h.func("@aggregate"));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {

                List<ValueBox> kas = thread.getStack().getTop()
                        .dumpKeepAlives();

                assertEquals(2L, ((IntBox) kas.get(0)).getValue().longValue());
                assertEquals(222L, ((IntBox) kas.get(1)).getValue().longValue());

                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();
    }

    @Test
    public void testAllocs() throws InterruptedException {
        InterpreterStack stack = h.makeStack(h.func("@allocs"), IntBox(10L));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread) {

                List<ValueBox> kas = thread.getStack().getTop()
                        .dumpKeepAlives();

                fail("Not implemented yet.");

                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();
    }

    // /////////////////////// Simple sum

    @Test
    @Ignore
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
