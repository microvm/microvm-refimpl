package uvm.ssavalue;

import uvm.OpCode;

/**
 * An enumeration of all comparison operators. Used by InstCmp.
 */
public enum CmpOptr {
    EQ(OpCode.EQ), NE(OpCode.NE), ULT(OpCode.ULT), ULE(OpCode.ULE), UGT(
            OpCode.UGT), UGE(OpCode.UGE), SLT(OpCode.SLT), SLE(OpCode.SLE), SGT(
            OpCode.SGT), SGE(OpCode.SGE), FTRUE(OpCode.FTRUE), FFALSE(
            OpCode.FFALSE), FORD(OpCode.FORD), FOEQ(OpCode.FOEQ), FONE(
            OpCode.FONE), FOLT(OpCode.FOLT), FOLE(OpCode.FOLE), FOGT(
            OpCode.FOGT), FOGE(OpCode.FOGE), FUNO(OpCode.FUNO), FUEQ(
            OpCode.FUEQ), FUNE(OpCode.FUNE), FULT(OpCode.FULT), FULE(
            OpCode.FULE), FUGT(OpCode.FUGT), FUGE(OpCode.FUGE);

    private final int opCode;

    private CmpOptr(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }

    public static CmpOptr valueByOpcode(int opc) {
        switch (opc) {
        case OpCode.EQ:
            return EQ;
        case OpCode.NE:
            return NE;
        case OpCode.SGE:
            return SGE;
        case OpCode.SGT:
            return SGT;
        case OpCode.SLE:
            return SLE;
        case OpCode.SLT:
            return SLT;
        case OpCode.UGE:
            return UGE;
        case OpCode.UGT:
            return UGT;
        case OpCode.ULE:
            return ULE;
        case OpCode.ULT:
            return ULT;
        case OpCode.FFALSE:
            return FFALSE;
        case OpCode.FTRUE:
            return FTRUE;
        case OpCode.FUNO:
            return FUNO;
        case OpCode.FUEQ:
            return FUEQ;
        case OpCode.FUNE:
            return FUNE;
        case OpCode.FUGT:
            return FUGT;
        case OpCode.FUGE:
            return FUGE;
        case OpCode.FULT:
            return FULT;
        case OpCode.FULE:
            return FULE;
        case OpCode.FORD:
            return FORD;
        case OpCode.FOEQ:
            return FOEQ;
        case OpCode.FONE:
            return FONE;
        case OpCode.FOGT:
            return FOGT;
        case OpCode.FOGE:
            return FOGE;
        case OpCode.FOLT:
            return FOLT;
        case OpCode.FOLE:
            return FOLE;
        }
        return null;
    }
}
