package uvm.refimpl.mem;

public class MemConstants {
    public static final int WORD_SIZE_LOG = 6;
    public static final int WORD_SIZE_BITS = 1<<WORD_SIZE_LOG;
    public static final int WORD_SIZE_BYTES = 1<<(WORD_SIZE_LOG-3);
}
