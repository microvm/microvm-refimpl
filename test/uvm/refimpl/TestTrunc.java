package uvm.refimpl;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uvm.refimpl.itpr.OpHelper;

public class TestTrunc {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() {
        assertEquals(BigInteger.valueOf(0x7FFFFFFFFFFFFFFFL), OpHelper.mask(63));

        for (long i = 0; i < 128; i++) {
            System.out.format("%d %x\n", i, (11L << i));
        }

        System.out.println(1L << 64);
        System.out.println((1L << 64) - 1);

        assertEquals(new BigInteger("ffffffffffffffff", 16), OpHelper.mask(64));
    }

}
