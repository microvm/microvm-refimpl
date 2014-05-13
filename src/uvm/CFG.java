package uvm;

import java.util.ArrayList;
import java.util.List;

import uvm.ssavalue.Instruction;
import uvm.ssavalue.Parameter;

/**
 * A CFG (Control Flow Graph) is the body of a Function. It has many basic
 * blocks which then contains many instructions.
 */
public class CFG {
    /**
     * The associated Function object.
     */
    private Function func;

    /**
     * The formal parameter list.
     */
    private List<Parameter> params = new ArrayList<Parameter>();

    /**
     * A list of all basic blocks.
     */
    private List<BasicBlock> bbs = new ArrayList<BasicBlock>();

    /**
     * The entry block.
     */
    private BasicBlock entry;

    /**
     * Basic block namespace.
     */
    private Namespace<BasicBlock> bbNs = new SimpleNamespace<BasicBlock>();

    /**
     * Instruction (local SSA Value) namespace.
     */
    private Namespace<Instruction> instNs = new SimpleNamespace<Instruction>();

    public Function getFunc() {
        return func;
    }

    public void setFunc(Function func) {
        this.func = func;
    }

    public FunctionSignature getSig() {
        return func.getSig();
    }

    public List<Parameter> getParams() {
        return params;
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

    public Namespace<BasicBlock> getBBNs() {
        return bbNs;
    }

    public Namespace<Instruction> getInstNs() {
        return instNs;
    }
}
