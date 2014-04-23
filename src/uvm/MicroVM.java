package uvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uvm.type.Type;

public class MicroVM {
    public static final int POINTER_SIZE = 64;
    
    public static final MicroVM v = new MicroVM();
    
    private MicroVM() {}
    
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
