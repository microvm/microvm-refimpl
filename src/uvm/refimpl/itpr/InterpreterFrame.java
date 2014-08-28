package uvm.refimpl.itpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uvm.BasicBlock;
import uvm.CFG;
import uvm.Function;
import uvm.IdentifiedHelper;
import uvm.ssavalue.HasKeepAlives;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.Parameter;
import uvm.ssavalue.UseBox;
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
    private CFG cfg;
    private HashMap<Value, ValueBox> valueDict = new HashMap<Value, ValueBox>();

    private BasicBlock curBb;
    private int curInstIndex;

    private InterpreterFrame prevFrame;
    private long savedStackPointer;

    public InterpreterFrame(Function func, InterpreterFrame prev) {
        this.cfg = func.getCFG();

        this.curBb = this.cfg.getEntry();
        this.curInstIndex = 0;

        this.prevFrame = prev;
        this.savedStackPointer = 0;

        makeBoxes();
    }

    private void makeBoxes() {
        for (Parameter param : cfg.getParams()) {
            maybePutBox(param);
        }
        for (BasicBlock bb : cfg.getBBs()) {
            for (Instruction inst : bb.getInsts()) {
                maybePutBox(inst);
            }
        }
    }

    private void maybePutBox(Value value) {
        Type type = value.getType();
        if (type != null) {
            valueDict.put(value, makeTypedBox(type));
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
            return box;
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
    }

    public Function getFunc() {
        return cfg.getFunc();
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

    public long getSavedStackPointer() {
        return savedStackPointer;
    }

    public void setSavedStackPointer(long savedStackPointer) {
        this.savedStackPointer = savedStackPointer;
    }

    public ValueBox getValueBox(Value value) {
        return getValueDict().get(value);
    }

    public List<ValueBox> dumpKeepAlives() {
        Instruction inst = getCurInst();
        if (inst instanceof HasKeepAlives) {
            HasKeepAlives hka = (HasKeepAlives) inst;
            List<UseBox> kas = hka.getKeepAlives();
            ArrayList<ValueBox> boxes = new ArrayList<ValueBox>(kas.size());
            for (UseBox ub : kas) {
                boxes.add(getValueBox(ub.getDst()));
            }
            return boxes;
        } else {
            ErrorUtils.uvmError("Instruction does not have keepalives: "
                    + IdentifiedHelper.repr(inst));
            return null;
        }
    }

}
