package uvm.type;


public abstract class AbstractReferenceType extends Type {

    /**
     * The type of the referenced object.
     */
    protected Type referenced;

    public Type getReferenced() {
        return referenced;
    }

    public void setReferenced(Type referenced) {
        this.referenced = referenced;
    }

}