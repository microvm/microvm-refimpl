package uvm.platformsupport.ordinaryjava;

import uvm.platformsupport.VMThread;

@Deprecated
public class JavaThread extends Thread implements VMThread {

    public JavaThread(Runnable runnable) {
        super(runnable);
    }

}
