package uvm.type;

import java.util.List;

import uvm.Type;

public class Struct extends Type {
    List<Type> types;
    int size = 0;
    
    protected Struct(List<Type> types) {
        super();
        this.types = types;
        // FIXME: need to care about alignment
        for (Type t : types)
            size += t.size();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append("struct<");
        for (int i = 0; i < types.size(); i++) {
            ret.append(types.get(i).prettyPrint());
            if (i != types.size() - 1)
                ret.append(" ");
        }
        ret.append(">");
        return ret.toString();
    }

}