package uvm.refimpl.itpr;

public class RefBox extends ValueBox implements HasObjRef {
    private long addr;

    public long getAddr() {
        return addr;
    }

    public void setAddr(long addr) {
        this.addr = addr;
    }

    @Override
    public void copyValue(ValueBox that) {
        this.addr = ((RefBox) that).getAddr();
    }

    @Override
    public long getObjRef() {
        return getAddr();
    }

    @Override
    public void setObjRef(long objRef) {
        setAddr(objRef);
    }

}
