package uvm.type;

public class Stack extends Type {
    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitStack(this);
    }

}
