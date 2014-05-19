package uvm;

public class AtomicRMWOpCodes {

    // AtomicRMW operators
    public static final int XCHG = 0x00;
    public static final int ADD = 0x01;
    public static final int SUB = 0x02;
    public static final int AND = 0x03;
    public static final int NAND = 0x04;
    public static final int OR = 0x05;
    public static final int XOR = 0x06;
    public static final int MAX = 0x07;
    public static final int MIN = 0x08;
    public static final int UMAX = 0x09;
    public static final int UMIN = 0x0A;
}
