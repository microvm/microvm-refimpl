package uvm.refimpl.mem;

import static uvm.refimpl.mem.MemConstants.WORD_SIZE_BYTES;
import static uvm.refimpl.mem.TypeSizes.sizeOf;
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
import uvm.type.TypeVisitor;
import uvm.type.Void;
import uvm.type.WeakRef;

/**
 * Return the alignment (in bytes) of each type.
 */
public class AlignOf implements TypeVisitor<Integer> {

    public static int genericAlign(int n, int wordSize) {
        if (n < wordSize) {
            int i = 1;
            while (i < n) {
                i <<= 1;
            }
            return i;
        } else {
            return wordSize;
        }
    }

    @Override
    public Integer visitInt(Int type) {
        return genericAlign(sizeOf(type), WORD_SIZE_BYTES);
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
        return genericAlign(sizeOf(type), WORD_SIZE_BYTES);
    }

    @Override
    public Integer visitArray(Array type) {
        return genericAlign(sizeOf(type), WORD_SIZE_BYTES);
    }

    @Override
    public Integer visitHybrid(Hybrid type) {
        return -1;
    }

    @Override
    public Integer visitVoid(Void type) {
        return -1;
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
        return WORD_SIZE_BYTES;
    }

}
