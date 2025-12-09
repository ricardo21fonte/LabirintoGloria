package structures;

import java.util.Iterator;//mudar este

import Lists.ArrayUnorderedList; 

/**
 * Implementação customizada de um Dicionário (Mapa) Chave-Valor.
 * Usa a ArrayUnorderedList para o armazenamento interno.
 * * @param <K> O tipo da Chave (Key - ex: ID da Sala/Corredor)
 * @param <V> O tipo do Valor (Value - Objeto Room/Corridor)
 */
public class MapaAdt<K, V> {

    // A estrutura subjacente que armazena os pares Key-Value (MapaNode)
    private ArrayUnorderedList<MapaNode<K, V>> entries;

    public MapaAdt() {
        // Inicializa o armazenamento do Mapa usando a sua lista customizada
        this.entries = new ArrayUnorderedList<>();
    }

    /**
     * Adiciona ou atualiza um par Chave-Valor.
     * Complexidade: O(n) (tempo linear), pois percorre a lista interna para procurar a chave.
     * @param key A chave para procurar.
     * @param value O valor a ser armazenado.
     */
    public void put(K key, V value) {
        Iterator<MapaNode<K, V>> it = entries.iterator();
        while (it.hasNext()) {
            MapaNode<K, V> node = it.next();
            // Se a chave já existe, atualiza o valor
            if (node.getKey().equals(key)) {
                node.setValue(value);
                return;
            }
        }
        // Se a chave não foi encontrada, cria um novo nó e adiciona ao fim
        MapaNode<K, V> newNode = new MapaNode<>(key, value);
        entries.addToRear(newNode);
    }

    /**
     * Retorna o valor associado à chave, ou null se não for encontrado.
     * Complexidade: O(n) (tempo linear), devido à busca na lista interna.
     * @param key A chave para procurar.
     * @return O Valor (Value) V associado à chave, ou null.
     */
    public V get(K key) {
        Iterator<MapaNode<K, V>> it = entries.iterator();
        while (it.hasNext()) {
            MapaNode<K, V> node = it.next();
            // Se a chave for igual, retorna o Valor (o objeto Room/Corridor)
            if (node.getKey().equals(key)) {
                return node.getValue();
            }
        }
        return null;
    }

    /**
     * Verifica se a chave existe no dicionário.
     */
    public boolean containsKey(K key) {
        // Complexidade: O(n)
        return get(key) != null;
    }

    /**
     * Devolve o número de entradas (pares K-V) no dicionário.
     * Complexidade: O(1) (tempo constante), usando o size() da lista interna.
     */
    public int size() {
        return entries.size();
    }
}