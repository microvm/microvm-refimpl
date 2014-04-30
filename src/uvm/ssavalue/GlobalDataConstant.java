package uvm.ssavalue;

import uvm.GlobalData;
import uvm.OpCode;
import uvm.type.IRef;
import uvm.type.Type;

/**
 * A constant SSA Value that represents the address of a piece of global data.
 */
public class GlobalDataConstant extends Constant {

    private GlobalData globalData;

    /**
     * The type of this constant. That is, an iref to the global data's type.
     */
    private Type type;

    public GlobalData getGlobalData() {
        return globalData;
    }

    public void setGlobalData(GlobalData globalData) {
        this.globalData = globalData;
        type = new IRef(globalData.getType());
    }

    @Override
    public int opcode() {
        return OpCode.GDATAIREF_IMM;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visitGlobalDataConstant(this);
    }

}
