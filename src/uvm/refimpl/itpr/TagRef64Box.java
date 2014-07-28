package uvm.refimpl.itpr;

import java.math.BigInteger;

public class TagRef64Box extends ValueBox implements HasObjRef {
    private long bits;

    public boolean isFp() {
        return OpHelper.tr64IsFp(bits);

    }

    public boolean isInt() {
        return OpHelper.tr64IsInt(bits);
    }

    public boolean isRef() {
        return OpHelper.tr64IsRef(bits);
    }

    public double getFp() {
        return OpHelper.tr64ToFp(bits);
    }

    public BigInteger getInt() {
        return BigInteger.valueOf(OpHelper.tr64ToInt(bits));
    }

    public long getRef() {
        return OpHelper.tr64ToRef(bits);
    }

    public BigInteger getTag() {
        return BigInteger.valueOf(OpHelper.tr64ToTag(bits));
    }

    public void setFp(double val) {
        bits = OpHelper.fpToTr64(val);
    }

    public void setInt(BigInteger val) {
        bits = OpHelper.intToTr64(val.longValue());
    }

    public void setRef(long addr, BigInteger tag) {
        bits = OpHelper.refToTr64(addr, tag.longValue());
    }

    @Override
    public void copyValue(ValueBox _that) {
        TagRef64Box that = (TagRef64Box) _that;
        this.bits = that.bits;
    }

    @Override
    public long getObjRef() {
        return getRef();
    }

    @Override
    public void setObjRef(long objRef) {
        BigInteger tag = getTag();
        setRef(objRef, tag);
    }

    public long getBits() {
        return this.bits;
    }

    public void setBits(long bits) {
        this.bits = bits;
    }
}
