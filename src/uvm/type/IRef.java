package uvm.type;

/**
 * Internal reference.
 */
public class IRef extends AbstractReferenceType {
    public IRef() {
    }

    public IRef(Type referenced) {
        super();
        this.referenced = referenced;
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitIRef(this);
    }
}
