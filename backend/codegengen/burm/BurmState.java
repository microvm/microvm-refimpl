package burm;

import uvm.irtree.IRTreeNode;

public class BurmState {
    IRTreeNode node;
    BurmState[] leaves;
    static final short INFINITE = Short.MAX_VALUE;
    short[] cost;
    short[] rule; 

	public void record(int nt, int cost, int ruleno) {
      if (cost < this.cost[nt]) {
        this.cost[nt] = (short) cost;
        this.rule[nt] = (short) ruleno;
      }
    }
    
    public IRTreeNode getNode() {
      return node;
    }
    
    public BurmState getLeaf(int i) {
      return leaves[i];
    }
    
    public short getCost(int i) {
      return cost[i];
    }
    
    public short getRule(int i) {
      return rule[i];
    }
    
    public String prettyPrint() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < cost.length; i++) {
            builder.append("rule" + rule[i] + "=" + cost[i]);
            if (i != cost.length - 1)
                builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }
}