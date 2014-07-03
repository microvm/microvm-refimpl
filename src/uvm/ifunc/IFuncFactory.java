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

    private static final uvm.type.Void VOID = new uvm.type.Void();
    private static final uvm.type.Thread THREAD = new uvm.type.Thread();
    private static final uvm.type.Stack STACK = new uvm.type.Stack();

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

    static {
        add(IFUNC__UVM__NEW_THREAD, "@uvm.new_thread", THREAD, STACK);
        add(IFUNC__UVM__SWAP_STACK, "@uvm.swap_stack", VOID, STACK);
        add(IFUNC__UVM__KILL_STACK, "@uvm.kill_stack", VOID, STACK);
        add(IFUNC__UVM__SWAP_AND_KILL, "@uvm.swap_and_kill", VOID, STACK);
        add(IFUNC__UVM__THREAD_EXIT, "@uvm.thread_exit", VOID);
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
