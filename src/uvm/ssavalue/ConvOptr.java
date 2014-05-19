package uvm.ssavalue;

import uvm.OpCode;

/**
 * An enumeration of all conversion operators. Used in InstConversion.
 */
public enum ConvOptr {
    TRUNC(OpCode.TRUNC), ZEXT(OpCode.ZEXT), SEXT(OpCode.SEXT), FPTRUNC(
            OpCode.FPTRUNC), FPEXT(OpCode.FPEXT), FPTOUI(OpCode.FPTOUI), FPTOSI(
            OpCode.FPTOSI), UITOFP(OpCode.UITOFP), SITOFP(OpCode.SITOFP), BITCAST(
            OpCode.BITCAST), REFCAST(OpCode.REFCAST), IREFCAST(OpCode.IREFCAST), FUNCCAST(
            OpCode.FUNCCAST);

    private final int opCode;

    private ConvOptr(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }

    public static ConvOptr valueByOpcode(int opc) {
        switch (opc) {
        case OpCode.TRUNC:
            return TRUNC;
        case OpCode.ZEXT:
            return ZEXT;
        case OpCode.SEXT:
            return SEXT;
        case OpCode.FPTRUNC:
            return FPTRUNC;
        case OpCode.FPEXT:
            return FPEXT;
        case OpCode.FPTOUI:
            return FPTOUI;
        case OpCode.FPTOSI:
            return FPTOSI;
        case OpCode.UITOFP:
            return UITOFP;
        case OpCode.SITOFP:
            return SITOFP;
        case OpCode.BITCAST:
            return BITCAST;
        case OpCode.REFCAST:
            return REFCAST;
        case OpCode.IREFCAST:
            return IREFCAST;
        case OpCode.FUNCCAST:
            return FUNCCAST;
        }
        return null;
    }
}
