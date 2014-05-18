package uvm.ir.io;

/**
 * Wrapper for IOException so that the exception is not part of the function
 * signatures of BinaryOutputStream.
 */
public class NestedIOException extends RuntimeException {
    public NestedIOException() {
        super();
    }

    public NestedIOException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NestedIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public NestedIOException(String message) {
        super(message);
    }

    public NestedIOException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = -5333059096574440177L;
}
