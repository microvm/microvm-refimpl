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
}
