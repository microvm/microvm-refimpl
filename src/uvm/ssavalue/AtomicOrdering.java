package uvm.ssavalue;

import uvm.OrderingOpCodes;

public enum AtomicOrdering {
    NOT_ATOMIC(OrderingOpCodes.NOT_ATOMIC), //
    UNORDERED(OrderingOpCodes.UNORDERED), //
    MONOTONIC(OrderingOpCodes.MONOTONIC), //
    ACQUIRE(OrderingOpCodes.ACQUIRE), //
    RELEASE(OrderingOpCodes.RELEASE), //
    ACQ_REL(OrderingOpCodes.ACQ_REL), //
    SEQ_CST(OrderingOpCodes.SEQ_CST);

    private final int opCode;

    private AtomicOrdering(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }
}
