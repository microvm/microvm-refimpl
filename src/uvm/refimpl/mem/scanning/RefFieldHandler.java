package uvm.refimpl.mem.scanning;

import uvm.refimpl.itpr.HasObjRef;

public interface RefFieldHandler {
    public boolean handle(boolean fromClient, HasObjRef fromBox, long fromObj,
            long fromIRef, long toObj, boolean isWeak, boolean isTR64);
}