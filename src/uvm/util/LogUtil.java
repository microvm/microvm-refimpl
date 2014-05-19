package uvm.util;

public class LogUtil {

    public static void log(String fmt, Object... args) {
        if (LogUtil.DEBUG) {
            System.err.format(fmt, args);
        }
    }

    public static final boolean DEBUG = false;

}
