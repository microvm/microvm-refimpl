package uvm.type;

public class TagRef64 extends Type {
    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitTagRef64(this);
    }
}
