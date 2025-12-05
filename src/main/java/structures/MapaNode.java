package structures;

/**
 * Representa um par Chave-Valor (Key-Value pair) genérico para ser armazenado
 * no Dicionário (Mapa).
 * @param <K> O tipo da Chave (Key - ex: ID da Sala).
 * @param <V> O tipo do Valor (Value - ex: objeto Room ou Corridor).
 */
public class MapaNode<K, V> {

    // Campos privados: garantem que os dados só são alterados por métodos controlados.
    private K key;
    private V value;

    public MapaNode(K key, V value) {
        this.key = key;
        this.value = value;
    }

    // --- Métodos de Acesso (Para o MyDictionary/MapaAdt) ---

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    /**
     * Permite que a classe Mapa atualize o Valor (V) associado à Chave (K).
     */
    public void setValue(V value) {
        this.value = value;
    }
    
    // NOTA: Se o seu ArrayUnorderedList exigir que esta classe implemente Comparable, 
    // terá que adicionar o "implements Comparable<MapaNode<K, V>>" e o método compareTo.
}