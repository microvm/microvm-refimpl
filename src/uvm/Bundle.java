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
    private Map<Integer, String> idToName = new HashMap<Integer, String>();
    /**
     * A mapping between textual names and numerical IDs. Not all objects have
     * names.
     */
    private Map<String, Integer> nameToId = new HashMap<String, Integer>();

    // Dictionary accessors

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

    public Map<Integer, String> getIdToName() {
        return idToName;
    }

    public Map<String, Integer> getNameToId() {
        return nameToId;
    }

    // Convenient methods for adding objects.

    private void bind(int id, String name) {
        if (name != null) {
            idToName.put(id, name);
            nameToId.put(name, id);
        }
    }

    public void registerConstant(int id, String name, Constant constant) {
        constants.put(id, constant);
        bind(id, name);
    }

    public void registerType(int id, String name, Type type) {
        types.put(id, type);
        bind(id, name);
    }

    public void registerFuncSig(int id, String name, FunctionSignature funcSig) {
        funcSigs.put(id, funcSig);
        bind(id, name);
    }

    public void registerFunc(int id, String name, Function func) {
        funcs.put(id, func);
        bind(id, name);
    }

    // Convenient methods for getting.

    public Constant getConstantByName(String name) {
        return constants.get(nameToId.get(name));
    }

    public Type getTypeByName(String name) {
        return types.get(nameToId.get(name));
    }

    public FunctionSignature getFuncSigByName(String name) {
        return funcSigs.get(nameToId.get(name));
    }

    public Function getFuncByName(String name) {
        return funcs.get(nameToId.get(name));
    }

}
