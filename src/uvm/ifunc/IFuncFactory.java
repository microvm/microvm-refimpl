package uvm.ifunc;

import uvm.Namespace;
import uvm.SimpleNamespace;
import uvm.type.Type;

/**
 * This class lists all supported intrinsic functions. Instances can be
 * retrieved.
 */
public class IFuncFactory {

    /*
     * Not fully implemented. All intrinsic functions should be documented.
     */

    private static final uvm.type.Double DOUBLE = new uvm.type.Double();
    private static final uvm.type.Void VOID = new uvm.type.Void();
    private static final uvm.type.Thread THREAD = new uvm.type.Thread();
    private static final uvm.type.Stack STACK = new uvm.type.Stack();
    private static final uvm.type.TagRef64 TR64 = new uvm.type.TagRef64();
    private static final uvm.type.Int I1 = new uvm.type.Int(1);
    private static final uvm.type.Int I6 = new uvm.type.Int(6);
    private static final uvm.type.Int I52 = new uvm.type.Int(52);
    private static final uvm.type.Ref REFVOID = new uvm.type.Ref(VOID);

    private static Namespace<IFunc> ifuncs = new SimpleNamespace<IFunc>();

    private static void add(int id, String name, Type retType,
            Type... paramTypes) {
        IFunc ifunc = new GenericIFunc(id, name, retType, paramTypes);
        ifuncs.put(id, name, ifunc);
    }

    public static final int IFUNC__UVM__NEW_THREAD = 0x201;
    public static final int IFUNC__UVM__SWAP_STACK = 0x202;
    public static final int IFUNC__UVM__KILL_STACK = 0x203;
    public static final int IFUNC__UVM__SWAP_AND_KILL = 0x204;
    public static final int IFUNC__UVM__THREAD_EXIT = 0x205;
    public static final int IFUNC__UVM__CURRENT_STACK = 0x206;

    public static final int IFUNC__UVM__TR64__IS_FP = 0x211;
    public static final int IFUNC__UVM__TR64__IS_INT = 0x212;
    public static final int IFUNC__UVM__TR64__IS_REF = 0x213;
    public static final int IFUNC__UVM__TR64__FROM_FP = 0x214;
    public static final int IFUNC__UVM__TR64__FROM_INT = 0x215;
    public static final int IFUNC__UVM__TR64__FROM_REF = 0x216;
    public static final int IFUNC__UVM__TR64__TO_FP = 0x217;
    public static final int IFUNC__UVM__TR64__TO_INT = 0x218;
    public static final int IFUNC__UVM__TR64__TO_REF = 0x219;
    public static final int IFUNC__UVM__TR64__TO_TAG = 0x21a;

    static {
        add(IFUNC__UVM__NEW_THREAD, "@uvm.new_thread", THREAD, STACK);
        add(IFUNC__UVM__SWAP_STACK, "@uvm.swap_stack", VOID, STACK);
        add(IFUNC__UVM__KILL_STACK, "@uvm.kill_stack", VOID, STACK);
        add(IFUNC__UVM__SWAP_AND_KILL, "@uvm.swap_and_kill", VOID, STACK);
        add(IFUNC__UVM__THREAD_EXIT, "@uvm.thread_exit", VOID);
        add(IFUNC__UVM__CURRENT_STACK, "@uvm.current_stack", STACK);

        add(IFUNC__UVM__TR64__IS_FP, "@uvm.tr64.is_fp", I1, TR64);
        add(IFUNC__UVM__TR64__IS_INT, "@uvm.tr64.is_int", I1, TR64);
        add(IFUNC__UVM__TR64__IS_REF, "@uvm.tr64.is_ref", I1, TR64);
        add(IFUNC__UVM__TR64__FROM_FP, "@uvm.tr64.from_fp", TR64, DOUBLE);
        add(IFUNC__UVM__TR64__FROM_INT, "@uvm.tr64.from_int", TR64, I52);
        add(IFUNC__UVM__TR64__FROM_REF, "@uvm.tr64.from_ref", TR64, REFVOID, I6);
        add(IFUNC__UVM__TR64__TO_FP, "@uvm.tr64.to_fp", DOUBLE, TR64);
        add(IFUNC__UVM__TR64__TO_INT, "@uvm.tr64.to_int", I52, TR64);
        add(IFUNC__UVM__TR64__TO_REF, "@uvm.tr64.to_ref", REFVOID, TR64);
        add(IFUNC__UVM__TR64__TO_TAG, "@uvm.tr64.to_tag", I6, TR64);
    }

    /**
     * Get the instance of an intrinsic function by ID.
     * 
     * @param id
     *            The ID of the intrinsicFunction
     * @return The IntrinsicFunction instance for the ID
     */
    public static IFunc getIFuncByID(int id) {
        return ifuncs.getByID(id);
    }

    /**
     * Get the instance of an intrinsic function by name.
     * 
     * @param name
     *            The name of the intrinsic function.
     * @return The IntrinsicFunction instance for the name.
     */
    public static IFunc getIFuncByName(String name) {
        return ifuncs.getByName(name);
    }
}
