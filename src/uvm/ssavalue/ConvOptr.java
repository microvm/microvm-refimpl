package uvm.ssavalue;

import uvm.OpCode;

/**
 * An enumeration of all conversion operators. Used in InstConversion.
 */
public enum ConvOptr {
    TRUNC(OpCode.TRUNC),
    ZEXT(OpCode.ZEXT),
    SEXT(OpCode.SEXT),
    FPTRUNC(OpCode.FPTRUNC),
    FPEXT(OpCode.FPEXT),
    FPTOUI(OpCode.FPTOUI),
    FPTOSI(OpCode.FPTOSI),
    UITOFP(OpCode.UITOFP),
    SITOFP(OpCode.SITOFP),
    BITCAST(OpCode.BITCAST),
    REFCAST(OpCode.REFCAST),
    IREFCAST(OpCode.IREFCAST);
    
    private final int opCode;

    private ConvOptr(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }
}
