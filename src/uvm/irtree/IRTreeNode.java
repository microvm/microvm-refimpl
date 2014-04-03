package uvm.irtree;

import java.util.List;

import uvm.Identified;

/**
 * IRTreeNode and its friends are a tree structure for tree-based instruction
 * selection.
 * 
 * @param <T>
 *            The type of the payload (probably BurmState) stored in a node.
 */
public interface IRTreeNode<T> extends Identified {
    /**
     * Get the opcode for the current node. Opcode is defined in uvm.OpCode
     * 
     * @return The opcode for the current node.
     */
    int getOpCode();

    /**
     * Get a list of child nodes. The exact children for each kind of node are
     * defined by the instruction selector and may be subject to change when the
     * instruction selector demands more information.
     * 
     * @return A list of child nodes.
     */
    List<IRTreeNode<T>> getChildren();

    /**
     * Get the currently stored payload.
     * 
     * @return The payload stored in the current node.
     */
    T getState();

    /**
     * Store a payload (usually BurmState) into the current node.
     * 
     * @param state
     *            The payload to store.
     */
    void setState(T state);
}
