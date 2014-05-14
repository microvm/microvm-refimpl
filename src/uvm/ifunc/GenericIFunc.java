package uvm.ifunc;

import java.util.Arrays;

import uvm.FunctionSignature;
import uvm.type.Type;

/**
 * A generic implementation of an intrinsic function placeholder.
 * <p>
 * This is only a symbolic representation of an intrinsic function. How to
 * implement it in the functional level is done by the interpreter or the code
 * generator.
 */
public class GenericIFunc implements IFunc {

    /**
     * The signature of the intrinsic function.
     */
    private FunctionSignature sig;

    /**
     * The ID.
     */
    private int id;

    /**
     * The name, usually in the style: "@uvm.xxx.xxx".
     */
    private String name;

    public GenericIFunc(int id, String name, Type retType, Type... paramTypes) {
        this.id = id;
        this.name = name;
        this.sig = new FunctionSignature(retType, Arrays.asList(paramTypes));
    }

    @Override
    public Type getType() {
        return sig.getReturnType();
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}
