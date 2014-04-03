package uvm.irtree;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the base implementation of the IRTreeNode interface.
 */
public abstract class BaseIRTreeNode<T> implements IRTreeNode<T> {
    private List<IRTreeNode<T>> children = new ArrayList<IRTreeNode<T>>();
    private T state;

    @Override
    public List<IRTreeNode<T>> getChildren() {
        return this.children;
    }

    public T getState() {
        return state;
    }

    public void setState(T state) {
        this.state = state;
    }

}
