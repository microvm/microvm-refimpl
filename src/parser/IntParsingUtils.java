package parser;

/**
 * Some helper functions for int parsing.
 */
public class IntParsingUtils {

    /**
     * A hand-written long num parser. If the input is between
     * 0x8000000000000000 and 0xffffffffffffffff, it will be represented by a
     * Java negative long number with the same bits in 2's complement
     * representation.
     * 
     * @param str
     *            The input number string. Must not contain '-'.
     * @return The result as a long.
     */
    public static long manualParse(String str, long base) {
        long res = 0;
        for (int i = 0; i < str.length(); i++) {
            int ch = str.charAt(i);
            long digit = ch <= '9' ? (long) (ch - '0')
                    : ch <= 'F' ? (long) (ch - 'A' + 10)
                            : (long) (ch - 'a' + 10);
            res = res * base + digit;
        }
        return res;
    }
}
