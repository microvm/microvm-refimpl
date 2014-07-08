package uvm.refimpl.itpr;

import java.math.BigInteger;

public class IntBox extends ValueBox {
    private BigInteger value = BigInteger.ZERO;

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    @Override
    public void copyValue(ValueBox that) {
        this.value = ((IntBox) that).getValue();
    }

}
