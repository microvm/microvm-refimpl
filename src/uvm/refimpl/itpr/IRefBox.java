package uvm.refimpl.itpr;

public class IRefBox extends ValueBox {
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
}
