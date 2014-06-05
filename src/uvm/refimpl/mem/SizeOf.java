package uvm.refimpl.mem;

import static uvm.refimpl.mem.TypeSizes.alignOf;
import static uvm.refimpl.mem.TypeSizes.alignUp;
import static uvm.refimpl.mem.TypeSizes.sizeOf;
import static uvm.refimpl.mem.MemConstants.WORD_SIZE_BYTES;
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
 * Return the length (in bytes) of an object of each type.
 */
public class SizeOf implements TypeVisitor<Integer> {

    public static int genericSize(int n, int wordSize) {
        if (n < wordSize) {
            int i = 1;
            while (i < n) {
                i <<= 1;
            }
            return i;
        } else {
            return alignUp(n, wordSize);
        }
    }

    @Override
    public Integer visitInt(Int type) {
        return TypeSizes.nextPowOfTwo(type.getSize());
    }

    @Override
    public Integer visitFloat(Float type) {
        return 4;
    }

    @Override
    public Integer visitDouble(Double type) {
        return 8;
    }

    @Override
    public Integer visitRef(Ref type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Integer visitIRef(IRef type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Integer visitWeakRef(WeakRef type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Integer visitStruct(Struct type) {
        int sz = 0;
        for (Type ty : type.getFieldTypes()) {
            sz = alignUp(sz, alignOf(ty)) + sizeOf(ty);
        }
        return sz;
    }

    @Override
    public Integer visitArray(Array type) {
        return alignUp(sizeOf(type.getElemType()), alignOf(type.getElemType()))
                * type.getLength();
    }

    @Override
    public Integer visitHybrid(Hybrid type) {
        return -1;
    }

    @Override
    public Integer visitVoid(Void type) {
        return 0;
    }

    @Override
    public Integer visitFunc(Func type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Integer visitThread(Thread type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Integer visitStack(Stack type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Integer visitTagRef64(TagRef64 type) {
        return 64;
    }

}
