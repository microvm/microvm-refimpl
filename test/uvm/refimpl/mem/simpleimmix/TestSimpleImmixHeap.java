package uvm.refimpl.mem.simpleimmix;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import uvm.Bundle;
import uvm.ir.text.input.TestingHelper;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.facade.MicroVMClient;
import uvm.type.Hybrid;
import uvm.type.Int;
import uvm.type.Struct;

public class TestSimpleImmixHeap {

    private static final long BEGIN = 0x100000;
    private static final long SIZE = 0x20000;

    public static MicroVM microVM;
    public static Bundle bundle;
    public static SimpleImmixHeap heap;
    public static SimpleImmixMutator mutator;

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            microVM = new MicroVM(128 * 1024, 32 * 1024, 32 * 1024);
            bundle = TestingHelper
                    .parseUir("tests/uvm-refimpl-test/primitives.uir");
            microVM.addBundle(bundle);
            heap = microVM.getMemoryManager().getHeap();
            mutator = heap.makeMutator();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testBasicAlloc() {
        Int i64 = (Int) bundle.getTypeNs().getByName("@i64");
        Struct structFoo = (Struct) bundle.getTypeNs().getByName("@StructFoo");
        Hybrid ca = (Hybrid) bundle.getTypeNs().getByName("@hCharArray");

        long a1 = mutator.newScalar(structFoo);
        long a2 = mutator.newScalar(i64);
        long a3 = mutator.newHybrid(ca, 100L);
        long a4 = mutator.newScalar(i64);

        System.out.format("%d %d %d %d\n", a1, a2, a3, a4);
        assertTrue(a1 < a2);
        assertTrue(a2 < a3);
        assertTrue(a3 < a4);
    }

    @Test
    public void testMoreAlloc() {
        Hybrid ca = (Hybrid) bundle.getTypeNs().getByName("@hCharArray");

        final long unitLen = 4096;
        final int units = 400;
        final long[] as = new long[units];

        microVM.setClient(new MicroVMClient() {
            @Override
            public List<Long> extraRoots() {
                ArrayList<Long> ls = new ArrayList<Long>();
                int j = 0;
                for (int i = units / 2; i < units; i++) {
                    if (as[j] != 0) {
                        ls.add(as[j]);
                    }
                    j++;
                }
                return ls;
            }

        });

        for (int i = 0; i < units; i++) {
            long a = mutator.newHybrid(ca, unitLen);
            as[i] = a;
            System.out.format("as[%d] = %d\n", i, a);
        }
    }
}
