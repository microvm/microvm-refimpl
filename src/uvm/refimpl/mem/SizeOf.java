package uvm.refimpl.mem;

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
import uvm.type.TypeVisitor;
import uvm.type.Void;
import uvm.type.WeakRef;

/**
 * Return the length (in bytes) of an object of each type.
 * <p>
 * Atomic types have their own sizes. For aggregate types, structs have all of
 * its fields aligned for their respective alignment requirements, and arrays
 * have many elements all of which are aligned.
 */
public class SizeOf implements TypeVisitor<Long> {

    @Override
    public Long visitInt(Int type) {
        return TypeSizes.intBitsToBytes(type.getSize());
    }

    @Override
    public Long visitFloat(Float type) {
        return 4L;
    }

    @Override
    public Long visitDouble(Double type) {
        return 8L;
    }

    @Override
    public Long visitRef(Ref type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Long visitIRef(IRef type) {
        return 2 * WORD_SIZE_BYTES;
    }

    @Override
    public Long visitWeakRef(WeakRef type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Long visitStruct(Struct type) {
        return TypeSizes.structPrefixSizeOf(type, type.getFieldTypes().size());
    }

    @Override
    public Long visitArray(Array type) {
        return TypeSizes.arrayPrefixSizeOf(type, type.getLength());
    }

    @Override
    public Long visitHybrid(Hybrid type) {
        return -1L;
    }

    @Override
    public Long visitVoid(Void type) {
        return 0L;
    }

    @Override
    public Long visitFunc(Func type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Long visitThread(Thread type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Long visitStack(Stack type) {
        return WORD_SIZE_BYTES;
    }

    @Override
    public Long visitTagRef64(TagRef64 type) {
        return 64L;
    }

}
