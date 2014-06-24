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
                for (UseBox ub : inst.getUses()) {
                    Value dst = ub.getDst();
                    if (dst instanceof Constant) {
                        ValueBox constBox = makeConstBox((Constant) dst);
                        // TODO: Assign this to a dict.
                    }
                }
            }
        }
    }

    private ValueBox makeConstBox(Constant constant) {
        ValueBox theBox = null;

        if (constant instanceof IntConstant) {
            IntConstant c = (IntConstant) constant;
            IntBox b = new IntBox();
            b.setValue(c.getValue());
            theBox = b;
        } else if (constant instanceof FloatConstant) {
            FloatConstant c = (FloatConstant) constant;
            FloatBox b = new FloatBox();
            b.setValue(c.getValue());
            theBox = b;
        } else if (constant instanceof DoubleConstant) {
            FloatConstant c = (FloatConstant) constant;
            FloatBox b = new FloatBox();
            b.setValue(c.getValue());
            theBox = b;
        } else if (constant instanceof StructConstant) {
            StructConstant c = (StructConstant) constant;
            StructBox b = new StructBox();
            for (Constant cc : c.getValues()) {
                ValueBox cb = makeConstBox(cc);
                b.addBox(cb);
            }
            theBox = b;
        } else if (constant instanceof NullConstant) {
            NullConstant c = (NullConstant) constant;
            
            FloatBox b = new FloatBox();
        } else if (constant instanceof FunctionConstant) {
            FloatConstant c = (FloatConstant) constant;
            FloatBox b = new FloatBox();
            b.setValue(c.getValue());
            theBox = b;
        } else if (constant instanceof GlobalDataConstant) {
            
        }

        return theBox;
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
