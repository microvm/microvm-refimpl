package uvm.ssavalue;

import uvm.OpCode;
import uvm.type.Type;

/**
 * Store if and only if the original value is as expected.
 */
public class InstCmpXchg extends Instruction {

    /**
     * Ordering when the CMPXCHG operation succeeded.
     */
    private AtomicOrdering orderingSucc;

    /**
     * Ordering when the CMPXCHG operation failed.
     */
    private AtomicOrdering orderingFail;

    /**
     * The type the location property refers to.
     */
    private Type referentType;

    /**
     * The reference to store into.
     */
    private UseBox location;

    /**
     * The expected old value.
     */
    private UseBox expected;

    /**
     * The desired new value.
     */
    private UseBox desired;

    public InstCmpXchg() {
        super();
    }

    public InstCmpXchg(AtomicOrdering orderingSucc,
            AtomicOrdering orderingFail, Type referentType, Value location,
            Value expected, Value desired) {
        super();
        this.orderingSucc = orderingSucc;
        this.orderingFail = orderingFail;
        this.referentType = referentType;
        this.location = use(location);
        this.expected = use(expected);
        this.desired = use(desired);
    }

    public AtomicOrdering getOrderingSucc() {
        return orderingSucc;
    }

    public void setOrderingSucc(AtomicOrdering orderingSucc) {
        this.orderingSucc = orderingSucc;
    }

    public AtomicOrdering getOrderingFail() {
        return orderingFail;
    }

    public void setOrderingFail(AtomicOrdering orderingFail) {
        this.orderingFail = orderingFail;
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

    public Value getExpected() {
        return expected.getDst();
    }

    public void setExpected(Value expected) {
        assertNotReset(this.expected);
        this.expected = use(expected);
    }

    public Value getDesired() {
        return desired.getDst();
    }

    public void setDesired(Value desired) {
        assertNotReset(this.desired);
        this.desired = use(desired);
    }

    @Override
    public int opcode() {
        return OpCode.CMPXCHG;
    }

    @Override
    public Type getType() {
        return referentType;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitCmpXchg(this);
    }

}
