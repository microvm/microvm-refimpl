package uvm.refimpl.itpr;

import java.util.HashMap;

import uvm.BasicBlock;
import uvm.CFG;
import uvm.Function;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.Parameter;
import uvm.ssavalue.Value;
import uvm.type.Func;
import uvm.type.IRef;
import uvm.type.Int;
import uvm.type.Ref;
import uvm.type.Struct;
import uvm.type.TagRef64;
import uvm.type.Type;
import uvm.util.ErrorUtils;

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
            valueDict.put(param, makeTypedBox(param.getType()));
        }
        for (BasicBlock bb : cfg.getBBs()) {
            for (Instruction inst : bb.getInsts()) {
                valueDict.put(inst, makeTypedBox(inst.getType()));
            }
        }
    }

    private ValueBox makeTypedBox(Type type) {
        if (type instanceof Int) {
            return new IntBox();
        } else if (type instanceof uvm.type.Float) {
            return new FloatBox();
        } else if (type instanceof uvm.type.Double) {
            return new DoubleBox();
        } else if (type instanceof Ref) {
            return new RefBox();
        } else if (type instanceof IRef) {
            return new IRefBox();
        } else if (type instanceof Struct) {
            Struct structType = (Struct) type;
            StructBox box = new StructBox();
            for (Type fieldType : structType.getFieldTypes()) {
                ValueBox fieldBox = makeTypedBox(fieldType);
                box.addBox(fieldBox);
            }
        } else if (type instanceof Func) {
            return new FuncBox();
        } else if (type instanceof uvm.type.Thread) {
            return new ThreadBox();
        } else if (type instanceof uvm.type.Stack) {
            return new StackBox();
        } else if (type instanceof TagRef64) {
            return new TagRef64Box();
        } else if (type instanceof uvm.type.Void) {
            return new VoidBox();
        } else {
            ErrorUtils.uvmError("Unknown type to create type: "
                    + type.getClass().getName());
            return null;
        }
        return null;
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
