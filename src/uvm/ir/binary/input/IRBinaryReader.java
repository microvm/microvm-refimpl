package uvm.ir.binary.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uvm.Bundle;
import uvm.OpCode;
import uvm.TopLevelOpCodes;
import uvm.ir.io.NestedIOException;

/**
 * Read a bundle in the binary form.
 * <p>
 * This takes the DOM-like approach. An abstract syntax tree-like structure is
 * composed using "AbstractModel".
 */
public class IRBinaryReader {
    private static final String UTF8 = "UTF-8";

    private BinaryInputStream bis;

    private Bundle bundle = new Bundle();

    /**
     * Abstract models, for constructing bundle.
     */
    private List<AbstractModel> models = new ArrayList<AbstractModel>();

    // For name binding

    private Map<Integer, String> bindings = new HashMap<Integer, String>();

    // Public interfaces

    public IRBinaryReader(InputStream is) {
        bis = new BinaryInputStream(is);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void readBundle() {
        while (true) {
            int maybeOpCode = bis.maybeReadOpc();
            if (maybeOpCode == -1) {
                break;
            }

            readTopLevel(maybeOpCode);
        }
    }

    // Model constructor

    private void addModel(int opc, int id, Object... others) {
        AbstractModel model = new AbstractModel(opc, id, others);
        models.add(model);
    }

    // Hierarchical reader.

    private void readTopLevel(int opc) {
        switch (opc) {
        case TopLevelOpCodes.TYPEDEF:
            readTypeDef(opc);
            break;
        case TopLevelOpCodes.FUNCSIG:
            readFuncSig(opc);
            break;
        case TopLevelOpCodes.CONST:
            readConstDef(opc);
            break;
        case TopLevelOpCodes.GLOBAL:
            readGlobalData(opc);
            break;
        case TopLevelOpCodes.FUNCDECL:
            readFuncDecl(opc);
            break;
        case TopLevelOpCodes.FUNCDEF:
            readFuncDef(opc);
            break;
        case TopLevelOpCodes.NAMEBIND:
            readNameBind(opc);
            break;
        }
    }

    private int[] readIDList() {
        int len = bis.readLen();
        int[] ids = new int[len];
        for (int i = 0; i < len; i++) {
            int id = bis.readID();
            ids[i] = id;
        }
        return ids;
    }

    private void readTypeDef(int opc) {
        int id = bis.readID();

        int typeOpc = bis.readOpc();

        switch (typeOpc) {
        case TopLevelOpCodes.INT: {
            byte length = bis.readByte();
            addModel(opc, id, typeOpc, length);
            break;
        }
        case TopLevelOpCodes.FLOAT:
        case TopLevelOpCodes.DOUBLE:
        case TopLevelOpCodes.VOID:
        case TopLevelOpCodes.THREAD:
        case TopLevelOpCodes.STACK:
        case TopLevelOpCodes.TAGREF64: {
            addModel(opc, id, typeOpc);
            break;
        }
        case TopLevelOpCodes.REF:
        case TopLevelOpCodes.IREF:
        case TopLevelOpCodes.WEAKREF: {
            int referentType = bis.readID();
            addModel(opc, id, typeOpc, referentType);
            break;
        }
        case TopLevelOpCodes.STRUCT: {
            int[] fields = readIDList();
            addModel(opc, id, typeOpc, fields);
            break;
        }
        case TopLevelOpCodes.ARRAY: {
            int elemType = bis.readID();
            long len = bis.readArySz();
            addModel(opc, id, typeOpc, elemType, len);
            break;
        }
        case TopLevelOpCodes.HYBRID: {
            int fType = bis.readID();
            int vType = bis.readID();
            addModel(opc, id, typeOpc, fType, vType);
            break;
        }
        case TopLevelOpCodes.FUNC: {
            int sig = bis.readID();
            addModel(opc, id, typeOpc, sig);
            break;
        }
        }
    }

    private void readFuncSig(int opc) {
        int id = bis.readID();
        int retTy = bis.readID();
        int[] paramTys = readIDList();
        addModel(opc, id, retTy, paramTys);
    }

    private void readConstDef(int opc) {
        int id = bis.readID();
        int constOpc = bis.readOpc();

        switch (constOpc) {
        case TopLevelOpCodes.INTCC: {
            long num = bis.readLong();
            addModel(opc, id, constOpc, num);
            break;
        }
        case TopLevelOpCodes.FLOATCC: {
            float num = bis.readFloat();
            addModel(opc, id, constOpc, num);
            break;
        }
        case TopLevelOpCodes.DOUBLECC: {
            double num = bis.readDouble();
            addModel(opc, id, constOpc, num);
            break;
        }
        case TopLevelOpCodes.STRUCTCC: {
            int[] ids = readIDList();
            addModel(opc, id, constOpc, ids);
            break;
        }
        case TopLevelOpCodes.NULLCC: {
            addModel(opc, id, constOpc);
            break;
        }
        }
    }

    private void readGlobalData(int opc) {
        int id = bis.readID();
        int ty = bis.readID();
        addModel(opc, id, ty);
    }

    private void readFuncDecl(int opc) {
        int id = bis.readID();
        int sig = bis.readID();
        addModel(opc, id, sig);
    }

    private void readFuncDef(int opc) {
        int id = bis.readID();
        int sig = bis.readID();
        int[] params = readIDList();

        int nBBs = bis.readInt();
        AbstractModel[] bbModels = new AbstractModel[nBBs];

        for (int i = 0; i < nBBs; i++) {
            int bbID = bis.readID();
            int nInsts = bis.readInt();
            AbstractModel[] instModels = new AbstractModel[nInsts];
            for (int j = 0; j < nInsts; j++) {
                AbstractModel instModel = readInst();
                instModels[j] = instModel;
            }
            AbstractModel bbModel = new AbstractModel(-1, bbID, instModels);
            bbModels[i] = bbModel;
        }
        addModel(opc, id, sig, params, bbModels);
    }

    private AbstractModel readInst() {
        int id = bis.readID();
        int opc = bis.readOpc();
        switch (opc) {
        case OpCode.ADD:
        case OpCode.SUB:
        case OpCode.MUL:
        case OpCode.UDIV:
        case OpCode.SDIV:
        case OpCode.UREM:
        case OpCode.SREM:
        case OpCode.SHL:
        case OpCode.LSHR:
        case OpCode.ASHR:
        case OpCode.AND:
        case OpCode.OR:
        case OpCode.XOR:
        case OpCode.FADD:
        case OpCode.FSUB:
        case OpCode.FMUL:
        case OpCode.FDIV:
        case OpCode.FREM:
        case OpCode.EQ:
        case OpCode.NE:
        case OpCode.SGE:
        case OpCode.SGT:
        case OpCode.SLE:
        case OpCode.SLT:
        case OpCode.UGE:
        case OpCode.UGT:
        case OpCode.ULE:
        case OpCode.ULT:
        case OpCode.FFALSE:
        case OpCode.FTRUE:
        case OpCode.FUNO:
        case OpCode.FUEQ:
        case OpCode.FUNE:
        case OpCode.FUGT:
        case OpCode.FUGE:
        case OpCode.FULT:
        case OpCode.FULE:
        case OpCode.FORD:
        case OpCode.FOEQ:
        case OpCode.FONE:
        case OpCode.FOGT:
        case OpCode.FOGE:
        case OpCode.FOLT:
        case OpCode.FOLE:
        case OpCode.TRUNC:
        case OpCode.ZEXT:
        case OpCode.SEXT:
        case OpCode.FPTRUNC:
        case OpCode.FPEXT:
        case OpCode.FPTOUI:
        case OpCode.FPTOSI:
        case OpCode.UITOFP:
        case OpCode.SITOFP:
        case OpCode.BITCAST:
        case OpCode.REFCAST:
        case OpCode.IREFCAST:
        case OpCode.FUNCCAST:
        case OpCode.BRANCH2: {
            int id1 = bis.readID();
            int id2 = bis.readID();
            int id3 = bis.readID();
            return new AbstractModel(opc, id, id1, id2, id3);
        }
        case OpCode.SELECT: {
            int t = bis.readID();
            int cond = bis.readID();
            int iftrue = bis.readID();
            int iffalse = bis.readID();
            return new AbstractModel(opc, id, t, cond, iftrue, iffalse);
        }
        case OpCode.BRANCH: {
            int dest = bis.readID();
            return new AbstractModel(opc, id, dest);
        }
        case OpCode.SWITCH: {
            int t = bis.readID();
            int opnd = bis.readID();
            int def = bis.readID();
            int ncases = bis.readLen();
            int[] cases = new int[ncases];
            int[] dests = new int[ncases];
            for (int i = 0; i < ncases; i++) {
                int cas = bis.readID();
                int dst = bis.readID();
                cases[i] = cas;
                dests[i] = dst;
            }
            return new AbstractModel(opc, id, t, opnd, def, cases, dests);
        }
        case OpCode.PHI: {
            int t = bis.readID();
            int nbbs = bis.readLen();
            int[] bbs = new int[nbbs];
            int[] vals = new int[nbbs];
            for (int i = 0; i < nbbs; i++) {
                int bb = bis.readID();
                int val = bis.readID();
                bbs[i] = bb;
                vals[i] = val;
            }
            return new AbstractModel(opc, id, t, bbs, vals);
        }
        case OpCode.CALL: {
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            return new AbstractModel(opc, id, sig, func, args, kas);
        }
        case OpCode.INVOKE: {
            int sig = bis.readID();
            int func = bis.readID();
            int nor = bis.readID();
            int exc = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            return new AbstractModel(opc, id, sig, nor, exc, func, args, kas);
        }
        case OpCode.TAILCALL: {
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            return new AbstractModel(opc, id, sig, func, args);
        }
        case OpCode.RET: {
            int t = bis.readID();
            int rv = bis.readID();
            return new AbstractModel(opc, id, t, rv);
        }
        case OpCode.RETVOID: {
            return new AbstractModel(opc, id);
        }
        case OpCode.THROW: {
            int exc = bis.readID();
            return new AbstractModel(opc, id, exc);
        }
        case OpCode.LANDINGPAD: {
            return new AbstractModel(opc, id);
        }
        case OpCode.EXTRACTVALUE: {
            int t = bis.readID();
            int len = bis.readLen();
            int opnd = bis.readID();
            return new AbstractModel(opc, id, t, len, opnd);
        }
        case OpCode.INSERTVALUE: {
            int t = bis.readID();
            int len = bis.readLen();
            int opnd = bis.readID();
            int newVal = bis.readID();
            return new AbstractModel(opc, id, t, len, opnd, newVal);
        }
        case OpCode.NEW: {
            int t = bis.readID();
            return new AbstractModel(opc, id, t);
        }
        case OpCode.NEWHYBRID: {
            int t = bis.readID();
            int len = bis.readID();
            return new AbstractModel(opc, id, t, len);
        }
        case OpCode.ALLOCA: {
            int t = bis.readID();
            return new AbstractModel(opc, id, t);
        }
        case OpCode.ALLOCAHYBRID: {
            int t = bis.readID();
            int len = bis.readID();
            return new AbstractModel(opc, id, t, len);
        }
        case OpCode.GETIREF:
        case OpCode.GETFIXEDPARTIREF:
        case OpCode.GETVARPARTIREF: {
            int t = bis.readID();
            int opnd = bis.readID();
            return new AbstractModel(opc, id, t, opnd);
        }
        case OpCode.GETFIELDIREF: {
            int t = bis.readID();
            int index = bis.readLen();
            int opnd = bis.readID();
            return new AbstractModel(opc, id, t, index, opnd);
        }
        case OpCode.GETELEMIREF:
        case OpCode.SHIFTIREF: {
            int t = bis.readID();
            int opnd = bis.readID();
            int index = bis.readID();
            return new AbstractModel(opc, id, t, opnd, index);
        }
        case OpCode.LOAD: {
            int ord = bis.readOpc();
            int t = bis.readID();
            int loc = bis.readID();
            return new AbstractModel(opc, id, ord, t, loc);
        }
        case OpCode.STORE: {
            int ord = bis.readOpc();
            int t = bis.readID();
            int loc = bis.readID();
            int newVal = bis.readID();
            return new AbstractModel(opc, id, ord, t, loc, newVal);
        }
        case OpCode.CMPXCHG: {
            int ordSucc = bis.readOpc();
            int ordFail = bis.readOpc();
            int t = bis.readID();
            int loc = bis.readID();
            int expected = bis.readID();
            int desired = bis.readID();
            return new AbstractModel(opc, id, ordSucc, ordFail, t, loc,
                    expected, desired);
        }
        case OpCode.ATOMICRMW: {
            int ord = bis.readOpc();
            int optr = bis.readOpc();
            int t = bis.readID();
            int loc = bis.readID();
            int opnd = bis.readID();
            return new AbstractModel(opc, id, ord, optr, t, loc, opnd);
        }
        case OpCode.FENCE: {
            int ord = bis.readOpc();
            return new AbstractModel(opc, id, ord);
        }
        case OpCode.TRAP: {
            int t = bis.readID();
            int nor = bis.readID();
            int exc = bis.readID();
            int[] kas = readIDList();
            return new AbstractModel(opc, id, t, nor, exc, kas);
        }
        case OpCode.WATCHPOINT: {
            int wpid = bis.readID();
            int t = bis.readID();
            int dis = bis.readID();
            int nor = bis.readID();
            int exc = bis.readID();
            int[] kas = readIDList();
            return new AbstractModel(opc, id, wpid, t, dis, nor, exc, kas);
        }
        case OpCode.CCALL: {
            int callconv = bis.readOpc();
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            return new AbstractModel(opc, id, callconv, sig, func, args);
        }
        case OpCode.NEWSTACK: {
            int sig = bis.readID();
            int func = bis.readID();
            int[] args = readIDList();
            return new AbstractModel(opc, id, sig, func, args);
        }
        case OpCode.ICALL: {
            int func = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            return new AbstractModel(opc, id, func, args, kas);
        }
        case OpCode.IINVOKE: {
            int func = bis.readID();
            int nor = bis.readID();
            int exc = bis.readID();
            int[] args = readIDList();
            int[] kas = readIDList();
            return new AbstractModel(opc, id, func, nor, exc, args, kas);
        }

        }

        return null;
    }

    private void readNameBind(int opc) {
        try {
            int id = bis.readID();
            int len = bis.readLen();
            byte[] buf = new byte[len];
            bis.read(buf);
            String name = new String(buf, UTF8);
            bindings.put(id, name);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }
}
