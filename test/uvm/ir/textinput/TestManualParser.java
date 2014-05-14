package uvm.ir.textinput;

import static org.junit.Assert.assertEquals;
import static uvm.ir.textinput.IntParsingUtils.manualParse;

import org.junit.Test;

public class TestManualParser {
    @Test
    public void testManualParser() {
        assertEquals(42L, manualParse("42", 10));
        assertEquals(0x42L, manualParse("42", 16));
        assertEquals(042L, manualParse("42", 8));
        assertEquals(0xFFFFFFFFFFFFFFFFL, manualParse("FFFFFFFFFFFFFFFF", 16));
        assertEquals(0x7FFFFFFFFFFFFFFFL, manualParse("7FFFFFFFFFFFFFFF", 16));
        assertEquals(0x0L, manualParse("10000000000000000", 16));
        assertEquals(0x1L, manualParse("10000000000000001", 16));
    }
}
