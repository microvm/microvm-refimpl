package uvm.refimpl.mem;

import static uvm.refimpl.mem.MemConstants.WORD_SIZE_BYTES;
import static uvm.refimpl.mem.TypeSizes.sizeOf;
import static uvm.refimpl.mem.TypeSizes.alignOf;
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
 * Return the alignment (in bytes) of each type.
 * <p>
 * Atomic types align to their own sizes except IRef which is only single-word
 * aligned. Structs have the largest alignment of its fields. Arrays have the
 * same alignment as their elements. The void type and empty structs are 1-byte
 * aligned as it can be at any byte.
 */
public class AlignOf implements TypeVisitor<Integer> {

    @Override
    public Integer visitInt(Int type) {
        return sizeOf(type);
    }

    @Override
    public Integer visitFloat(Float type) {
        return sizeOf(type);
    }

    @Override
    public Integer visitDouble(Double type) {
        return sizeOf(type);
    }

    @Override
    public Integer visitRef(Ref type) {
        return sizeOf(type);
    }

    @Override
    public Integer visitIRef(IRef type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Integer visitWeakRef(WeakRef type) {
        return sizeOf(type);
    }

    @Override
    public Integer visitStruct(Struct type) {
        int maxAlign = 1;
        for (Type ty : type.getFieldTypes()) {
            int align = alignOf(ty);
            if (align > maxAlign) {
                maxAlign = align;
            }
        }
        return maxAlign;
    }

    @Override
    public Integer visitArray(Array type) {
        return alignOf(type.getElemType());
    }

    @Override
    public Integer visitHybrid(Hybrid type) {
        return -1;
    }

    @Override
    public Integer visitVoid(Void type) {
        return 1;
    }

    @Override
    public Integer visitFunc(Func type) {
        return sizeOf(type);
    }

    @Override
    public Integer visitThread(Thread type) {
        return sizeOf(type);
    }

    @Override
    public Integer visitStack(Stack type) {
        return sizeOf(type);
    }

    @Override
    public Integer visitTagRef64(TagRef64 type) {
        return sizeOf(type);
    }

}
