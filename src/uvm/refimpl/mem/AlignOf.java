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
public class AlignOf implements TypeVisitor<Long> {

    @Override
    public Long visitInt(Int type) {
        return sizeOf(type);
    }

    @Override
    public Long visitFloat(Float type) {
        return sizeOf(type);
    }

    @Override
    public Long visitDouble(Double type) {
        return sizeOf(type);
    }

    @Override
    public Long visitRef(Ref type) {
        return sizeOf(type);
    }

    @Override
    public Long visitIRef(IRef type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Long visitWeakRef(WeakRef type) {
        return sizeOf(type);
    }

    @Override
    public Long visitStruct(Struct type) {
        long maxAlign = 1;
        for (Type ty : type.getFieldTypes()) {
            long align = alignOf(ty);
            if (align > maxAlign) {
                maxAlign = align;
            }
        }
        return maxAlign;
    }

    @Override
    public Long visitArray(Array type) {
        return alignOf(type.getElemType());
    }

    @Override
    public Long visitHybrid(Hybrid type) {
        return -1L;
    }

    @Override
    public Long visitVoid(Void type) {
        return 1L;
    }

    @Override
    public Long visitFunc(Func type) {
        return sizeOf(type);
    }

    @Override
    public Long visitThread(Thread type) {
        return sizeOf(type);
    }

    @Override
    public Long visitStack(Stack type) {
        return sizeOf(type);
    }

    @Override
    public Long visitTagRef64(TagRef64 type) {
        return sizeOf(type);
    }

}
