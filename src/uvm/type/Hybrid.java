package uvm.type;

public class Hybrid extends Type {
    private Type fixedPart;
    private Type varPart;

    public Hybrid() {
        super();
    }

    public Hybrid(Type fixedPart, Type varPart) {
        super();
        this.fixedPart = fixedPart;
        this.varPart = varPart;
    }

    public Type getFixedPart() {
        return fixedPart;
    }

    public void setFixedPart(Type fixedPart) {
        this.fixedPart = fixedPart;
    }

    public Type getVarPart() {
        return varPart;
    }

    public void setVarPart(Type varPart) {
        this.varPart = varPart;
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitHybrid(this);
    }
}
