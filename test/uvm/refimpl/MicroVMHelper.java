package uvm.refimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uvm.Function;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.itpr.DoubleBox;
import uvm.refimpl.itpr.FloatBox;
import uvm.refimpl.itpr.FuncBox;
import uvm.refimpl.itpr.IRefBox;
import uvm.refimpl.itpr.IntBox;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.RefBox;
import uvm.refimpl.itpr.StackBox;
import uvm.refimpl.itpr.StructBox;
import uvm.refimpl.itpr.ThreadBox;
import uvm.refimpl.itpr.ValueBox;
import uvm.type.Int;
import uvm.type.Ref;
import uvm.type.Type;

public class MicroVMHelper {
    private MicroVM microVM;

    public MicroVMHelper(MicroVM microVM) {
        super();
        this.microVM = microVM;
    }

    public Function func(int fid) {
        return microVM.getGlobalBundle().getFuncNs().getByID(fid);
    }

    public Function func(String fName) {
        return microVM.getGlobalBundle().getFuncNs().getByName(fName);
    }

    public InterpreterStack makeStack(Function func, ValueBox... args) {
        return microVM.newStack(func, Arrays.asList(args));
    }

    public static IntBox IntBox(long value) {
        IntBox box = new IntBox();
        box.setValue(value);
        return box;
    }

    public static FloatBox FloatBox(float value) {
        FloatBox box = new FloatBox();
        box.setValue(value);
        return box;
    }

    public static DoubleBox DoubleBox(double value) {
        DoubleBox box = new DoubleBox();
        box.setValue(value);
        return box;
    }

    public static RefBox RefBox(long addr) {
        RefBox box = new RefBox();
        box.setAddr(addr);
        return box;
    }

    public static IRefBox IRefBox(long base, long offset) {
        IRefBox box = new IRefBox();
        box.setBase(base);
        box.setOffset(offset);
        return box;
    }

    public static StructBox StructBox(ValueBox... boxes) {
        StructBox box = new StructBox();
        for (ValueBox b : boxes) {
            box.addBox(b);
        }
        return box;
    }

    public static FuncBox FuncBox(Function func) {
        FuncBox box = new FuncBox();
        box.setFunc(func);
        return box;
    }

    public static ThreadBox ThreadBox(InterpreterThread thr) {
        ThreadBox box = new ThreadBox();
        box.setThread(thr);
        return box;
    }

    public static StackBox StackBox(InterpreterStack sta) {
        StackBox box = new StackBox();
        box.setStack(sta);
        return box;
    }
}
