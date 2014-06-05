package uvm.refimpl.mem;

import uvm.type.Type;

public class TypeSizes {

    private static SizeOf SIZE_OF = new SizeOf();
    private static AlignOf ALIGN_OF = new AlignOf();

    public static int sizeOf(Type ty) {
        return ty.accept(SIZE_OF);
    }

    public static int alignOf(Type ty) {
        return ty.accept(ALIGN_OF);
    }

    public static int nextPowOfTwo(int n) {
        int i = 1;
        while (i < n) {
            i <<= 1;
        }
        return i;
    }

    public static int alignUp(int n, int alignment) {
        return ((n - 1) & ~(alignment - 1)) + alignment;
    }

    public static int alignDown(int n, int alignment) {
        return n & ~(alignment - 1);
    }

    public static long alignUp(long n, long alignment) {
        return ((n - 1) & ~(alignment - 1)) + alignment;
    }

    public static long alignDown(long n, long alignment) {
        return n & ~(alignment - 1);
    }
}
