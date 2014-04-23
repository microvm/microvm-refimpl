package uvm.type;

/**
 * Integer type.
 */
public class Int extends Type {
    private int size;

    public Int() {
    }

    public Int(int size) {
        super();
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitInt(this);
    }

}
