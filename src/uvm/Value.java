package uvm;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the base class of all SSA Values. An SSA Value can be either a
 * constant, a parameter or an instruction.
 * <p>
 * Lists of {@link UseBox} can be retrieved to find all other Values used by the
 * current Value and all other Values using the current Value.
 */
public abstract class Value implements Identified, HasOpCode {

    /**
     * The globally unique identifier to the value.
     */
    private int id;

    /**
     * An optional register name. Set if it is assigned to a register.
     */
    private String name = null;

    /**
     * All other Values used by this Value.
     */
    protected List<UseBox> uses = new ArrayList<UseBox>();

    /**
     * All other Values that uses this value.
     */
    protected List<UseBox> usedBy = new ArrayList<UseBox>();

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

    public void setName(String regName) {
        this.name = regName;
    }

    public List<UseBox> getUses() {
        return uses;
    }

    public List<UseBox> getUsedBy() {
        return usedBy;
    }

    /**
     * A convenient binding for Usebox.use.
     * 
     * @param that
     *            The other SSA Value used by this.
     * @return a UseBox
     */
    public UseBox use(Value that) {
        return UseBox.use(this, that);
    }

    /**
     * Assert that a UseBox field is not assigned twice, otherwise there will be
     * two UseBoxes for a user.
     * 
     * @param useBoxField
     *            The UseBox field which should be null.
     */
    protected void assertNotReset(UseBox useBoxField) {
        if (useBoxField != null) {
            throw new IllegalStateException(
                    "Cannot reset value. Please change the usebox, instead.");
        }
    }

    /**
     * Get the type of the current SSA Value.
     * 
     * @return A Type object. If this is an instruction and only has side effect
     *         (e.g. Store, Return, Branch, etc.), return null.
     */
    public abstract Type getType();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String main = String.format("%s%s", this.getClass().getSimpleName(),
                IdentifiedHelper.repr(this));
        sb.append(main);

        for (UseBox ub : uses) {
            Value child = ub.getDst();
            sb.append(" ").append(IdentifiedHelper.repr(child));
        }
        return sb.toString();
    }
}
