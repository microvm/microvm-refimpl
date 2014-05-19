package uvm.ir.binary.input;

import uvm.BasicBlock;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
import uvm.ifunc.IFunc;
import uvm.ifunc.IFuncFactory;
import uvm.ssavalue.Constant;
import uvm.ssavalue.Value;
import uvm.type.Type;

public abstract class AbstractResolver {

    protected IRBinaryReader br;

    public AbstractResolver(IRBinaryReader br) {
        this.br = br;
    }

    protected Type type(int id) {
        return br.bundle.getTypeNs().getByID(id);
    }

    protected FunctionSignature sig(int id) {
        return br.bundle.getFuncSigNs().getByID(id);
    }

    protected GlobalData global(int id) {
        return br.bundle.getGlobalDataNs().getByID(id);
    }

    protected Function func(int id) {
        return br.bundle.getFuncNs().getByID(id);
    }

    protected Constant constant(int id) {
        return br.bundle.getGlobalValueNs().getByID(id);
    }

    protected Value value(int id) {
        return br.allValues.get(id);
    }

    protected BasicBlock bb(int id) {
        return br.allBBs.get(id);
    }

    protected IFunc ifunc(int i) {
        return IFuncFactory.getIFuncByID(i);
    }

}
