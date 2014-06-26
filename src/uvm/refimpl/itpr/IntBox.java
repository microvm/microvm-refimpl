package uvm.refimpl.itpr;

public class IntBox extends ValueBox {
    private long value = 0;

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public void copyValue(ValueBox that) {
        this.value = ((IntBox) that).getValue();
    }

}
