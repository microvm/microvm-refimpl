package uvm.inst;

import uvm.OpCode;

/**
 * An enumeration of all binary operators. Used by InstBinOp.
 */
public enum BinOptr {
    ADD(OpCode.ADD),
    SUB(OpCode.SUB),
    MUL(OpCode.MUL),
    UDIV(OpCode.UDIV),
    SDIV(OpCode.SDIV),
    UREM(OpCode.UREM),
    SREM(OpCode.SREM),
    SHL(OpCode.SHL),
    LSHR(OpCode.LSHR),
    ASHR(OpCode.ASHR),
    AND(OpCode.AND),
    OR(OpCode.OR),
    XOR(OpCode.XOR),
    FADD(OpCode.FADD),
    FSUB(OpCode.FSUB),
    FMUL(OpCode.FMUL),
    FDIV(OpCode.FDIV),
    FREM(OpCode.FREM);
    
    private final int opCode;

    private BinOptr(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }
}
