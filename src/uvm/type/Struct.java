package uvm.type;

import java.util.ArrayList;
import java.util.List;

public class Struct extends Type {
    private List<Type> types = new ArrayList<Type>();

    public Struct() {
    }

    public Struct(List<Type> types) {
        this.types.addAll(types);
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitStruct(this);
    }
}
