package uvm.inst;

import java.util.HashMap;
import java.util.Map;

import uvm.BasicBlock;
import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.UseBox;
import uvm.Value;
import uvm.ValueVisitor;

/**
 * A phi-node in the SSA form. Its value depends on the incoming control flow.
 */
public class InstPhi extends Instruction {
    /**
     * The type of this instruction.
     */
    private Type type;

    /**
     * A map from basic blocks and use boxes. Each use box contains the value
     * from that basic block.
     */
    private HashMap<BasicBlock, UseBox> valueMap = new HashMap<BasicBlock, UseBox>();

    public InstPhi() {
    }

    /**
     * Create a new Phi node from a give BasicBlock-Value map.
     * 
     * @param type
     *            The type of the node.
     * @param values
     *            A map from each source basic block to an SSA Value
     */
    public InstPhi(Type type, Map<BasicBlock, Value> values) {
        this.type = type;

        for (Map.Entry<BasicBlock, Value> e : values.entrySet()) {
            this.setValueFrom(e.getKey(), e.getValue());
        }
    }

    /**
     * Get the SSA Value for the control flow from a given basic block.
     * 
     * @param bb
     *            the basic block.
     * @return the SSA Value associated to that block.
     */
    public Value getValueFrom(BasicBlock bb) {
        return valueMap.get(bb).getDst();
    }

    /**
     * Add a new control flow transfer case for the current phi-node. The
     * control flow from basic block bb will be associated with value v.
     * <p>
     * A UseBox will be automatically created.
     * 
     * @param bb
     *            The source basic block.
     * @param v
     *            The SSA Value corresponding to that basic block.
     */
    public void setValueFrom(BasicBlock bb, Value v) {
        assertNotReset(this.valueMap.get(bb));
        this.valueMap.put(bb, use(v));
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    
    @Override
    public int opcode() {
        return OpCode.PHI;
    }
    
    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitPhi(this);
    }
}
