package uvm.type;

import java.util.ArrayList;
import java.util.List;

public class Struct extends Type {
    private List<Type> fieldTypes = new ArrayList<Type>();

    public Struct() {
    }

    public Struct(List<Type> types) {
        this.fieldTypes.addAll(types);
    }

    public List<Type> getFieldTypes() {
        return fieldTypes;
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visitStruct(this);
    }
}
