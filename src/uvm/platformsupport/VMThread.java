package uvm.platformsupport;

/**
 * @deprecated RJava should implement java.lang.Thread.
 */
@Deprecated
public interface VMThread {
    void setDaemon(boolean on);

    void start();

    void join() throws InterruptedException;
}
