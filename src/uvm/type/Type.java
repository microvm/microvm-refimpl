package uvm.type;

import uvm.IdentifiedSettable;

/**
 * Supertype of all µVM types.
 */
public abstract class Type implements IdentifiedSettable {
    private int id;
    private String name;

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public abstract <T> T accept(TypeVisitor<T> visitor);
}
