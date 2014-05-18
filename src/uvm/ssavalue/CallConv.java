package uvm.ssavalue;

public enum CallConv {
    DEFAULT(0);
    
    private final int opCode;

    private CallConv(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }
}
