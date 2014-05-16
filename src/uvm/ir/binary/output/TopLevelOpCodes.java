package uvm.ir.binary.output;

public class TopLevelOpCodes {
    // Top-level definitions.
    public static byte TYPEDEF = 0x01;
    public static byte FUNCSIG = 0x02;
    public static byte CONST = 0x03;
    public static byte GLOBAL = 0x04;
    public static byte FUNCDECL = 0x05;
    public static byte FUNCDEF = 0x06;
    public static byte NAMEBIND = 0x07;

    // Type constructors.
    public static byte INT = 0x01;
    public static byte FLOAT = 0x02;
    public static byte DOUBLE = 0x03;
    public static byte REF = 0x04;
    public static byte IREF = 0x05;
    public static byte WEAKREF = 0x06;
    public static byte STRUCT = 0x07;
    public static byte ARRAY = 0x08;
    public static byte HYBRID = 0x09;
    public static byte VOID = 0x0A;
    public static byte FUNC = 0x0B;
    public static byte THREAD = 0x0C;
    public static byte STACK = 0x0D;
    public static byte TAGREF64 = 0x0E;
    
    // Constant constructors.
    public static byte INTCC = 0x01;
    public static byte FLOATCC = 0x02;
    public static byte DOUBLECC = 0x03;
    public static byte STRUCTCC = 0x04;
    public static byte NULLCC = 0x05;

}
