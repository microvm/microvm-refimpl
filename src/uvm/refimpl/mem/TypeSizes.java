package uvm.refimpl.mem;

import uvm.type.Array;
import uvm.type.Hybrid;
import uvm.type.Struct;
import uvm.type.Type;

/**
 * Responsible for object layout.
 * <p>
 * Scalar object:
 * <pre>
 *   1bit     1bit    30bits   32bits  
 * +-------+--------+--------+---------+--------------
 * |            header (8bytes)        | payload... 
 * | moved | marked | unused | type ID |
 * +-------+--------+--------+---------+--------------
 * ^objref-8bytes                      ^ objref
 * </pre>
 * Hybrid:
 * <pre>
 *    8 bytes           8 bytes
 * +-----------------+-----------------+------------+--------------
 * | var part length |  header         | fixed part | var part
 * |                 |  same as scalar |            |
 * +-----------------+-----------------+------------+--------------
 * ^objref-16bytes  ^objref-8bytes    ^ objref
 * </pre>
 */
public class TypeSizes {

    public static final long GC_HEADER_SIZE_SCALAR = 8;
    public static final long GC_HEADER_SIZE_HYBRID = 16;

    public static final long GC_HEADER_OFFSET_TAG = -8;
    public static final long GC_HEADER_OFFSET_HYBRID_LENGTH = -16;

    private static final SizeOf SIZE_OF = new SizeOf();
    private static final AlignOf ALIGN_OF = new AlignOf();

    public static long sizeOf(Type ty) {
        return ty.accept(SIZE_OF);
    }

    public static long alignOf(Type ty) {
        return ty.accept(ALIGN_OF);
    }

    public static long hybridSizeOf(Hybrid type, long len) {
        long fixedSize = sizeOf(type.getFixedPart());
        long varAlign = alignOf(type.getVarPart());
        long varSize = shiftOffsetOf(type.getVarPart(), len);
        long size = alignUp(fixedSize, varAlign) + varSize;
        return size;
    }

    public static long hybridAlignOf(Hybrid type, long len) {
        long fixedAlign = alignOf(type.getFixedPart());
        long varAlign = alignOf(type.getVarPart());
        long align = Math.max(fixedAlign, varAlign);
        return align;
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

    public static long intBitsToBytes(long n) {
        long p2 = nextPowOfTwo(n);
        if (p2 < 8) {
            return 1;
        } else {
            return p2 / 8;
        }
    }

    public static long alignUp(long n, long alignment) {
        return ((n - 1) & ~(alignment - 1)) + alignment;
    }

    public static long alignDown(long n, long alignment) {
        return n & ~(alignment - 1);
    }

}
