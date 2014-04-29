package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

/**
 * Load from a memory location.
 */
public class InstLoad extends Instruction {

    /**
     * Requirement of atomicity and memory ordering.
     */
    private AtomicOrdering ordering;

    /**
     * The type the location property refers to.
     */
    private Type referentType;

    /**
     * The reference to load from.
     */
    private UseBox location;

    public InstLoad() {
        super();
    }

    public InstLoad(AtomicOrdering ordering, Type referentType, Value location) {
        super();
        this.ordering = ordering;
        this.referentType = referentType;
        this.location = use(location);
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

    @Override
    public int opcode() {
        return OpCode.LOAD;
    }

    @Override
    public Type getType() {
        return referentType;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitLoad(this);
    }

}
