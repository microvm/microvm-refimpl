package uvm.util;

/**
 * This utility class reports error conditions. RJava implementation can replace
 * this class.
 */
public class ErrorUtils {
    public static void uvmAssert(boolean expr, String msg) {
        if (!expr) {
            throw new RuntimeException(msg);
        }
    }

    public static void uvmError(String msg) {
        throw new RuntimeException(msg);
    }
}
