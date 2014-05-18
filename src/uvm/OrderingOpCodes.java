package uvm;

public class OrderingOpCodes {
    // Memory ordering
    public static int NOT_ATOMIC = 0x00;
    public static int UNORDERED = 0x01;
    public static int MONOTONIC = 0x02;
    public static int ACQUIRE = 0x03;
    public static int RELEASE = 0x04;
    public static int ACQ_REL = 0x05;
    public static int SEQ_CST = 0x06;

}
