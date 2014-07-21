package uvm.refimpl.itpr;

import java.util.HashMap;
import java.util.Map;

import uvm.refimpl.facade.MicroVM;

public class TrapManager {
    private MicroVM microVM;

    private TrapHandler trapHandler;

    private UndefinedFunctionHanlder undefinedFunctionHandler;

    private Map<Integer, Boolean> watchpointEnabled = new HashMap<Integer, Boolean>();

    public TrapManager(MicroVM microVM) {
        this.microVM = microVM;
    }

    public TrapHandler getTrapHandler() {
        return trapHandler;
    }

    public void setTrapHandler(TrapHandler trapHandler) {
        this.trapHandler = trapHandler;
    }

    public MicroVM getMicroVM() {
        return microVM;
    }

    public void newWatchpoint(int id) {
        // TODO µVM semantic: if a watchpoint is enabled and new µVM-IR code
        // contains the same watchpoint, should it be enabled in the beginning?
        watchpointEnabled.put(id, false);
    }

    public boolean isWatchpointEnabled(int id) {
        return watchpointEnabled.get(id);
    }

    public void enableWatchpoint(int id) {
        watchpointEnabled.put(id, true);
    }

    public UndefinedFunctionHanlder getUndefinedFunctionHandler() {
        return undefinedFunctionHandler;
    }

    public void setUndefinedFunctionHandler(
            UndefinedFunctionHanlder undefinedFunctionHandler) {
        this.undefinedFunctionHandler = undefinedFunctionHandler;
    }

}
