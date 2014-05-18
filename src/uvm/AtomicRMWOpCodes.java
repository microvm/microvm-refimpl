package uvm;

public class AtomicRMWOpCodes {

    // AtomicRMW operators
    public static int XCHG = 0x00;
    public static int ADD = 0x01;
    public static int SUB = 0x02;
    public static int AND = 0x03;
    public static int NAND = 0x04;
    public static int OR = 0x05;
    public static int XOR = 0x06;
    public static int MAX = 0x07;
    public static int MIN = 0x08;
    public static int UMAX = 0x09;
    public static int UMIN = 0x0A;
}
