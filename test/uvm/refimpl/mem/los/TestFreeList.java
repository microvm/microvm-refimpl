package uvm.refimpl.mem.los;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import uvm.util.LogUtil;

public class TestFreeList {
    
    @BeforeClass
    public static void setUpClass() {
        LogUtil.enableLoggers("FreeList");
    }

    @Test
    public void testAllocSingle() {
        final int SZ = 10;
        FreeList fl = new FreeList(SZ);
        int ss[] = new int[SZ];
        for (int i = 0; i < 10; i++) {
            fl.debugPrintList();
            System.out.printf("Allocating %d...\n", i);
            ss[i] = fl.allocate(1);
            System.out.printf("ss[%d] = %d\n", i, ss[i]);
        }
        fl.debugPrintList();

        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                assertNotEquals(ss[i], ss[j]);
            }
        }

        for (int i = 0; i < 10; i++) {
            int i2 = (i + 5) % 10;
            fl.debugPrintList();
            System.out.printf("Deallocating %d...\n", ss[i2]);
            fl.deallocate(ss[i2]);
        }

        for (int i = 0; i < 10; i++) {
            fl.debugPrintList();
            System.out.printf("Allocating %d...\n", i);
            ss[i] = fl.allocate(1);
        }

        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                assertNotEquals(ss[i], ss[j]);
            }
        }

    }

    @Test
    public void testAllocMulti() {
        final int SZ = 30;
        final int GRAN = 3;
        FreeList fl = new FreeList(SZ);
        int ss[] = new int[SZ];
        for (int i = 0; i < 10; i++) {
            fl.debugPrintList();
            System.out.printf("Allocating %d...\n", i);
            ss[i] = fl.allocate(GRAN);
            System.out.printf("ss[%d] = %d\n", i, ss[i]);
        }
        fl.debugPrintList();

        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                assertNotEquals(ss[i], ss[j]);
            }
        }

        for (int i = 0; i < 10; i++) {
            int i2 = (i + 5) % 10;
            fl.debugPrintList();
            System.out.printf("Deallocating %d...\n", ss[i2]);
            fl.deallocate(ss[i2]);
        }

        for (int i = 0; i < 10; i++) {
            fl.debugPrintList();
            System.out.printf("Allocating %d...\n", i);
            ss[i] = fl.allocate(GRAN);
        }

        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                assertNotEquals(ss[i], ss[j]);
            }
        }

    }

    @Test
    public void testAllocMultiPartial() {
        final int SZ = 15;
        final int GRAN = 3;
        FreeList fl = new FreeList(SZ);
        int ss[] = new int[SZ];
        for (int i = 0; i < 4; i++) {
            fl.debugPrintList();
            System.out.printf("Allocating %d...\n", i);
            ss[i] = fl.allocate(GRAN);
            System.out.printf("ss[%d] = %d\n", i, ss[i]);
        }
        fl.debugPrintList();

        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                assertNotEquals(ss[i], ss[j]);
            }
        }

        for (int i = 2; i < 4; i++) {
            fl.debugPrintList();
            System.out.printf("Deallocating %d...\n", ss[i]);
            fl.deallocate(ss[i]);
        }

        for (int i = 2; i < 5; i++) {
            fl.debugPrintList();
            System.out.printf("Allocating %d...\n", i);
            ss[i] = fl.allocate(GRAN);
        }

        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                assertNotEquals(ss[i], ss[j]);
            }
        }

    }

    @Test
    public void testNotEnoughMemory() {
        final int SZ = 5;
        FreeList fl = new FreeList(SZ);
        int ss[] = new int[SZ];

        ss[0] = fl.allocate(3);
        ss[1] = fl.allocate(2);
        assertNotEquals(ss[0], ss[1]);

        ss[2] = fl.allocate(3);
        assertEquals(-1, ss[2]);
    }

    @Test
    public void testFragmentation() {
        final int SZ = 10;
        FreeList fl = new FreeList(SZ);
        int ss[] = new int[SZ];

        for (int i = 0; i < SZ; i++) {
            ss[i] = fl.allocate(1);
        }

        for (int i = 0; i < SZ; i += 2) {
            fl.deallocate(ss[i]);
        }

        int canIGetIt = fl.allocate(2);
        assertEquals(-1, canIGetIt);
    }

}
