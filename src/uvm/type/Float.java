package uvm.type;

/**
 * 32-bit floating point type.
 */
public class Float extends FPType {
    
    protected Float() {
        super();
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitFloat(this);
    }
}
