package uvm.ssavalue;

import uvm.Function;
import uvm.OpCode;
import uvm.type.Func;
import uvm.type.Type;

/**
 * A constant of func type. Associated with a declared or defined function.
 */
public class FunctionConstant extends Constant {
    /**
     * The associated function.
     */
    private Function function;

    /**
     * The type of this constant. It is a func of the signature.
     */
    private Type type;

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
        type = new Func(function.getSig());
    }

    @Override
    public int opcode() {
        return OpCode.FUNCID_IMM;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitFunctionConstant(this);
    }

}
