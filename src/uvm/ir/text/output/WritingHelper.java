package uvm.ir.text.output;

import uvm.Identified;

/**
 * Miscellaneous methods
 */
public class WritingHelper {
    public static String g(Identified i) {
        String name = i.getName();
        if (name != null) {
            return name;
        } else {
            return "@" + i.getID();
        }
    }
    
    public static String l(Identified i) {
        String name = i.getName();
        if (name != null) {
            return name;
        } else {
            return "%" + i.getID();
        }
    }
}
