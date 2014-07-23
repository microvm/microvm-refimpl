package uvm.refimpl.mem.simpleimmix;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uvm.Bundle;
import uvm.ir.text.input.TestingHelper;
import uvm.refimpl.DumbClient;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.facade.MicroVMClient;
import uvm.refimpl.mem.scanning.ObjectMarker;
import uvm.type.Hybrid;

public class TestSimpleImmixHeapWithLargerHeap {

    private static MicroVM microVM;
    private static MicroVMClient client = new DumbClient() {
        @Override
        public void markExternalRoots(ObjectMarker marker) {
            testMarker.markExternalRoots(marker);
        }
    };
    private static Bundle bundle;
    private static SimpleImmixHeap heap;
    private static SimpleImmixMutator mutator;

    private static interface Marker {
        public void markExternalRoots(ObjectMarker marker);
    }

    private static Marker testMarker;

    private static Marker DO_NOTHING_MARKER = new Marker() {
        @Override
        public void markExternalRoots(ObjectMarker marker) {
        }
    };

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            microVM = new MicroVM();
            microVM.setClient(client);
            bundle = TestingHelper
                    .parseUir("tests/uvm-refimpl-test/primitives.uir");
            microVM.addBundle(bundle);
            heap = microVM.getMemoryManager().getHeap();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Before
    public void setUp() {
        mutator = heap.makeMutator();
        testMarker = DO_NOTHING_MARKER;
    }

    @After
    public void cleanUp() {
        mutator.close();

        microVM.getMemoryManager().getHeap().mutatorTriggerAndWaitForGCEnd(false);
    }

    @Test
    public void testLOS() {
        Hybrid ca = (Hybrid) bundle.getTypeNs().getByName("@hCharArray");

        final long unitLen = 128 * 1024;
        final int units = 15;
        final long[] as = new long[units];

        testMarker = new Marker() {
            @Override
            public void markExternalRoots(ObjectMarker marker) {
                for (int i = 0; i < units; i++) {
                    marker.markObjRef(as[i]);
                }
            }
        };

        for (int i = 0; i < units; i++) {
            long a = mutator.newHybrid(ca, unitLen);
            as[i] = a;
            System.out.format("as[%d] = %d\n", i, a);
        }

        testMarker = new Marker() {
            @Override
            public void markExternalRoots(ObjectMarker marker) {
                for (int i = 8; i < 9; i++) {
                    marker.markObjRef(as[i]);
                }
            }
        };

        System.out.format("Allocating a relatively larger object...\n");
        long lo = mutator.newHybrid(ca, 1 * 1024 * 1024);
        System.out.format("lo = %d\n", lo);

    }
}
