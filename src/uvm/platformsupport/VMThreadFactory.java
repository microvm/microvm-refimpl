package uvm.platformsupport;

@Deprecated
public interface VMThreadFactory {
    VMThread newThread(Runnable runnable);
}
