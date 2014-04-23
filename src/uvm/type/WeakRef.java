package uvm.type;

/**
 * Weak reference.
 */
public class WeakRef extends AbstractReferenceType {
    public WeakRef() {
    }

    public WeakRef(Type referenced) {
        super();
        this.referenced = referenced;
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitWeakRef(this);
    }
}
