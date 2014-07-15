package uvm.refimpl.facade;

import uvm.refimpl.mem.ObjectMarker;

public interface MicroVMClient {
    void markExternalRoots(ObjectMarker marker);
}
