package uvm.refimpl.mem;

public class MemConstants {
    public static final long WORD_SIZE_LOG = 6L;
    public static final long WORD_SIZE_BITS = 1L << WORD_SIZE_LOG;
    public static final long WORD_SIZE_BYTES = 1L << (WORD_SIZE_LOG - 3L);
}
