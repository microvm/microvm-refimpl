package uvm.ssavalue;

import uvm.AtomicRMWOpCodes;

public enum AtomicRMWOp {
    XCHG(AtomicRMWOpCodes.XCHG), //
    ADD(AtomicRMWOpCodes.ADD), //
    SUB(AtomicRMWOpCodes.SUB), //
    AND(AtomicRMWOpCodes.AND), //
    NAND(AtomicRMWOpCodes.NAND), //
    OR(AtomicRMWOpCodes.OR), //
    XOR(AtomicRMWOpCodes.XOR), //
    MIN(AtomicRMWOpCodes.MIN), //
    MAX(AtomicRMWOpCodes.MAX), //
    UMIN(AtomicRMWOpCodes.UMIN), //
    UMAX(AtomicRMWOpCodes.UMAX);

    private final int opCode;

    private AtomicRMWOp(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }

    public static AtomicRMWOp valueByOpcode(int opc) {
        switch (opc) {
        case AtomicRMWOpCodes.XCHG:
            return XCHG;
        case AtomicRMWOpCodes.ADD:
            return ADD;
        case AtomicRMWOpCodes.SUB:
            return SUB;
        case AtomicRMWOpCodes.AND:
            return AND;
        case AtomicRMWOpCodes.NAND:
            return NAND;
        case AtomicRMWOpCodes.OR:
            return OR;
        case AtomicRMWOpCodes.XOR:
            return XOR;
        case AtomicRMWOpCodes.MIN:
            return MIN;
        case AtomicRMWOpCodes.MAX:
            return MAX;
        case AtomicRMWOpCodes.UMIN:
            return UMIN;
        case AtomicRMWOpCodes.UMAX:
            return UMAX;

        }
        return null;
    }
}
