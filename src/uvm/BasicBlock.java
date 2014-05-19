package uvm;

import java.util.ArrayList;
import java.util.List;

import uvm.ssavalue.Instruction;

public class BasicBlock implements IdentifiedSettable {
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

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
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
