package uvm.type;

/**
 * 64-bit floating point type.
 */
public class Double extends FPType {

    public Double() {
        super();
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitDouble(this);
    }
}
