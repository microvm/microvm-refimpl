package uvm;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A simple flat (non-nested) implementation of a namespace.
 * 
 * @param <T>
 *            The object stored.
 */
public class SimpleNamespace<T extends Identified> implements Namespace<T> {
    private Map<Integer, T> fromID = new LinkedHashMap<Integer, T>();
    private Map<String, Integer> nameToID = new LinkedHashMap<String, Integer>();

    @Override
    public T getByID(int id) {
        return fromID.get(id);
    }

    @Override
    public T getByName(String name) {
        return fromID.get(nameToID.get(name));
    }

    @Override
    public void put(int id, String name, T object) {
        fromID.put(id, object);
        if (name != null) {
            bind(id, name);
        }
    }

    @Override
    public void bind(int id, String name) {
        nameToID.put(name, id);
    }

    @Override
    public Set<Integer> getIDSet() {
        return fromID.keySet();
    }

    @Override
    public Set<String> getNameSet() {
        return nameToID.keySet();
    }

    @Override
    public Collection<T> getObjects() {
        return fromID.values();
    }


}
