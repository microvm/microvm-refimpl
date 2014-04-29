package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

/**
 * Load, do an operation and store the result as an atomic operation.
 */
public class InstAtomicRMW extends Instruction {

    /**
     * Requirement of memory ordering.
     */
    private AtomicOrdering ordering;

    /**
     * The operator.
     */
    private AtomicRMWOp optr;

    /**
     * The type the location property refers to.
     */
    private Type referentType;

    /**
     * The reference to store into.
     */
    private UseBox location;

    /**
     * The right-hand-side operand
     */
    private UseBox opnd;

    public InstAtomicRMW() {
        super();
    }

    public InstAtomicRMW(AtomicOrdering ordering, AtomicRMWOp optr,
            Type referentType, Value location, Value opnd) {
        super();
        this.ordering = ordering;
        this.optr = optr;
        this.referentType = referentType;
        this.location = use(location);
        this.opnd = use(opnd);
    }

    public AtomicOrdering getOrdering() {
        return ordering;
    }

    public void setOrdering(AtomicOrdering ordering) {
        this.ordering = ordering;
    }

    public AtomicRMWOp getOptr() {
        return optr;
    }

    public void setOptr(AtomicRMWOp optr) {
        this.optr = optr;
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

    public Value getOpnd() {
        return opnd.getDst();
    }

    public void setOpnd(Value opnd) {
        assertNotReset(this.opnd);
        this.opnd = use(opnd);
    }

    @Override
    public int opcode() {
        return OpCode.ATOMICRMW;
    }

    @Override
    public Type getType() {
        return referentType;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitAtomicRMW(this);
    }

}
