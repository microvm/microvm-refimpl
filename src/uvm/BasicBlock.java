package uvm;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock implements Identified {
    private int id;
    private String name;

    private CFG cfg;
    private List<Instruction> insts = new ArrayList<Instruction>();

    public BasicBlock(CFG cfg) {
        this.cfg = cfg;
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
    
    public CFG getCfg() {
        return cfg;
    }

    public void addInstruction(Instruction i) {
        this.insts.add(i);
    }

    public List<Instruction> getInsts() {
        return insts;
    }

}
