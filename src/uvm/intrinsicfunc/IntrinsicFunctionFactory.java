package uvm.intrinsicfunc;

/**
 * This class lists all supported intrinsic functions. Instances can be
 * retrieved.
 */
public class IntrinsicFunctionFactory {

    /**
     * Not yet implemented. When implemented, there will be one instance for
     * each intrinsic function.
     */
    private static final GenericIntrinsicFunction PLACE_HOLDER = new GenericIntrinsicFunction(
            null);

    /**
     * Get the instance of an intrinsic function by name.
     * 
     * @param name
     *            The name of the intrinsic function.
     * @return The IntrinsicFunction instance for the name.
     */
    public static IntrinsicFunction getIntrinsicFunctionByName(String name) {
        return PLACE_HOLDER;
    }
}
