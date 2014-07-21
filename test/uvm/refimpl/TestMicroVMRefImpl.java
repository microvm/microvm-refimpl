package uvm.refimpl;

import static org.junit.Assert.*;
import static uvm.platformsupport.Config.*;
import static uvm.refimpl.MicroVMHelper.*;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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
import uvm.refimpl.itpr.DoubleBox;
import uvm.refimpl.itpr.FloatBox;
import uvm.refimpl.itpr.FuncBox;
import uvm.refimpl.itpr.IRefBox;
import uvm.refimpl.itpr.IntBox;
import uvm.refimpl.itpr.InterpreterFrame;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.RefBox;
import uvm.refimpl.itpr.TrapHandler;
import uvm.refimpl.itpr.ValueBox;
import uvm.refimpl.mem.TypeSizes;
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
                public Long onTrap(InterpreterThread thread,
                        ValueBox trapValueBox) {

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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {

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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {

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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {

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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
                List<ValueBox> kas = thread.getStack().getTop()
                        .dumpKeepAlives();

                long addrNew = ((RefBox) kas.get(0)).getAddr();
                long addrNewHybrid = ((RefBox) kas.get(1)).getAddr();
                long addrAlloca = ((IRefBox) kas.get(2)).getAddr();
                long addrAllocaHybrid = ((IRefBox) kas.get(3)).getAddr();

                int tidNew = getTID(addrNew);
                int tidNewHybrid = getTID(addrNewHybrid);
                int tidAlloca = getTID(addrAlloca);
                int tidAllocaHybrid = getTID(addrAllocaHybrid);

                // How can I check if an address is in heap or stack when stacks
                // are byte arrays in the LOS?

                assertEquals(h.type("@StructBar").getID(), tidNew);
                assertEquals(h.type("@hCharArray").getID(), tidNewHybrid);
                assertEquals(h.type("@StructBar").getID(), tidAlloca);
                assertEquals(h.type("@hCharArray").getID(), tidAllocaHybrid);

                return null;
            }

            private int getTID(long addr) {
                long tag = MEMORY_SUPPORT.loadLong(addr
                        + TypeSizes.GC_HEADER_OFFSET_TAG);
                return (int) (tag & 0xffffffffl);
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();
    }

    @Test
    public void testMemAddressing() throws InterruptedException {
        InterpreterStack stack = h.makeStack(h.func("@memAddressing"));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
                List<ValueBox> kas = thread.getStack().getTop()
                        .dumpKeepAlives();

                long addrBarRef = ((RefBox) kas.get(0)).getAddr();
                long addrBarIRef = ((IRefBox) kas.get(1)).getAddr();
                long addrBar3 = ((IRefBox) kas.get(2)).getAddr();
                long addrBazIRef = ((IRefBox) kas.get(3)).getAddr();
                long addrBaz3 = ((IRefBox) kas.get(4)).getAddr();
                long addrBaz6 = ((IRefBox) kas.get(5)).getAddr();
                long addrJaRef = ((RefBox) kas.get(6)).getAddr();
                long addrJaIRef = ((IRefBox) kas.get(7)).getAddr();
                long addrJaFix = ((IRefBox) kas.get(8)).getAddr();
                long addrJaVar = ((IRefBox) kas.get(9)).getAddr();

                assertEquals(addrBarRef, addrBarIRef);
                assertEquals(addrBarRef + 14L, addrBar3);

                assertEquals(addrBazIRef + 6L, addrBaz3);
                assertEquals(addrBazIRef + 12L, addrBaz6);

                assertEquals(addrJaRef, addrJaIRef);
                assertEquals(addrJaIRef, addrJaFix);
                assertEquals(addrJaIRef + 4L, addrJaVar);

                return null;
            }

        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();
    }

    @Test
    public void testMemAccessing() throws InterruptedException {
        InterpreterStack stack = h.makeStack(h.func("@memAccessing"));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
                List<ValueBox> kas = thread.getStack().getTop()
                        .dumpKeepAlives();

                long vr = ((RefBox) kas.get(0)).getAddr();
                long vir = ((IRefBox) kas.get(1)).getAddr();
                long l8 = ((IntBox) kas.get(2)).getValue().longValue();
                long l16 = ((IntBox) kas.get(3)).getValue().longValue();
                long l32 = ((IntBox) kas.get(4)).getValue().longValue();
                long l64 = ((IntBox) kas.get(5)).getValue().longValue();
                float lf = ((FloatBox) kas.get(6)).getValue();
                double ld = ((DoubleBox) kas.get(7)).getValue();
                long lr = ((RefBox) kas.get(8)).getAddr();
                long lir = ((IRefBox) kas.get(9)).getAddr();
                long lwr = ((RefBox) kas.get(10)).getAddr();
                Function lfunc = ((FuncBox) kas.get(11)).getFunc();

                assertEquals(41L, l8);
                assertEquals(42L, l16);
                assertEquals(43L, l32);
                assertEquals(44L, l64);
                assertEquals(45.0f, lf, 0.001f);
                assertEquals(46.0d, ld, 0.001d);
                assertEquals(vr, lr);
                assertEquals(vir, lir);
                assertEquals(vr, lwr);
                assertEquals(h.func("@memAccessing"), lfunc);

                return null;
            }

        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();
    }

    @Test
    public void testMemAccessingAtomic() throws InterruptedException {
        InterpreterStack stack = h.makeStack(h.func("@memAccessingAtomic"));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
                List<ValueBox> kas = thread.getStack().getTop()
                        .dumpKeepAlives();

                long vr = ((RefBox) kas.get(0)).getAddr();
                long vr2 = ((RefBox) kas.get(1)).getAddr();
                long vr3 = ((RefBox) kas.get(2)).getAddr();
                long cx32_1 = ((IntBox) kas.get(3)).getValue().longValue();
                long cx32_2 = ((IntBox) kas.get(4)).getValue().longValue();
                long cx64_1 = ((IntBox) kas.get(5)).getValue().longValue();
                long cx64_2 = ((IntBox) kas.get(6)).getValue().longValue();
                long l32 = ((IntBox) kas.get(7)).getValue().longValue();
                long l64 = ((IntBox) kas.get(8)).getValue().longValue();
                long cxr_1 = ((RefBox) kas.get(9)).getAddr();
                long cxr_2 = ((RefBox) kas.get(10)).getAddr();
                long lr = ((RefBox) kas.get(11)).getAddr();

                assertEquals(43L, cx32_1);
                assertEquals(53L, cx32_2);
                assertEquals(44L, cx64_1);
                assertEquals(54L, cx64_2);
                assertEquals(53L, l32);
                assertEquals(54L, l64);
                assertEquals(vr, cxr_1);
                assertEquals(vr2, cxr_2);
                assertEquals(vr2, lr);

                long rmw0 = ((IntBox) kas.get(12)).getValue().longValue();
                long rmw1 = ((IntBox) kas.get(13)).getValue().longValue();
                long rmw2 = ((IntBox) kas.get(14)).getValue().longValue();
                long rmw3 = ((IntBox) kas.get(15)).getValue().longValue();
                long rmw4 = ((IntBox) kas.get(16)).getValue().longValue();
                long rmw5 = ((IntBox) kas.get(17)).getValue().longValue();
                long rmw6 = ((IntBox) kas.get(18)).getValue().longValue();
                long rmw7 = ((IntBox) kas.get(19)).getValue().longValue();
                long rmw8 = ((IntBox) kas.get(20)).getValue().longValue();
                long rmw9 = ((IntBox) kas.get(21)).getValue().longValue();
                long rmwA = ((IntBox) kas.get(22)).getValue().longValue();
                long l64_2 = ((IntBox) kas.get(23)).getValue().longValue();

                assertEquals(1, rmw0);
                assertEquals(0x55ab, rmw1);
                assertEquals(0x55ae, rmw2);
                assertEquals(0x55aa, rmw3);
                assertEquals(0x500a, rmw4);
                assertEquals(~0x500a, rmw5);
                assertEquals(~0x000a, rmw6);
                assertEquals(~0x55a0, rmw7);
                assertEquals(-0x7fffffffffffffdel, rmw8);
                assertEquals(42, rmw9);
                assertEquals(11, rmwA);
                assertEquals(0xffffffffffffffdel, l64_2);

                return null;
            }

        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();
    }

    @Test
    public void testWatchPoint() throws InterruptedException {
        microVM.getTrapManager().newWatchpoint(42);

        InterpreterStack stack = h.makeStack(h.func("@watchpointtest"));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
                assertEquals("%trapdis", thread.getStack().getTop()
                        .getCurInst().getName());
                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();

        microVM.getTrapManager().enableWatchpoint(42);

        stack = h.makeStack(h.func("@watchpointtest"));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {

                String trapName = thread.getStack().getTop().getCurInst()
                        .getName();
                if (trapName.equals("%wp")) {
                    ((IntBox) trapValueBox).setValue(BigInteger.valueOf(99));
                } else {
                    assertEquals("%trapena", trapName);
                    List<ValueBox> kas = thread.getStack().getTop()
                            .dumpKeepAlives();
                    long ka0 = ((IntBox) kas.get(0)).getValue().longValue();
                    assertEquals(99L, ka0);
                }
                return null;
            }
        });

        thread = microVM.newThread(stack);
        thread.join();

        stack = h.makeStack(h.func("@watchpointtest"));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {

                String trapName = thread.getStack().getTop().getCurInst()
                        .getName();
                if (trapName.equals("%wp")) {
                    return 0L;
                } else {
                    assertEquals("%trapena", trapName);
                    List<ValueBox> kas = thread.getStack().getTop()
                            .dumpKeepAlives();
                    long ka0 = ((RefBox) kas.get(0)).getAddr();
                    assertEquals(0L, ka0);
                }
                return null;
            }
        });

        thread = microVM.newThread(stack);
        thread.join();
    }

    @Test
    public void testSwapStack() throws InterruptedException {

        InterpreterStack stack = h.makeStack(h.func("@testswapstack"));

        microVM.getTrapManager().setTrapHandler(new TrapHandler() {

            private long expValue = 0L;

            @Override
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
                List<ValueBox> kas = thread.getStack().getTop()
                        .dumpKeepAlives();
                long value = ((IntBox) kas.get(0)).getValue().longValue();
                long shouldStop = ((IntBox) kas.get(1)).getValue().longValue();

                System.out.format("TRAP: Expect %d, seen %d, shouldStop=%d\n",
                        expValue, value, shouldStop);

                if (expValue < 3) {
                    assertEquals(expValue, value);
                    assertEquals(0, shouldStop);
                    expValue++;
                } else {
                    assertEquals(1, shouldStop);
                }
                return null;
            }
        });

        InterpreterThread thread = microVM.newThread(stack);
        while (thread.isRunning()) {
            System.out.format(
                    "%s: %s: %s\n",
                    thread.getStack().getID(),
                    IdentifiedHelper.repr(thread.getStack().getTop()
                            .getCurInst()), thread.getStack().getTop()
                            .getCurInst().getClass().getName());

            thread.step();
        }

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
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
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
