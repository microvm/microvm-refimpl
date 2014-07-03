package uvm;

import uvm.ssavalue.Constant;
import uvm.type.Type;
import uvm.util.ErrorUtils;

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

    private <T extends Identified> void simpleMerge(Namespace<T> nsDst,
            Namespace<T> nsSrc) {
        for (int id : nsSrc.getIDSet()) {
            T obj = nsSrc.getByID(id);
            if (nsDst.getByID(id) != null) {
                ErrorUtils.uvmError("Redefinition not allowed: "
                        + IdentifiedHelper.repr(obj));
                return;
            }
            String name = obj.getName();
            nsDst.put(id, name, obj);
        }
    }

    private void mergeFuncs(Namespace<Function> nsDst, Namespace<Function> nsSrc) {
        for (int id : nsSrc.getIDSet()) {
            Function oldFunc = nsDst.getByID(id);
            Function newFunc = nsSrc.getByID(id);
            if (oldFunc != null) {
                oldFunc.setCFG(newFunc.getCFG());
            } else {
                String name = newFunc.getName();
                nsDst.put(id, name, newFunc);
            }
        }
    }

    public void mergeFrom(Bundle bundle) {
        simpleMerge(typeNs, bundle.typeNs);
        simpleMerge(funcSigNs, bundle.funcSigNs);
        simpleMerge(globalValueNs, bundle.globalValueNs);
        simpleMerge(declaredConstNs, bundle.declaredConstNs);
        simpleMerge(globalDataNs, bundle.globalDataNs);
        mergeFuncs(funcNs, bundle.funcNs);
    }

}
