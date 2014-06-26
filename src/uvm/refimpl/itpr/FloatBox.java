package uvm.refimpl.itpr;

public class FloatBox extends ValueBox {
    private float value;

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public void copyValue(ValueBox that) {
        this.value = ((FloatBox) that).getValue();
    }

}
