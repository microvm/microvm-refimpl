package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

/**
 * Store into a memory location.
 */
public class InstStore extends Instruction {

    /**
     * Requirement of atomicity and memory ordering.
     */
    private AtomicOrdering ordering;

    /**
     * The type the location property refers to.
     */
    private Type referentType;

    /**
     * The reference to store into.
     */
    private UseBox location;

    /**
     * The new value to store.
     */
    private UseBox newVal;

    public InstStore() {
        super();
    }

    public InstStore(AtomicOrdering ordering, Type referentType,
            Value location, Value newVal) {
        super();
        this.ordering = ordering;
        this.referentType = referentType;
        this.location = use(location);
        this.newVal = use(newVal);
    }

    public AtomicOrdering getOrdering() {
        return ordering;
    }

    public void setOrdering(AtomicOrdering ordering) {
        this.ordering = ordering;
    }

    public Type getReferentType() {
        return referentType;
    }

    public void setReferentType(Type referentType) {
        this.referentType = referentType;
    }

    public Value getLocation() {
        return location.getDst();
    }

    public void setLocation(Value location) {
        assertNotReset(this.location);
        this.location = use(location);
    }

    public Value getNewVal() {
        return newVal.getDst();
    }

    public void setNewVal(Value newVal) {
        assertNotReset(this.newVal);
        this.newVal = use(newVal);
    }

    @Override
    public int opcode() {
        return OpCode.STORE;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitStore(this);
    }

}
