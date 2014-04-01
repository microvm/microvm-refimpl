package uvm;

/**
 * The UseBox represents a "use" relation between SSA Values. One SSA Value
 * "uses" another SSA Value if the former refers to the latter as an operand.
 * <p>
 * The purpose is to make it convenient to substitute one Value with another and
 * also update all uses.
 */
public class UseBox {
    private Value src;
    private Value dst;

    public Value getSrc() {
        return src;
    }

    public void setSrc(Value src) {
        this.src = src;
    }

    public Value getDst() {
        return dst;
    }

    public void setDst(Value dst) {
        this.dst = dst;
    }

    public UseBox(Value src, Value dst) {
        super();
        this.src = src;
        this.dst = dst;
    }

    /**
     * Create a UseBox and automatically link the src and dst's uses and usedBy
     * fields to this box.
     * 
     * @param src the user
     * @param dst the used value
     * @return a new UseBox
     */
    public static UseBox use(Value src, Value dst) {
        UseBox useBox = new UseBox(src, dst);
        src.uses.add(useBox);
        dst.usedBy.add(useBox);
        return useBox;
    }
}
