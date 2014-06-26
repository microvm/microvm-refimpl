package uvm.refimpl.itpr;

public abstract class WrapperBox<T> extends ValueBox {

    protected T object;

    public WrapperBox() {
        super();
    }

    @Override
    public void copyValue(ValueBox _that) {
        @SuppressWarnings("unchecked")
        WrapperBox<T> that = (WrapperBox<T>) _that;
        this.object = that.object;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

}