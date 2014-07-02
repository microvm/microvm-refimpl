package uvm.refimpl.itpr;

public class TrapResponse {
    public static final int NORMAL = 1;
    public static final int EXCEPTIONAL = 2;
    
    private int answer = NORMAL;
    private long exceptionAddr;

    public void raiseException(long exceptionAddr) {
        this.exceptionAddr = exceptionAddr;
    }

    public int getAnswer() {
        return answer;
    }

    public long getExceptionAddr() {
        return exceptionAddr;
    }
}
