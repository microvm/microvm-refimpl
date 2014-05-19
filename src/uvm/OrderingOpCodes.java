package uvm;

public class OrderingOpCodes {
    // Memory ordering
    public static final int NOT_ATOMIC = 0x00;
    public static final int UNORDERED = 0x01;
    public static final int MONOTONIC = 0x02;
    public static final int ACQUIRE = 0x03;
    public static final int RELEASE = 0x04;
    public static final int ACQ_REL = 0x05;
    public static final int SEQ_CST = 0x06;

}
