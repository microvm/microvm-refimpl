package uvm.ssavalue;

import uvm.BasicBlock;

public interface HandlesException {

    public abstract BasicBlock getNor();

    public abstract void setNor(BasicBlock nor);

    public abstract BasicBlock getExc();

    public abstract void setExc(BasicBlock exc);

}