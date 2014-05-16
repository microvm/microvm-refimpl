package uvm.ir.text.output;

import static uvm.ir.text.output.WritingHelper.g;
import uvm.type.Array;
import uvm.type.Double;
import uvm.type.Float;
import uvm.type.Func;
import uvm.type.Hybrid;
import uvm.type.IRef;
import uvm.type.Int;
import uvm.type.Ref;
import uvm.type.Stack;
import uvm.type.Struct;
import uvm.type.TagRef64;
import uvm.type.Thread;
import uvm.type.Type;
import uvm.type.TypeVisitor;
import uvm.type.Void;
import uvm.type.WeakRef;

/**
 * Writes a type. For IRTextWriter use only.
 */
public class TypeWriter implements TypeVisitor<String> {

    @SuppressWarnings("unused")
    private IRTextWriter irTextWriter;

    public TypeWriter(IRTextWriter irTextWriter) {
        this.irTextWriter = irTextWriter;
    }

    @Override
    public String visitInt(Int type) {
        return String.format("int<%d>", type.getSize());
    }

    @Override
    public String visitFloat(Float type) {
        return "float";
    }

    @Override
    public String visitDouble(Double type) {
        return "double";
    }

    @Override
    public String visitRef(Ref type) {
        return String.format("ref<%s>", g(type.getReferenced()));
    }

    @Override
    public String visitIRef(IRef type) {
        return String.format("iref<%s>", g(type.getReferenced()));
    }

    @Override
    public String visitWeakRef(WeakRef type) {
        return String.format("weakref<%s>", g(type.getReferenced()));
    }

    @Override
    public String visitStruct(Struct type) {
        StringBuilder sb = new StringBuilder();
        sb.append("struct < ");
        for (Type fieldType : type.getFieldTypes()) {
            sb.append(g(fieldType)).append(" ");
        }
        sb.append(">");
        return sb.toString();

    }

    @Override
    public String visitArray(Array type) {
        return String.format("array<%s %d>", g(type.getElemType()),
                type.getLength());
    }

    @Override
    public String visitHybrid(Hybrid type) {
        return String.format("hybrid<%s %s>", g(type.getFixedPart()),
                g(type.getVarPart()));
    }

    @Override
    public String visitVoid(Void type) {
        return "void";
    }

    @Override
    public String visitFunc(Func type) {
        return String.format("func<%s>", g(type.getSig()));
    }

    @Override
    public String visitThread(Thread type) {
        return "thread";
    }

    @Override
    public String visitStack(Stack type) {
        return "stack";
    }

    @Override
    public String visitTagRef64(TagRef64 type) {
        return "tagref64";
    }

}
