package uvm.ssavalue;

import java.util.List;

public interface HasKeepAlives {

    List<UseBox> getKeepAlives();

    /**
     * Add a new keep-alive register. A UseBox will be created.
     * 
     * @param ka
     *            The register.
     */
    void addKeepAlive(Value ka);
}
