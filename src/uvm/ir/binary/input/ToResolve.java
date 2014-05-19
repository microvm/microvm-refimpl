package uvm.ir.binary.input;

/**
 * A container for all µVM objects that have IDs to resolve after object
 * creation.
 */
public class ToResolve<T> {
    public T resultObj;
    public int[] ids;
    public int[] ids2;
    public int[] ids3;

    /**
     * The µVM object to construct from this abstract model.
     */

    public ToResolve(T resultObj, int... ids) {
        super();
        this.resultObj = resultObj;
        this.ids = ids;
    }

    public ToResolve(T resultObj, int[] ids2, int[] ids3, int... ids) {
        super();
        this.resultObj = resultObj;
        this.ids = ids;
        this.ids2 = ids2;
        this.ids3 = ids3;
    }

    public ToResolve() {
        super();
    }

}
