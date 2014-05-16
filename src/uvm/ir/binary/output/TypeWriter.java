package uvm.ir.binary.output;

import static uvm.ir.binary.output.TopLevelOpCodes.ARRAY;
import static uvm.ir.binary.output.TopLevelOpCodes.DOUBLE;
import static uvm.ir.binary.output.TopLevelOpCodes.FLOAT;
import static uvm.ir.binary.output.TopLevelOpCodes.FUNC;
import static uvm.ir.binary.output.TopLevelOpCodes.HYBRID;
import static uvm.ir.binary.output.TopLevelOpCodes.INT;
import static uvm.ir.binary.output.TopLevelOpCodes.IREF;
import static uvm.ir.binary.output.TopLevelOpCodes.REF;
import static uvm.ir.binary.output.TopLevelOpCodes.STACK;
import static uvm.ir.binary.output.TopLevelOpCodes.STRUCT;
import static uvm.ir.binary.output.TopLevelOpCodes.TAGREF64;
import static uvm.ir.binary.output.TopLevelOpCodes.THREAD;
import static uvm.ir.binary.output.TopLevelOpCodes.VOID;
import static uvm.ir.binary.output.TopLevelOpCodes.WEAKREF;
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

/**
 * Write type constructors in the binary form.
 */
public class TypeWriter implements TypeVisitor<Void> {

    @SuppressWarnings("unused")
    private IRBinaryWriter irBinaryWriter;
    private BinaryOutputStream bos;

    public TypeWriter(IRBinaryWriter irBinaryWriter) {
        this.irBinaryWriter = irBinaryWriter;
        this.bos = irBinaryWriter.bos;
    }
    
    @Override
    public Void visitInt(Int type) {
        bos.writeOpc(INT);
        bos.writeByte((byte) type.getSize());
        return null;
    }

    @Override
    public Void visitFloat(Float type) {
        bos.writeOpc(FLOAT);
        return null;
    }

    @Override
    public Void visitDouble(Double type) {
        bos.writeOpc(DOUBLE);
        return null;
    }

    @Override
    public Void visitRef(Ref type) {
        bos.writeOpc(REF);
        bos.writeID(type.getReferenced());
        return null;
    }

    @Override
    public Void visitIRef(IRef type) {
        bos.writeOpc(IREF);
        bos.writeID(type.getReferenced());
        return null;
    }

    @Override
    public Void visitWeakRef(WeakRef type) {
        bos.writeOpc(WEAKREF);
        bos.writeID(type.getReferenced());
        return null;
    }

    @Override
    public Void visitStruct(Struct type) {
        bos.writeOpc(STRUCT);
        bos.writeLen(type.getFieldTypes());
        for(Type ft : type.getFieldTypes()) {
            bos.writeID(ft);
        }
        return null;
    }

    @Override
    public Void visitArray(Array type) {
        bos.writeOpc(ARRAY);
        bos.writeID(type.getElemType());
        bos.writeArySz(type.getLength());
        return null;
    }

    @Override
    public Void visitHybrid(Hybrid type) {
        bos.writeOpc(HYBRID);
        bos.writeID(type.getFixedPart());
        bos.writeID(type.getVarPart());
        return null;
    }

    @Override
    public Void visitVoid(uvm.type.Void type) {
        bos.writeOpc(VOID);
        return null;
    }

    @Override
    public Void visitFunc(Func type) {
        bos.writeOpc(FUNC);
        bos.writeID(type.getSig());
        return null;
    }

    @Override
    public Void visitThread(Thread type) {
        bos.writeOpc(THREAD);
        return null;
    }

    @Override
    public Void visitStack(Stack type) {
        bos.writeOpc(STACK);
        return null;
    }

    @Override
    public Void visitTagRef64(TagRef64 type) {
        bos.writeOpc(TAGREF64);
        return null;
    }

}
