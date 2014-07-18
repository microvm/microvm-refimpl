package uvm.refimpl.facade;

import uvm.refimpl.mem.scanning.ObjectMarker;

public interface MicroVMClient {
    void markExternalRoots(ObjectMarker marker);
}
