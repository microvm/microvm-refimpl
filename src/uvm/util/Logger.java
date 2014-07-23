package uvm.util;

public class Logger {
    private String name;
    private boolean enabled;

    public Logger(String name) {
        this.name = name;
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void format(String fmt, Object... args) {
        if (enabled) {
            System.out.println(name + ": " + String.format(fmt, args));
        }
    }

}
