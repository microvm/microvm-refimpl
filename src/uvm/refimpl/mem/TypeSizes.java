package uvm.refimpl.mem;

import uvm.type.Array;
import uvm.type.Struct;
import uvm.type.Type;

public class TypeSizes {

    private static SizeOf SIZE_OF = new SizeOf();
    private static AlignOf ALIGN_OF = new AlignOf();

    public static long sizeOf(Type ty) {
        return ty.accept(SIZE_OF);
    }

    public static long alignOf(Type ty) {
        return ty.accept(ALIGN_OF);
    }

    public static long fieldOffsetOf(Struct structType, int index) {
        Type fieldType = structType.getFieldTypes().get(index);
        long fieldAlign = alignOf(fieldType);
        long prefixSize = structPrefixSizeOf(structType, index);
        long offset = alignUp(prefixSize, fieldAlign);
        return offset;
    }

    public static long structPrefixSizeOf(Struct type, int prefixLen) {
        long sz = 0;
        for (int i = 0; i < prefixLen; i++) {
            Type ty = type.getFieldTypes().get(i);
            sz = alignUp(sz, alignOf(ty)) + sizeOf(ty);
        }
        return sz;
    }

    public static long arrayPrefixSizeOf(Array type, long length) {
        return shiftOffsetOf(type.getElemType(), length);
    }

    public static long shiftOffsetOf(Type elemType, long index) {
        return alignUp(sizeOf(elemType), alignOf(elemType)) * index;
    }

    public static long nextPowOfTwo(long n) {
        long i = 1;
        while (i < n) {
            i <<= 1;
        }
        return i;
    }

    public static long alignUp(long n, long alignment) {
        return ((n - 1) & ~(alignment - 1)) + alignment;
    }

    public static long alignDown(long n, long alignment) {
        return n & ~(alignment - 1);
    }

}
