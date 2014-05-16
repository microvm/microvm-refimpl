package uvm.ir.text.input;

import org.junit.Test;

public class RealWorldParsingTest extends BundleTester {

    @Override
    protected String bundleName() {
        return "tests/micro-bm/int-prime-number/prime-number.uir";
    }

    @Test
    public void testParsing() {
        // It is okay as long as it runs.
    }
}
