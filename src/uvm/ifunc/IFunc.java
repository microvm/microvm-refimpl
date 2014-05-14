package uvm.ifunc;

import uvm.Identified;
import uvm.type.Type;

/**
 * The base parent of all intrinsic functions.
 */
public interface IFunc extends Identified {
    Type getType();
}
