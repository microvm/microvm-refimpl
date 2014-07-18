package uvm.refimpl.itpr;

import uvm.type.Hybrid;
import uvm.type.Int;

public class MicroVMInternalTypes {

    public static final uvm.type.Void VOID_TYPE;
    public static final Int BYTE_TYPE;
    public static final Hybrid BYTE_ARRAY_TYPE;

    static {
        VOID_TYPE = new uvm.type.Void();
        VOID_TYPE.setID(0x10);
        VOID_TYPE.setName("@uvm.internal.void_type");
        BYTE_TYPE = new Int(8);
        BYTE_TYPE.setID(0x11);
        BYTE_TYPE.setName("@uvm.internal.byte_type");
        BYTE_ARRAY_TYPE = new Hybrid(VOID_TYPE, BYTE_TYPE);
        BYTE_ARRAY_TYPE.setID(0x12);
        BYTE_ARRAY_TYPE.setName("@uvm.internal.byte_array_type");
    }

}
