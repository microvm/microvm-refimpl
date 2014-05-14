package uvm.ir.textoutput;

import static uvm.ir.textoutput.WritingHelper.g;
import static uvm.ir.textoutput.WritingHelper.l;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import uvm.BasicBlock;
import uvm.Bundle;
import uvm.CFG;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
import uvm.ssavalue.Constant;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.Parameter;
import uvm.type.Type;

/**
 * This package writes a bundle in the text form.
 * <p>
 * TODO: In the future, it should be written at a larger-than-bundle level.
 */
public class IRTextWriter {
    private PrintWriter pw;
    private TypeWriter TYPE_WRITER = new TypeWriter(this);
    private ValueWriter VALUE_WRITER = new ValueWriter(this);

    private List<String> pendingComments = new ArrayList<String>();

    public IRTextWriter(Writer writer) {
        this.pw = new PrintWriter(writer);
    }

    public void writeBundle(Bundle bundle) {
        for (Type type : bundle.getTypeNs().getObjects()) {
            writeTypeDef(type);
        }

        for (FunctionSignature sig : bundle.getFuncSigNs().getObjects()) {
            writeFuncSigDef(sig);
        }

        for (Constant constant : bundle.getDeclaredConstNs().getObjects()) {
            writeConstant(constant);
        }

        for (GlobalData globalData : bundle.getGlobalDataNs().getObjects()) {
            writeGlobalData(globalData);
        }

        for (Function function : bundle.getFuncNs().getObjects()) {
            if (function.getCFG() == null) {
                writeFuncDecl(function);
            } else {
                writeFuncDef(function);
            }
        }

        pw.flush();
    }

    public void addComment(String comment) {
        pendingComments.add(comment);
    }

    private void writeComments() {
        for (String comment : pendingComments) {
            pw.print("// ");
            pw.println(comment.replace('\n', ' '));
        }
        pendingComments.clear();
    }

    private void writeTypeDef(Type type) {
        pw.printf(".typedef %s = %s\n", g(type), type.accept(TYPE_WRITER));
        writeComments();
    }

    private void writeFuncSigDef(FunctionSignature sig) {
        StringBuilder sb = new StringBuilder();
        sb.append(g(sig.getReturnType()));
        sb.append(" ( ");
        for (Type paramTy : sig.getParamTypes()) {
            sb.append(g(paramTy)).append(" ");
        }
        sb.append(")");
        pw.printf(".funcsig %s <%s>\n", g(sig), sb.toString());
        writeComments();
    }

    private void writeConstant(Constant constant) {
        pw.printf(".const %s <%s> = %s\n", g(constant), g(constant.getType()),
                constant.accept(VALUE_WRITER));
        writeComments();
    }

    private void writeGlobalData(GlobalData globalData) {
        pw.printf(".global %s <%s>\n", g(globalData), g(globalData.getType()));
        writeComments();
    }

    private void writeFuncDecl(Function function) {
        pw.printf(".funcdecl %s <%s>\n", g(function), g(function.getSig()));
        writeComments();
    }

    private void writeFuncDef(Function function) {
        StringBuilder paramsSb = new StringBuilder(" ");
        CFG cfg = function.getCFG();

        for (Parameter param : cfg.getParams()) {
            paramsSb.append(l(param));
            paramsSb.append(" ");
        }

        pw.printf(".funcdef %s <%s> (%s) {\n", g(function),
                g(function.getSig()), paramsSb.toString());
        writeComments();

        for (BasicBlock bb : cfg.getBBs()) {
            pw.printf("    %s:\n", l(bb));

            for (Instruction inst : bb.getInsts()) {
                pw.printf("        %s = %s\n", l(inst),
                        inst.accept(VALUE_WRITER));
                writeComments();
            }
        }

        pw.println("}");

    }
}
