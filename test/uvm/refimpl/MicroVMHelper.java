package uvm.refimpl;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import uvm.Function;
import uvm.IdentifiedHelper;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.itpr.DoubleBox;
import uvm.refimpl.itpr.FloatBox;
import uvm.refimpl.itpr.FuncBox;
import uvm.refimpl.itpr.IRefBox;
import uvm.refimpl.itpr.IntBox;
import uvm.refimpl.itpr.InterpreterFrame;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.RefBox;
import uvm.refimpl.itpr.StackBox;
import uvm.refimpl.itpr.StructBox;
import uvm.refimpl.itpr.ThreadBox;
import uvm.refimpl.itpr.ValueBox;
import uvm.refimpl.itpr.VoidBox;
import uvm.ssavalue.Value;

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
        box.setValue(BigInteger.valueOf(value));
        return box;
    }

    public static IntBox BigIntBox(BigInteger value) {
        IntBox box = new IntBox();
        box.setValue(value);
        return box;
    }

    public static IntBox BigIntBox(String s, int radix) {
        IntBox box = new IntBox();
        box.setValue(new BigInteger(s, radix));
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

    public static void printStackTrace(InterpreterStack sta) {
        for (InterpreterFrame fra = sta.getTop(); fra != null; fra = fra
                .getPrevFrame()) {
            printFrame(fra);
            System.out.println();
        }
    }

    public static void printFrame(InterpreterFrame fra) {
        System.out.format("Function %s:\n",
                IdentifiedHelper.repr(fra.getFunc()));
        for (Map.Entry<Value, ValueBox> e : fra.getValueDict().entrySet()) {
            System.out.format("    %s = %s\n",
                    IdentifiedHelper.repr(e.getKey()), showBox(e.getValue()));
        }
    }

    private static String showBox(ValueBox vb) {
        if (vb instanceof IntBox) {
            IntBox b = (IntBox) vb;
            return String.format("IntBox(%s)", b.getValue().toString());
        } else if (vb instanceof FloatBox) {
            FloatBox b = (FloatBox) vb;
            return String.format("FloatBox(%f)", b.getValue());
        } else if (vb instanceof DoubleBox) {
            DoubleBox b = (DoubleBox) vb;
            return String.format("DoubleBox(%f)", b.getValue());
        } else if (vb instanceof RefBox) {
            RefBox b = (RefBox) vb;
            return String.format("RefBox(%d)", b.getAddr());
        } else if (vb instanceof IRefBox) {
            IRefBox b = (IRefBox) vb;
            return String.format("IRefBox(%d, %d)", b.getBase(), b.getOffset());
        } else if (vb instanceof StructBox) {
            StructBox b = (StructBox) vb;
            StringBuilder sb = new StringBuilder("{ ");
            for (int i = 0; i < b.size(); i++) {
                sb.append(showBox(b.getBox(i))).append(" ");
            }
            sb.append("}");
            return sb.toString();
        } else if (vb instanceof VoidBox) {
            return String.format("VoidBox()");
        } else if (vb instanceof FuncBox) {
            FuncBox b = (FuncBox) vb;
            return String.format("FuncBox(%s)",
                    IdentifiedHelper.repr(b.getFunc()));
        } else if (vb instanceof ThreadBox) {
            ThreadBox b = (ThreadBox) vb;
            return String.format("ThreadBox(%f)", b.getThread().getID());
        } else if (vb instanceof StackBox) {
            StackBox b = (StackBox) vb;
            return String.format("StackBox(%f)", b.getStack().getID());
        } else {
            return "Unknown box " + vb.getClass().getName();
        }
    }
}
