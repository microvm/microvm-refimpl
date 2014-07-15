package uvm.refimpl.itpr;

public class IRefBox extends ValueBox implements HasObjRef {
    private long base;
    private long offset;

    public long getBase() {
        return base;
    }

    public void setBase(long base) {
        this.base = base;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getAddr() {
        return this.base + this.offset;
    }

    @Override
    public void copyValue(ValueBox _that) {
        IRefBox that = (IRefBox) _that;
        this.base = that.getBase();
        this.offset = that.getOffset();
    }

    @Override
    public long getObjRef() {
        return getBase();
    }

    @Override
    public void setObjRef(long objRef) {
        setBase(objRef);
    }
}
