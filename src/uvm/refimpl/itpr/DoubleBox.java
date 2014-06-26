package uvm.refimpl.itpr;

public class DoubleBox extends ValueBox {
    private double value;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public void copyValue(ValueBox that) {
        this.value = ((DoubleBox) that).getValue();
    }
    
}
