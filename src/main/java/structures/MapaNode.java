package structures;

/**
 * Represents a generic key-value pair to be stored in a dictionary/map structure.
 * @param <K> the type of the key (for example, a room ID)
 * @param <V> the type of the value (for example, a Room or Corridor object)
 */
public class MapaNode<K, V> {

    /**
     * The key associated with this entry.
     */
    private K key;
    /**
     * The value associated with this entry.
     */
    private V value;

    /**
     * Creates a new MapaNode with the specified key and value.
     * @param key   the key to be stored
     * @param value the value associated with the given key
     */
    public MapaNode(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key stored in this node.
     * @return the key for this entry
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the value stored in this node.
     * @return the value associated with this key
     */
    public V getValue() {
        return value;
    }

    /**
     * Updates the value associated with this node's key.
     * @param value the new value to associate with this key
     */
    public void setValue(V value) {
        this.value = value;
    }
}