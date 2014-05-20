package uvm.ir.binary.input;

import java.io.PrintStream;

import uvm.ssavalue.BinOptr;
import uvm.ssavalue.CmpOptr;
import uvm.ssavalue.ConvOptr;

/**
 * Disassemble µVM IR Binary code on the fly. This is a static-only class.
 * <p>
 * <b>This does not generate correct text form.</b> It is impossible to identify
 * global IDs and local IDs without the context.
 */
@Deprecated
public class LiveDisassembler {
    private final PrintStream out;

    public LiveDisassembler(PrintStream out) {
        super();
        this.out = out;
    }

    public void typeDef(int id) {
        out.printf(".typedef @%d = ", id);
    }

    public void intType(int len) {
        out.printf("int<%d>\n", len);
    }

    public void floatType() {
        out.println("float");
    }

    public void doubleType() {
        out.println("double");
    }

    public void refType(int ty) {
        out.printf("ref<@%d>\n", ty);
    }

    public void irefType(int ty) {
        out.printf("iref<@%d>\n", ty);
    }

    public void weakrefType(int ty) {
        out.printf("weakref<@%d>\n", ty);
    }

    public void structfType(int... tys) {
        StringBuilder sb = new StringBuilder("struct< ");
        for (int ty : tys) {
            sb.append("@").append(ty).append(" ");
        }
        sb.append(">");
        out.println(sb.toString());
    }

    public void arrayType(int ty, long len) {
        out.printf("array<@%d %d>\n", ty, len);
    }

    public void hybridType(int ft, int vt) {
        out.printf("hybrid<@%d @%d>\n", ft, vt);
    }

    public void voidType() {
        out.println("void");
    }

    public void funcType(int sig) {
        out.printf("func<@%d>\n", sig);
    }

    public void threadType() {
        out.println("thread");
    }

    public void stackType() {
        out.println("stack");
    }

    public void tagref64Type() {
        out.println("tagref64");
    }

    public void funcSigDef(int rt, int... pts) {
        StringBuilder sb = new StringBuilder(rt).append(" ( ");
        for (int pt : pts) {
            sb.append("@").append(pt).append(" ");
        }
        sb.append(")");
        out.println(sb.toString());
    }

    public void constDef(int id, int ty) {
        out.printf(".const @%d <@%d> = ", id, ty);
    }

    public void intConst(long value) {
        out.println(value);
    }

    public void floatConst(float value) {
        out.printf("bits(0x%x) // %f\n", Float.floatToRawIntBits(value), value);
    }

    public void doubleConst(double value) {
        out.printf("bits(0x%x) // %d\n", Double.doubleToRawLongBits(value),
                value);
    }

    public void structConst(int... ids) {
        StringBuilder sb = new StringBuilder("{ ");
        for (int id : ids) {
            sb.append(id).append(" ");
        }
        sb.append("}");
        out.println(sb.toString());
    }

    public void nullConst() {
        out.println("NULL");
    }

    public void global(int id, int ty) {
        out.printf(".global @%id <@%ty>\n", id, ty);
    }

    public void funcdecl(int id, int sig) {
        out.printf(".funcdecl @%id <@%sig>\n", id, sig);
    }

    public void funcdef(int id, int sig, int... params) {
        StringBuilder sb = new StringBuilder(".funcdef ");
        sb.append("@").append(id).append(" <@").append(sig).append("> ( ");
        for (int param : params) {
            sb.append("%").append(param).append(" ");
        }
        sb.append(") {");
        out.println(sb.toString());
    }

    public void basicBlock(int id) {
        System.out.printf("    %%%d:\n", id);
    }

    public void inst(int id) {
        System.out.printf("        %%%d = ", id);
    }

    public void binOp(int opc, int t, int op1, int op2) {
        System.out.printf("%s <@%d> %%%d %%%d\n", BinOptr.valueByOpcode(opc),
                t, op1, op2);
    }

    public void cmp(int opc, int t, int op1, int op2) {
        System.out.printf("%s <@%d> %%%d %%%d\n", CmpOptr.valueByOpcode(opc),
                t, op1, op2);
    }

    public void conversion(int opc, int t1, int t2, int opnd) {
        System.out.printf("%s <@%d @%d> %%%d\n", ConvOptr.valueByOpcode(opc),
                t1, t2, opnd);
    }
}
