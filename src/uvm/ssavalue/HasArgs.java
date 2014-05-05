package uvm.ssavalue;

import java.util.List;

public interface HasArgs {
    List<UseBox> getArgs();

    void addArg(Value arg);
}
