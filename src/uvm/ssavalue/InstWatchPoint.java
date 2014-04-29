package uvm.ssavalue;

import java.util.List;

import uvm.BasicBlock;
import uvm.OpCode;
import uvm.type.Type;

/**
 * A trap which can be enabled later during execution.
 */
public class InstWatchPoint extends AbstractTrap {
    /**
     * The watch point ID.
     */
    private int watchPointId;

    /**
     * The basic block to branch to when the watch point is not enabled.
     */
    private BasicBlock disabled;

    public InstWatchPoint() {
        super();
    }

    public InstWatchPoint(int watchPointId, Type type, BasicBlock disabled,
            BasicBlock nor, BasicBlock exc, List<Value> keepAlives) {
        super(type, nor, exc, keepAlives);
        this.watchPointId = watchPointId;
        this.disabled = disabled;
    }

    public int getWatchPointId() {
        return watchPointId;
    }

    public void setWatchPointId(int watchPointId) {
        this.watchPointId = watchPointId;
    }

    public BasicBlock getDisabled() {
        return disabled;
    }

    public void setDisabled(BasicBlock disabled) {
        this.disabled = disabled;
    }

    @Override
    public int opcode() {
        return OpCode.WATCHPOINT;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.acceptWatchPoint(this);
    }

}
