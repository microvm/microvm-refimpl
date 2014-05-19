package uvm.ssavalue;

import uvm.OpCode;

/**
 * An enumeration of all binary operators. Used by InstBinOp.
 */
public enum BinOptr {
    ADD(OpCode.ADD), SUB(OpCode.SUB), MUL(OpCode.MUL), UDIV(OpCode.UDIV), SDIV(
            OpCode.SDIV), UREM(OpCode.UREM), SREM(OpCode.SREM), SHL(OpCode.SHL), LSHR(
            OpCode.LSHR), ASHR(OpCode.ASHR), AND(OpCode.AND), OR(OpCode.OR), XOR(
            OpCode.XOR), FADD(OpCode.FADD), FSUB(OpCode.FSUB), FMUL(OpCode.FMUL), FDIV(
            OpCode.FDIV), FREM(OpCode.FREM);

    private final int opCode;

    private BinOptr(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }

    public static BinOptr valueByOpcode(int opc) {
        switch (opc) {
        case OpCode.ADD:
            return ADD;
        case OpCode.SUB:
            return SUB;
        case OpCode.MUL:
            return MUL;
        case OpCode.SDIV:
            return SDIV;
        case OpCode.SREM:
            return SREM;
        case OpCode.UDIV:
            return UDIV;
        case OpCode.UREM:
            return UREM;
        case OpCode.SHL:
            return SHL;
        case OpCode.LSHR:
            return LSHR;
        case OpCode.ASHR:
            return ASHR;
        case OpCode.AND:
            return AND;
        case OpCode.OR:
            return OR;
        case OpCode.XOR:
            return XOR;
        case OpCode.FADD:
            return FADD;
        case OpCode.FSUB:
            return FSUB;
        case OpCode.FMUL:
            return FMUL;
        case OpCode.FDIV:
            return FDIV;
        case OpCode.FREM:
            return FREM;
        }
        return null;
    }
}
