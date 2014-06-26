package uvm.refimpl.itpr;

import java.util.HashMap;

import uvm.BasicBlock;
import uvm.CFG;
import uvm.Function;
import uvm.ssavalue.Constant;
import uvm.ssavalue.DoubleConstant;
import uvm.ssavalue.FloatConstant;
import uvm.ssavalue.FunctionConstant;
import uvm.ssavalue.GlobalDataConstant;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.Parameter;
import uvm.ssavalue.StructConstant;
import uvm.ssavalue.UseBox;
import uvm.ssavalue.Value;

public class InterpreterFrame {
    private Function func;
    private CFG cfg;
    private HashMap<Value, ValueBox> valueDict = new HashMap<Value, ValueBox>();

    private BasicBlock curBb;
    private int curInstIndex;

    private InterpreterFrame prevFrame;

    public InterpreterFrame(Function func, InterpreterFrame prev) {
        this.func = func;
        this.cfg = func.getCFG();

        this.curBb = this.cfg.getEntry();
        this.curInstIndex = 0;

        this.prevFrame = prev;

        makeBoxes();
    }

    private void makeBoxes() {
        for (Parameter param : cfg.getParams()) {
            makeTypedBox(param);
        }
        for (BasicBlock bb : cfg.getBBs()) {
            for (Instruction inst : bb.getInsts()) {
                makeTypedBox(inst);
            }
        }
    }

    private void makeTypedBox(Value inst) {
        // TODO Auto-generated method stub

    }

    public Function getFunc() {
        return func;
    }

    public CFG getCfg() {
        return cfg;
    }

    public HashMap<Value, ValueBox> getValueDict() {
        return valueDict;
    }

    public BasicBlock getCurBb() {
        return curBb;
    }

    public int getCurInstIndex() {
        return curInstIndex;
    }

    public Instruction getCurInst() {
        return curBb.getInsts().get(curInstIndex);
    }

    public void incPC() {
        curInstIndex++;
    }

    public void jump(BasicBlock bb, int ix) {
        curBb = bb;
        curInstIndex = ix;
    }

    public InterpreterFrame getPrevFrame() {
        return prevFrame;
    }

    public ValueBox getValueBox(Value value) {
        return getValueDict().get(value);
    }
}
