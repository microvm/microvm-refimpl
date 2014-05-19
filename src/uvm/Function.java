package uvm;

/**
 * A function is a callable thing in µVM. It is identified by an numerical ID
 * and optionally a human-readable name. It has an unchangeable signature stored
 * in the sig field and a run-time redefineable body represented by the cfg
 * field.
 * <p>
 * .funcdecl creates a new Function object, but .funcdef also creates a CFG.
 */
public class Function implements IdentifiedSettable {

    /**
     * The unique function ID
     */
    private int id;

    /**
     * An optional function name
     */
    private String name;

    /**
     * The signature of this function
     */
    private FunctionSignature sig;

    /**
     * Its current control flow graph. It is null if the function is declared
     * but not defined. When the function is re-defined, this will be changed.
     */
    private CFG cfg;

    /**
     * Its current compiled function. It has non-null value only when it is
     * defined and a compiled version of the currently defined version of CFG is
     * compiled.
     */
    private CompiledFunction compiledFunc;

    public Function() {
    }

    public Function(int id, String name, FunctionSignature sig) {
        this.id = id;
        this.name = name;
        this.sig = sig;
        this.cfg = null;
        this.compiledFunc = null;
    }

    @Override
    public String toString() {
        return name + " = " + sig;
    }


    @Override
    public int getID() {
        return id;
    }

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public FunctionSignature getSig() {
        return sig;
    }

    public void setSig(FunctionSignature sig) {
        this.sig = sig;
    }

    public CFG getCFG() {
        return cfg;
    }

    public void setCFG(CFG cfg) {
        this.cfg = cfg;
    }

    public CompiledFunction getCompiledFunc() {
        return compiledFunc;
    }

    public void setCompiledFunc(CompiledFunction compiledFunc) {
        this.compiledFunc = compiledFunc;
    }

}
