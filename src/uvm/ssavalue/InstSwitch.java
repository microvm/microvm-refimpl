package uvm.ssavalue;

import java.util.HashMap;
import java.util.Map;

import uvm.BasicBlock;
import uvm.OpCode;
import uvm.type.Type;

/**
 * Multi-way branching.
 */
public class InstSwitch extends Instruction {
    /**
     * The type of the operand.
     */
    private Type opndType;

    /**
     * The value to compare against.
     */
    private UseBox opnd;

    /**
     * The default destination. i.e. The destination if no case matches.
     */
    private BasicBlock defaultDest;

    /**
     * A map from basic blocks and use boxes. Each use box contains the value
     * from that basic block.
     */
    private HashMap<UseBox, BasicBlock> cases = new HashMap<UseBox, BasicBlock>();

    public InstSwitch() {
    }

    /**
     * Create a new Switch instruction from a Value-BasicBlock mapping. This
     * will automatically "use" all Values in the cases parameter.
     */
    public InstSwitch(Type opndType, Value opnd, BasicBlock defaultTarget,
            Map<Value, BasicBlock> cases) {
        this.opndType = opndType;
        this.opnd = use(opnd);
        this.defaultDest = defaultTarget;
        for (Map.Entry<Value, BasicBlock> e : cases.entrySet()) {
            this.cases.put(use(e.getKey()), e.getValue());
        }
    }

    public Type getOpndType() {
        return opndType;
    }

    public void setOpndType(Type opndType) {
        this.opndType = opndType;
    }

    public Value getOpnd() {
        return opnd.getDst();
    }

    public void setOpnd(Value opnd) {
        assertNotReset(this.opnd);
        this.opnd = use(opnd);
    }

    public BasicBlock getDefaultDest() {
        return defaultDest;
    }

    public void setDefaultDest(BasicBlock defaultDest) {
        this.defaultDest = defaultDest;
    }

    /**
     * Add a case for a given value.
     * <p>
     * A UseBox will be automatically created.
     * 
     * @param bb
     *            The source basic block.
     * @param v
     *            The SSA Value corresponding to that basic block.
     */
    public void setDestFor(Value theCase, BasicBlock dest) {
        this.cases.put(use(theCase), dest);
    }

    public HashMap<UseBox, BasicBlock> getCases() {
        return cases;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public int opcode() {
        return OpCode.SWITCH;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitSwitch(this);
    }
}
