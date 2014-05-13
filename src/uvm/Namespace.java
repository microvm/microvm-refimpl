package uvm;

import java.util.Collection;
import java.util.Set;

/**
 * A namespace interface where objects can be looked up by ID or name and can be
 * added by providing ID and/or names.
 * 
 * @param <T>
 *            The type of the object stored.
 */
public interface Namespace<T extends Identified> {
    /**
     * Get an object with a given ID.
     * @param id The ID.
     * @return The associated object, or null if no such object.
     */
    public T getByID(int id);

    /**
     * Get an object with a given name.
     * @param name The name.
     * @return The associated object, or null if no such object.
     */
    public T getByName(String name);

    /**
     * Put an object into this namespace and provide its ID and/or name.
     * @param id The ID.
     * @param name (Optional) The name, or null if no name.
     * @param object The object.
     */
    public void put(int id, String name, T object);

    /**
     * Associate a name with an ID. 
     * @param id The ID.
     * @param name The name.
     */
    public void bind(int id, String name);
    
    /**
     * Returns a set of all IDs defined in this namespace
     * @return a set of all IDs.
     */
    public Set<Integer> getIDSet();
    
    /**
     * Returns a set of all names defined in this namespace
     * @return a set of all names.
     */
    public Set<String> getNameSet();
    
    /**
     * Returns a collection of all objects stored in this namespace
     * @return a collection of all objects.
     */
    public Collection<T> getObjects();
}
