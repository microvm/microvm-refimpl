package uvm.platformsupport.ordinaryjava;

import uvm.platformsupport.VMThread;
import uvm.platformsupport.VMThreadFactory;

@Deprecated
public class JavaThreadFactory implements VMThreadFactory {

    @Override
    public VMThread newThread(Runnable runnable) {
        return new JavaThread(runnable);
    }
    
}
