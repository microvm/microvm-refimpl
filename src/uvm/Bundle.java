package uvm;

import java.util.HashMap;
import java.util.Map;

import uvm.ssavalue.Constant;
import uvm.type.Type;

/**
 * A "bundle" is analog to JVM's class file and one Bundle corresponds to one
 * uir file.
 */
public class Bundle {
    /**
     * All global constants. Local constants are in their respective CFGs (CFG
     * is inside a defined Function).
     */
    private Map<Integer, Constant> constants = new HashMap<Integer, Constant>();
    /**
     * All function signatures in this bundle, explicitly or implicitly defined.
     */
    private Map<Integer, FunctionSignature> funcSigs = new HashMap<Integer, FunctionSignature>();
    /**
     * All functions in this bundle, declared or defined.
     */
    private Map<Integer, Function> funcs = new HashMap<Integer, Function>();
    /**
     * All types in this bundle, explicitly or implicitly defined.
     */
    private Map<Integer, Type> types = new HashMap<Integer, Type>();
    /**
     * A mapping between numerical IDs and textual names.
     */
    private Map<Integer, String> nameDict = new HashMap<Integer, String>();

    public Map<Integer, Constant> getConstants() {
        return constants;
    }

    public Map<Integer, FunctionSignature> getFuncSigs() {
        return funcSigs;
    }

    public Map<Integer, Function> getFuncs() {
        return funcs;
    }

    public Map<Integer, Type> getTypes() {
        return types;
    }

    public Map<Integer, String> getNameDict() {
        return nameDict;
    }

}
