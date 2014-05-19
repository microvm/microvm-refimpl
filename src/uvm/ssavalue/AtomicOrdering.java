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

    public static AtomicOrdering valueByOpcode(int opc) {
        switch (opc) {
        case OrderingOpCodes.NOT_ATOMIC:
            return NOT_ATOMIC;
        case OrderingOpCodes.UNORDERED:
            return UNORDERED;
        case OrderingOpCodes.MONOTONIC:
            return MONOTONIC;
        case OrderingOpCodes.ACQUIRE:
            return ACQUIRE;
        case OrderingOpCodes.RELEASE:
            return RELEASE;
        case OrderingOpCodes.ACQ_REL:
            return ACQ_REL;
        case OrderingOpCodes.SEQ_CST:
            return SEQ_CST;
        }
        return null;
    }
}
