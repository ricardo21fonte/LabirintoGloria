package game;

import Lists.ArrayUnorderedList;
import enums.CorredorEvento;
import structures.GraphList;
import structures.MapaNode;

import java.util.Iterator;

public class LabyrinthGraph<T> extends GraphList<T> {

    // Índice = origem, lista tem pares <destino, evento>
    private ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[] edgeEvents;

    @SuppressWarnings("unchecked")
    public LabyrinthGraph() {
        super();
        this.edgeEvents = (ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[])
                new ArrayUnorderedList[DEFAULT_CAPACITY];
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void expandCapacity() {
        // Deixa o GraphList aumentar vertices / adjLists
        super.expandCapacity();

        ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[] largerEdgeEvents =
                (ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[])
                        new ArrayUnorderedList[vertices.length];

        // copiar existentes
        for (int i = 0; i < edgeEvents.length; i++) {
            largerEdgeEvents[i] = edgeEvents[i];
        }

        edgeEvents = largerEdgeEvents;
    }

    // Adiciona corredor com evento (nos dois sentidos)
    public void addCorridor(T vertex1, T vertex2, EventoCorredor event) {
        super.addEdge(vertex1, vertex2);

        int index1 = getIndex(vertex1);
        int index2 = getIndex(vertex2);

        adicionarEventoUnico(index1, index2, event);
        adicionarEventoUnico(index2, index1, event); // bidirecional
    }

    private void adicionarEventoUnico(int origem, int destino, EventoCorredor event) {
        if (edgeEvents[origem] == null) {
            edgeEvents[origem] = new ArrayUnorderedList<>();
        }
        MapaNode<Integer, EventoCorredor> node =
                new MapaNode<>(destino, event);
        edgeEvents[origem].addToRear(node);
    }

    // >>> NOME DO MÉTODO AJUSTADO PARA PORTUGUÊS <<<
    public EventoCorredor getCorredorEvento(T vertex1, T vertex2) {
        int index1 = getIndex(vertex1);
        int index2 = getIndex(vertex2);

        if (!indexIsValid(index1) || edgeEvents[index1] == null) {
            return new EventoCorredor(CorredorEvento.NONE, 0);
        }

        Iterator<MapaNode<Integer, EventoCorredor>> it = edgeEvents[index1].iterator();
        while (it.hasNext()) {
            MapaNode<Integer, EventoCorredor> node = it.next();
            if (node.getKey().equals(index2)) {
                return node.getValue();
            }
        }

        // se não tiver evento específico, é seguro
        return new EventoCorredor(CorredorEvento.NONE, 0);
    }

    public Object[] getVertices() {
        Object[] verticesCopia = new Object[numVertices];
        for (int i = 0; i < numVertices; i++) {
            verticesCopia[i] = this.vertices[i];
        }
        return verticesCopia;
    }

    public Lists.ArrayUnorderedList<T> getVizinhos(T sala) {
        Lists.ArrayUnorderedList<T> vizinhos = new Lists.ArrayUnorderedList<>();
        int index = getIndex(sala);

        if (!indexIsValid(index)) return vizinhos;

        java.util.Iterator<Integer> it = adjLists[index].iterator();
        while (it.hasNext()) {
            int vizinhoIndex = it.next();
            vizinhos.addToRear(vertices[vizinhoIndex]);
        }
        return vizinhos;
    }
}
