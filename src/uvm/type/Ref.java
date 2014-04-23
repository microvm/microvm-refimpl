package uvm.type;


/**
 * Object reference type.
 */
public class Ref extends AbstractReferenceType {
    public Ref() {
    }

    public Ref(Type referenced) {
        super();
        this.referenced = referenced;
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitRef(this);
    }

}
