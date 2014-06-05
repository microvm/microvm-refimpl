package uvm.refimpl.mem;

/**
 * A Space is a contiguous region of memory. Instances of Space or its
 * subclasses represent such regions and contain operations on them.
 */
public class Space {
    protected String name;
    protected long begin;
    protected long extend;

    public Space(String name, long begin, long extend) {
        super();
        this.name = name;
        this.begin = begin;
        this.extend = extend;
    }
    
    public boolean isInSpace(long addr) {
        return begin <= addr && addr < begin + extend;
    }

    private static final int MAX_SPACES = 20;
    private static Space[] spaces = new Space[MAX_SPACES];
    
    public static Space getSpaceForAddress(long addr) {
        for (int i=0; i<MAX_SPACES; i++) {
            if (spaces[i] != null && spaces[i].isInSpace(addr)) {
                return spaces[i];
            }
        }
        return null;
    }

}
