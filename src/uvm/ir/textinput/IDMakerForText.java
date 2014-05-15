package uvm.ir.textinput;

public class IDMakerForText {

    // ID facilities

    private int nextId = 65536;

    /**
     * Create a new globally unique ID.
     * 
     * @return A unique ID for everything: types, signatures, functions,
     *         instructions, ...
     */
    public int makeID() {
        int thisId = nextId++;
        return thisId;
    }

    public static IDMakerForText INSTANCE = new IDMakerForText();
}
