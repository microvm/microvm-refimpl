package uvm.ir.binary.input;

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
import uvm.type.WeakRef;

public class TypeResolver extends AbstractResolver implements TypeVisitor<Void> {

    private ToResolve<Type> tr;

    public TypeResolver(IRBinaryReader br, ToResolve<Type> tr) {
        super(br);
        this.tr = tr;
    }

    @Override
    public Void visitInt(Int type) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitFloat(Float type) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitDouble(Double type) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitRef(Ref type) {
        type.setReferenced(type(tr.ids[0]));
        return null;
    }


    @Override
    public Void visitIRef(IRef type) {
        type.setReferenced(type(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitWeakRef(WeakRef type) {
        type.setReferenced(type(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitStruct(Struct type) {
        for(int id : tr.ids) {
            type.getFieldTypes().add(type(id));
        }
        return null;
    }

    @Override
    public Void visitArray(Array type) {
        type.setElemType(type(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitHybrid(Hybrid type) {
        type.setFixedPart(type(tr.ids[0]));
        type.setVarPart(type(tr.ids[1]));
        return null;
    }

    @Override
    public Void visitVoid(uvm.type.Void type) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitFunc(Func type) {
        type.setSig(sig(tr.ids[0]));
        return null;
    }

    @Override
    public Void visitThread(Thread type) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitStack(Stack type) {
        // No need to resolve.
        return null;
    }

    @Override
    public Void visitTagRef64(TagRef64 type) {
        // No need to resolve.
        return null;
    }

}
