package uvm.refimpl.itpr;

import java.util.HashMap;
import java.util.Map;

import uvm.IdentifiedHelper;
import uvm.ssavalue.Constant;
import uvm.ssavalue.DoubleConstant;
import uvm.ssavalue.FloatConstant;
import uvm.ssavalue.FunctionConstant;
import uvm.ssavalue.GlobalDataConstant;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.StructConstant;
import uvm.type.Func;
import uvm.type.IRef;
import uvm.type.Ref;
import uvm.type.Stack;
import uvm.util.ErrorUtils;

public class ConstantPool {
    private Map<Constant, ValueBox> boxes = new HashMap<Constant, ValueBox>();

    public ConstantPool() {
    }

    public void addConstant(Constant constant) {
        maybeMakeConstBox(constant);
    }

    private ValueBox maybeMakeConstBox(Constant constant) {
        if (boxes.containsKey(constant)) {
            return boxes.get(constant);
        } else {
            ValueBox box = makeConstBox(constant);
            boxes.put(constant, box);
            return box;
        }

    }

    private ValueBox makeConstBox(Constant constant) {

        if (constant instanceof IntConstant) {
            IntConstant c = (IntConstant) constant;
            IntBox b = new IntBox();
            b.setValue(c.getValue());
            return b;
        } else if (constant instanceof FloatConstant) {
            FloatConstant c = (FloatConstant) constant;
            FloatBox b = new FloatBox();
            b.setValue(c.getValue());
            return b;
        } else if (constant instanceof DoubleConstant) {
            FloatConstant c = (FloatConstant) constant;
            FloatBox b = new FloatBox();
            b.setValue(c.getValue());
            return b;
        } else if (constant instanceof StructConstant) {
            StructConstant c = (StructConstant) constant;
            StructBox b = new StructBox();
            for (Constant cc : c.getValues()) {
                ValueBox cb = maybeMakeConstBox(cc);
                b.addBox(cb);
            }
            return b;
        } else if (constant instanceof NullConstant) {
            NullConstant c = (NullConstant) constant;

            if (c.getType() instanceof Ref) {
                RefBox b = new RefBox();
                b.setAddr(0);
                return b;
            } else if (c.getType() instanceof IRef) {
                IRefBox b = new IRefBox();
                b.setBase(0);
                b.setOffset(0);
                return b;
            } else if (c.getType() instanceof Func) {
                FuncBox b = new FuncBox();
                b.setFunc(null);
                return b;
            } else if (c.getType() instanceof uvm.type.Thread) {
                ThreadBox b = new ThreadBox();
                b.setThread(null);
                return b;
            } else if (c.getType() instanceof Stack) {
                StackBox b = new StackBox();
                b.setStack(null);
                return b;
            } else {
                ErrorUtils.uvmError("Bad constant type "
                        + IdentifiedHelper.repr(c.getType()));
                return null;
            }
        } else if (constant instanceof FunctionConstant) {
            FunctionConstant c = (FunctionConstant) constant;
            FuncBox b = new FuncBox();
            b.setFunc(c.getFunction());
            return b;
        } else if (constant instanceof GlobalDataConstant) {
            ErrorUtils.uvmError("TODO: Needs GlobalMemory implementation");
            return null;
        } else {
            ErrorUtils.uvmError("Unexpected constant type "
                    + constant.getClass().getName());
            return null;
        }

    }

    public ValueBox getValueBox(Constant value) {
        return boxes.get(value);
    }
}
