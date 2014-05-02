package uvm;

import java.util.ArrayList;
import java.util.List;

/**
 * A CFG (Control Flow Graph) is the body of a Function. It has many basic
 * blocks which then contains many instructions.
 */
public class CFG {
    private Function func;
    private List<BasicBlock> bbs = new ArrayList<BasicBlock>();
    private BasicBlock entry;

    public Function getFunc() {
        return func;
    }

    public void setFunc(Function func) {
        this.func = func;
    }

    public FunctionSignature getSig() {
        return func.getSig();
    }

    public List<BasicBlock> getBBs() {
        return bbs;
    }

    public BasicBlock getEntry() {
        return entry;
    }

    public void setEntry(BasicBlock entry) {
        this.entry = entry;
    }

}
