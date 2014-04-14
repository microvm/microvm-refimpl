package uvm;

public class IntConstant extends Constant {
    Type type;
    long value;

    public IntConstant(Type type, long value) {
        this.type = type;
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return this.type;
    }
    
    @Override
    public String toString() {
        return super.toString() + ": " + type + " = " + value;
    }
    
    @Override
    public int opcode() {
        return OpCode.INT_IMM;
    }
    
    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitIntConstant(this);
    }
}
