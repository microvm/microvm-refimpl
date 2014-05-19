package uvm;

import uvm.type.Type;

/**
 * An instance of GlobalData represents an instance of a type allocated in the
 * global memory.
 */
public class GlobalData implements IdentifiedSettable {
    private int id;
    private String name;

    /**
     * The type of the data in the memory.
     */
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public int getID() {
        return this.id;
    }

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
