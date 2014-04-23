package uvm.type;

public class Thread extends Type {
    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitThread(this);
    }
    
}
