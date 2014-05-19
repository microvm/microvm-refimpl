package uvm.ir.binary.output;

import static uvm.TopLevelOpCodes.CONST;
import static uvm.TopLevelOpCodes.FUNCDECL;
import static uvm.TopLevelOpCodes.FUNCDEF;
import static uvm.TopLevelOpCodes.FUNCSIG;
import static uvm.TopLevelOpCodes.GLOBAL;
import static uvm.TopLevelOpCodes.NAMEBIND;
import static uvm.TopLevelOpCodes.TYPEDEF;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import uvm.BasicBlock;
import uvm.Bundle;
import uvm.CFG;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
import uvm.Identified;
import uvm.ir.io.NestedIOException;
import uvm.ssavalue.Constant;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.Parameter;
import uvm.type.Type;

/**
 * This package writes a bundle in the binary form.
 * <p>
 * TODO: In the future, it should be written at a larger-than-bundle level.
 */
public class IRBinaryWriter implements Closeable {
    private static final String UTF8 = "UTF-8";
    private TypeWriter TYPE_WRITER;
    private ValueWriter VALUE_WRITER;

    BinaryOutputStream bos;

    public IRBinaryWriter(OutputStream os) {
        this.bos = new BinaryOutputStream(os);
        TYPE_WRITER = new TypeWriter(this);
        VALUE_WRITER = new ValueWriter(this);
    }

    @Override
    public void close() throws IOException {
        bos.close();
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

        writeNameBinds(bundle);

        try {
            bos.flush();
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    private void writeTypeDef(Type type) {
        bos.writeOpc(TYPEDEF);
        bos.writeID(type);
        type.accept(TYPE_WRITER);
    }

    private void writeFuncSigDef(FunctionSignature sig) {
        bos.writeOpc(FUNCSIG);
        bos.writeID(sig.getReturnType());
        bos.writeLen(sig.getParamTypes());
        for (Type paramTy : sig.getParamTypes()) {
            bos.writeID(paramTy);
        }
    }

    private void writeConstant(Constant constant) {
        bos.writeOpc(CONST);
        bos.writeID(constant);
        bos.writeID(constant.getType());
        constant.accept(VALUE_WRITER);
    }

    private void writeGlobalData(GlobalData globalData) {
        bos.writeOpc(GLOBAL);
        bos.writeID(globalData);
        bos.writeID(globalData.getType());
    }

    private void writeFuncDecl(Function function) {
        bos.writeOpc(FUNCDECL);
        bos.writeID(function);
        bos.writeID(function.getSig());
    }

    private void writeFuncDef(Function function) {
        bos.writeOpc(FUNCDEF);
        bos.writeID(function);
        bos.writeID(function.getSig());

        CFG cfg = function.getCFG();

        List<Parameter> params = cfg.getParams();
        bos.writeLen(params);

        for (Parameter param : params) {
            bos.writeID(param);
        }

        List<BasicBlock> bbs = cfg.getBBs();
        bos.writeLen(bbs.size());

        for (BasicBlock bb : bbs) {
            bos.writeID(bb);
            
            List<Instruction> insts = bb.getInsts();
            bos.writeInt(insts.size());

            for (Instruction inst : insts) {
                inst.accept(VALUE_WRITER);
            }
        }
    }

    private void maybeWriteNameBind(Identified obj) {
        try {
            if (obj.getName() != null) {
                bos.writeOpc(NAMEBIND);
                bos.writeID(obj);

                byte[] nameUTF8 = obj.getName().getBytes(UTF8);
                bos.writeLen(nameUTF8.length);
                bos.write(nameUTF8);
            }
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    private void writeNameBinds(Bundle bundle) {
        for (Type type : bundle.getTypeNs().getObjects()) {
            maybeWriteNameBind(type);
        }

        for (FunctionSignature sig : bundle.getFuncSigNs().getObjects()) {
            maybeWriteNameBind(sig);
        }

        for (Constant constant : bundle.getDeclaredConstNs().getObjects()) {
            maybeWriteNameBind(constant);
        }

        for (GlobalData globalData : bundle.getGlobalDataNs().getObjects()) {
            maybeWriteNameBind(globalData);
        }

        for (Function function : bundle.getFuncNs().getObjects()) {
            maybeWriteNameBind(function);

            CFG cfg = function.getCFG();
            if (cfg == null) {
                continue;
            }

            for (Parameter param : cfg.getParams()) {
                maybeWriteNameBind(param);
            }

            for (BasicBlock bb : cfg.getBBs()) {
                maybeWriteNameBind(bb);
                for (Instruction inst : bb.getInsts()) {
                    maybeWriteNameBind(inst);
                }
            }
        }
    }
}
