package uvm;

public abstract class Type implements Identified {
    private int id;
    private String name;

    protected Type() {
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

    public abstract int size();

    public abstract String prettyPrint();
}
