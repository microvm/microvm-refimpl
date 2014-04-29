package uvm.ssavalue;

import java.util.ArrayList;
import java.util.List;

import uvm.OpCode;
import uvm.type.Struct;

public class StructConstant extends Constant {
    private Struct type;
    private List<Constant> values = new ArrayList<Constant>();

    public StructConstant() {
    }

    public StructConstant(Struct type, List<Constant> values) {
        this.type = type;
        this.values.addAll(values);
    }

    @Override
    public Struct getType() {
        return this.type;
    }

    public void setType(Struct type) {
        this.type = type;
    }

    public List<Constant> getValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(": ").append(type).append(" = { ");
        for (Constant value : values) {
            sb.append(value);
            sb.append(" ");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int opcode() {
        return OpCode.STRUCT_IMM;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitStructConstant(this);
    }
}
