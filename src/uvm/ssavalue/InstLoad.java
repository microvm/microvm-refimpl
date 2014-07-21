package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Ref;
import uvm.type.Type;
import uvm.type.WeakRef;

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
    
    /**
     * The type of this instruction, may be Ref if referentType is WeakRef
     */
    private Type type;

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
        if (referentType instanceof WeakRef) {
            Ref type = new Ref();
            type.setReferenced(referentType);
            this.type = type;
        } else {
            this.type = referentType;
        }
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
        return type;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitLoad(this);
    }

}
