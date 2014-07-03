package uvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uvm.type.Int;
import uvm.type.Type;

/**
 * @deprecated Much of the information (word size, pointer size, constant pool,
 *             functions, etc.) is either implementation-dependent or bound to a
 *             running VM. The "uvm" package only contains models for the Micro
 *             VM IR, but not running VMs.
 * 
 * @see uvm.refimpl.facade.MicroVM
 */
@Deprecated
public class MicroVM {
    public static final int POINTER_SIZE = 64;

    public static final int WORD_SIZE_BITS = 64;

    public static final Int WORD_TYPE = new Int(WORD_SIZE_BITS);

    public static final MicroVM v = new MicroVM();

    private MicroVM() {
    }

    public HashMap<String, Type> types = new HashMap<String, Type>();

    public void declareType(String name, Type t) {
        types.put(name, t);
        System.out.println("declared type: " + t);
    }

    public HashMap<String, Function> funcs = new HashMap<String, Function>();

    public void declareFunc(String name, Function f) {
        funcs.put(name, f);
        System.out.println("declared func: " + f);
    }

    public List<CompiledFunction> compiledFuncs = new ArrayList<CompiledFunction>();

    public void compiledFunc(CompiledFunction cf) {
        compiledFuncs.add(cf);
    }
}
