package uvm;

import java.util.ArrayList;
import java.util.List;

public class FunctionSignature implements Identified {
    private int id;
    private String name;

    private Type returnType;
    private List<Type> paramTypes = new ArrayList<Type>();

    public FunctionSignature() {
    }

    public FunctionSignature(Type returnType, List<Type> paramTypes) {
        this.returnType = returnType;
        this.paramTypes.addAll(paramTypes);
    }

    @Override
    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public List<Type> getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(List<Type> paramTypes) {
        this.paramTypes = paramTypes;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(returnType);
        str.append(" (");
        for (int i = 0; i < paramTypes.size(); i++) {
            str.append(paramTypes.get(i));
            if (i != paramTypes.size() - 1)
                str.append(", ");
        }
        str.append(")");

        return str.toString();
    }

    public String prettyPrint() {
        return toString();
    }
}
