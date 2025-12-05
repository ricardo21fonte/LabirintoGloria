package game;

import Lists.ArrayUnorderedList;
import enums.CorredorEvent;
import structures.GraphList;
import structures.MapaNode;
import java.util.Iterator; // Necessário para percorrer a lista

public class LabyrinthGraph<T> extends GraphList<T> {

    // Estrutura auxiliar para guardar os eventos das arestas.
    // Índice do Array = Sala de Origem
    // MapaNode = {Destino, Evento}
    private ArrayUnorderedList<MapaNode<Integer, CorredorEvent>>[] edgeEvents;

    public LabyrinthGraph() {
        super();
        this.edgeEvents = new ArrayUnorderedList[DEFAULT_CAPACITY];
    }

    /**
     * IMPORTANTE: Aumenta a capacidade do array de eventos quando o grafo cresce.
     * Sem isto, o jogo falha se tiveres mais de 10 salas.
     */
    @Override
    protected void expandCapacity() {
        super.expandCapacity(); // Deixa o pai aumentar vertices e adjLists

        // Agora aumentamos o nosso array de eventos
        ArrayUnorderedList<MapaNode<Integer, CorredorEvent>>[] largerEdgeEvents = 
            new ArrayUnorderedList[edgeEvents.length * 2];

        for (int i = 0; i < numVertices; i++) {
            largerEdgeEvents[i] = edgeEvents[i];
        }

        edgeEvents = largerEdgeEvents;
    }

    /**
     * Adiciona um corredor com evento.
     * Adiciona nos DOIS sentidos (ida e volta) para ser consistente.
     */
    public void addCorridor(T vertex1, T vertex2, CorredorEvent event) {
        // 1. Cria a ligação lógica no grafo pai
        super.addEdge(vertex1, vertex2);

        // 2. Guarda o evento na estrutura auxiliar
        int index1 = getIndex(vertex1);
        int index2 = getIndex(vertex2);

        // Sentido Ida (A -> B)
        adicionarEventoUnico(index1, index2, event);
        
        // Sentido Volta (B -> A) - Opcional, mas recomendado para labirintos
        adicionarEventoUnico(index2, index1, event);
    }

    // Método auxiliar para não repetir código
    private void adicionarEventoUnico(int origem, int destino, CorredorEvent event) {
        if (edgeEvents[origem] == null) {
            edgeEvents[origem] = new ArrayUnorderedList<>();
        }
        MapaNode<Integer, CorredorEvent> node = new MapaNode<>(destino, event);
        edgeEvents[origem].addToRear(node);
    }

    /**
     * Retorna o evento do corredor. 
     * Renomeado de 'getEvent' para 'getCorridorEvent' para bater certo com o Labirinto.java
     */
    public CorredorEvent getCorridorEvent(T vertex1, T vertex2) {
        int index1 = getIndex(vertex1);
        int index2 = getIndex(vertex2);

        // Se o índice for inválido ou não houver lista de eventos, retorna NONE
        if (!indexIsValid(index1) || edgeEvents[index1] == null) {
            return new CorredorEvent(CorredorEvent.Type.NONE, 0);
        }

        // Procura na lista da origem
        Iterator<MapaNode<Integer, CorredorEvent>> it = edgeEvents[index1].iterator();
        while (it.hasNext()) {
            MapaNode<Integer, CorredorEvent> node = it.next();
            if (node.getKey().equals(index2)) {
                return node.getValue();
            }
        }

        // Se não encontrar evento específico, assume que é seguro
        return new CorredorEvent(CorredorEvent.Type.NONE, 0);
    }
    
    // Método auxiliar que o GraphList pode não expor publicamente
    public Object[] getVertices() {
        Object[] verticesCopia = new Object[numVertices];
        for (int i = 0; i < numVertices; i++) {
            verticesCopia[i] = this.vertices[i];
        }
        return verticesCopia;
    }

    /**
     * Retorna uma lista de salas ligadas à sala atual.
     * Útil para mostrar opções de movimento ao jogador.
     */
    public Lists.ArrayUnorderedList<T> getVizinhos(T sala) {
        Lists.ArrayUnorderedList<T> vizinhos = new Lists.ArrayUnorderedList<>();
        int index = getIndex(sala);

        if (!indexIsValid(index)) return vizinhos;

        // Percorrer a lista de adjacências deste vértice
        java.util.Iterator<Integer> it = adjLists[index].iterator();
        while (it.hasNext()) {
            int vizinhoIndex = it.next();
            vizinhos.addToRear(vertices[vizinhoIndex]);
        }
        return vizinhos;
    }
}