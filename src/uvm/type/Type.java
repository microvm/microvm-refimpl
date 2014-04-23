package uvm.type;

import uvm.Identified;

/**
 * Supertype of all µVM types.
 */
public abstract class Type implements Identified {
    private int id;
    private String name;

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

    public abstract <T> T accept(TypeVisitor<T> visitor);
}
