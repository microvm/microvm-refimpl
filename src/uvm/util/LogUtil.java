package uvm.util;

import java.util.HashMap;
import java.util.Map;

public class LogUtil {
    private static Map<String, Logger> loggers = new HashMap<String, Logger>();

    public static synchronized Logger getLogger(String name) {
        if (loggers.containsKey(name)) {
            return loggers.get(name);
        } else {
            Logger logger = new Logger(name);
            loggers.put(name, logger);
            return logger;
        }
    }

    public static synchronized void enableLoggers(String... names) {
        for (String name : names) {
            getLogger(name).setEnabled(true);
        }
    }

    public static synchronized void disableLoggers(String... names) {
        for (String name : names) {
            getLogger(name).setEnabled(false);
        }
    }

}
