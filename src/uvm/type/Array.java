package uvm.type;

public class Array extends Type {
    private Type elemType;
    private int length;

    public Array() {
    }

    public Array(Type elemType, int length) {
        super();
        this.elemType = elemType;
        this.length = length;
    }

    public Type getElemType() {
        return elemType;
    }

    public void setElemType(Type elemType) {
        this.elemType = elemType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitArray(this);
    }
}
