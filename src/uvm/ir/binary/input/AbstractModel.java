package uvm.ir.binary.input;

/**
 * An abstract model for all definitions.
 * <p>
 * This is basically a LISP-style "list of anything".
 */
public class AbstractModel {
    public int opcode;
    public int id;
    public Object[] others;

    /**
     * The µVM object to construct from this abstract model.
     */
    public Object resultObj;

    public AbstractModel(int opcode, int id, Object... others) {
        super();
        this.opcode = opcode;
        this.id = id;
        this.others = others;
    }

    public AbstractModel() {
        super();
    }

}
