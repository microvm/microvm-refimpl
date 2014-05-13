package uvm;

import uvm.ssavalue.Constant;
import uvm.type.Type;

/**
 * A "bundle" is analog to JVM's class file and one Bundle corresponds to one
 * uir file.
 */
public class Bundle {
    /**
     * All types.
     */
    private Namespace<Type> typeNs = new SimpleNamespace<Type>();
    
    /**
     * All function signatures.
     */
    private Namespace<FunctionSignature> funcSigNs = new SimpleNamespace<FunctionSignature>();

    /**
     * All global SSA Values. This includes all declared constants, iref
     * constants for global data and func constants for functions.
     */
    private Namespace<Constant> globalValueNs = new SimpleNamespace<Constant>();

    /**
     * All constants declared by ".const" only.
     */
    private Namespace<Constant> declaredConstNs = new SimpleNamespace<Constant>();
    
    /**
     * All global data.
     */
    private Namespace<GlobalData> globalDataNs = new SimpleNamespace<GlobalData>();
    
    /**
     * All functions, declared or defined.
     */
    private Namespace<Function> funcNs = new SimpleNamespace<Function>();

    public Namespace<Type> getTypeNs() {
        return typeNs;
    }

    public Namespace<FunctionSignature> getFuncSigNs() {
        return funcSigNs;
    }

    public Namespace<Constant> getGlobalValueNs() {
        return globalValueNs;
    }

    public Namespace<Constant> getDeclaredConstNs() {
        return declaredConstNs;
    }

    public Namespace<GlobalData> getGlobalDataNs() {
        return globalDataNs;
    }

    public Namespace<Function> getFuncNs() {
        return funcNs;
    }

}
