package uvm.refimpl.itpr;

public class TagRef64Box extends ValueBox {
    public static final int FP_KIND = 0;
    public static final int INT_KIND = 1;
    public static final int REF_KIND = 2;

    private int kind;

    private double fpVal;
    private long intVal;
    private long refAddr;

    public boolean isFp() {
        return kind == FP_KIND;
    }

    public boolean isInt() {
        return kind == INT_KIND;
    }

    public boolean isRef() {
        return kind == REF_KIND;
    }

    public double getFp() {
        return fpVal;
    }

    public long getInt() {
        return intVal;
    }

    public long getRef() {
        return refAddr;
    }

    public void setFp(double val) {
        fpVal = val;
        kind = FP_KIND;
    }

    public void setInt(long val) {
        intVal = val;
        kind = INT_KIND;
    }

    public void setRef(long addr) {
        refAddr = addr;
        kind = REF_KIND;
    }

    @Override
    public void copyValue(ValueBox _that) {
        TagRef64Box that = (TagRef64Box) _that;
        this.kind = that.kind;
        this.fpVal = that.fpVal;
        this.intVal = that.intVal;
        this.refAddr = that.refAddr;
    }
}
