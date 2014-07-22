package uvm.refimpl.itpr;

import java.util.HashMap;
import java.util.Map;

import uvm.refimpl.facade.MicroVM;

public class TrapManager {
    private MicroVM microVM;

    private Map<Integer, Boolean> watchpointEnabled = new HashMap<Integer, Boolean>();

    public TrapManager(MicroVM microVM) {
        this.microVM = microVM;
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

}
