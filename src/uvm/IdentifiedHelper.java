package uvm;

public class IdentifiedHelper {

    public static String optName(String name) {
        return name != null ? name : "_";
    }

    public static String optName(Identified ided) {
        return optName(ided.getName());
    }

    public static String repr(Identified ided) {
        return String.format("[%d:%s]", ided.getID(), optName(ided));
    }

}
