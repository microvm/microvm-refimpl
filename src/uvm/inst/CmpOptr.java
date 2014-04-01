package uvm.inst;

import uvm.OpCode;

/**
 * An enumeration of all comparison operators. Used by InstCmp.
 */
public enum CmpOptr {
    EQ(OpCode.EQ),
    NE(OpCode.NE),
    ULT(OpCode.ULT),
    ULE(OpCode.ULE),
    UGT(OpCode.UGT),
    UGE(OpCode.UGE),
    SLT(OpCode.SLT),
    SLE(OpCode.SLE),
    SGT(OpCode.SGT),
    SGE(OpCode.SGE),
    FTRUE(OpCode.FTRUE),
    FFALSE(OpCode.FFALSE),
    FORD(OpCode.FORD),
    FOEQ(OpCode.FOEQ),
    FONE(OpCode.FONE),
    FOLT(OpCode.FOLT),
    FOLE(OpCode.FOLE),
    FOGT(OpCode.FOGT),
    FOGE(OpCode.FOGE),
    FUNO(OpCode.FUNO),
    FUEQ(OpCode.FUEQ),
    FUNE(OpCode.FUNE),
    FULT(OpCode.FULT),
    FULE(OpCode.FULE),
    FUGT(OpCode.FUGT),
    FUGE(OpCode.FUGE);
    
    private final int opCode;

    private CmpOptr(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }
}
