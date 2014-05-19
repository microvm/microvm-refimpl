package uvm;

/**
 * These opcodes are for type/constant constructors and other flags in the
 * binary µVM IR, not instructions.
 */
public class TopLevelOpCodes {
    // Top-level definitions.
    public static final int TYPEDEF = 0x01;
    public static final int FUNCSIG = 0x02;
    public static final int CONST = 0x03;
    public static final int GLOBAL = 0x04;
    public static final int FUNCDECL = 0x05;
    public static final int FUNCDEF = 0x06;
    public static final int NAMEBIND = 0x07;

    // Type constructors.
    public static final int INT = 0x01;
    public static final int FLOAT = 0x02;
    public static final int DOUBLE = 0x03;
    public static final int REF = 0x04;
    public static final int IREF = 0x05;
    public static final int WEAKREF = 0x06;
    public static final int STRUCT = 0x07;
    public static final int ARRAY = 0x08;
    public static final int HYBRID = 0x09;
    public static final int VOID = 0x0A;
    public static final int FUNC = 0x0B;
    public static final int THREAD = 0x0C;
    public static final int STACK = 0x0D;
    public static final int TAGREF64 = 0x0E;

    // Constant constructors.
    public static final int INTCC = 0x01;
    public static final int FLOATCC = 0x02;
    public static final int DOUBLECC = 0x03;
    public static final int STRUCTCC = 0x04;
    public static final int NULLCC = 0x05;

}
