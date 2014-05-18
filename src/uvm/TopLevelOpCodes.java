package uvm;

/**
 * These opcodes are for type/constant constructors and other flags in the
 * binary µVM IR, not instructions.
 */
public class TopLevelOpCodes {
    // Top-level definitions.
    public static int TYPEDEF = 0x01;
    public static int FUNCSIG = 0x02;
    public static int CONST = 0x03;
    public static int GLOBAL = 0x04;
    public static int FUNCDECL = 0x05;
    public static int FUNCDEF = 0x06;
    public static int NAMEBIND = 0x07;

    // Type constructors.
    public static int INT = 0x01;
    public static int FLOAT = 0x02;
    public static int DOUBLE = 0x03;
    public static int REF = 0x04;
    public static int IREF = 0x05;
    public static int WEAKREF = 0x06;
    public static int STRUCT = 0x07;
    public static int ARRAY = 0x08;
    public static int HYBRID = 0x09;
    public static int VOID = 0x0A;
    public static int FUNC = 0x0B;
    public static int THREAD = 0x0C;
    public static int STACK = 0x0D;
    public static int TAGREF64 = 0x0E;

    // Constant constructors.
    public static int INTCC = 0x01;
    public static int FLOATCC = 0x02;
    public static int DOUBLECC = 0x03;
    public static int STRUCTCC = 0x04;
    public static int NULLCC = 0x05;

}
