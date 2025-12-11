package graph;

import java.util.Iterator;

import Lists.ArrayUnorderedList;
import enums.CorredorEvento;
import game.EventoCorredor;
import Graphs.GraphList;
import structures.MapaNode;
/**
 * Graph representation for the labyrinth game.
 * @param <T> the type of vertices stored in the labyrinth graph
 */
public class LabyrinthGraph<T> extends GraphList<T> {

    /**
     * For each vertex index, stores a list of key-value pairs where:
     */
    private ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[] edgeEvents;
    /**
     * Creates an empty LabyrinthGraph
     */
    public LabyrinthGraph() {
        super();
        this.edgeEvents = (ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[]) new ArrayUnorderedList[DEFAULT_CAPACITY];
    }
    /**
     * Expands the storage capacity when the number of vertices is reached
     */
    @Override
    protected void expandCapacity() {
        super.expandCapacity();

        ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[] largerEdgeEvents = (ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[]) new ArrayUnorderedList[vertices.length];

        for (int i = 0; i < edgeEvents.length; i++) {
            largerEdgeEvents[i] = edgeEvents[i];
        }

        edgeEvents = largerEdgeEvents;
    }

    /**
     * Adds an undirected edge between two vertices, associating the same event with both directions.
     * @param vertex1 the first vertex
     * @param vertex2 the second vertex
     * @param event   the corridor event to associate with this edge
     */
    public void addCorridor(T vertex1, T vertex2, EventoCorredor event) {
        super.addEdge(vertex1, vertex2);

        int index1 = getIndex(vertex1);
        int index2 = getIndex(vertex2);

        adicionarEventoUnico(index1, index2, event);
        adicionarEventoUnico(index2, index1, event); // bidirecional
    }

    /**
     * Associates a edge event from one origin vertex index to a destination index.
     * @param origem  the origin vertex index
     * @param destino the destination vertex index
     * @param event   the event to be stored
     */
    private void adicionarEventoUnico(int origem, int destino, EventoCorredor event) {
        if (edgeEvents[origem] == null) {
            edgeEvents[origem] = new ArrayUnorderedList<>();
        }
        MapaNode<Integer, EventoCorredor> node =
                new MapaNode<>(destino, event);
        edgeEvents[origem].addToRear(node);
    }

    /**
     * Returns the event associated with a specific edge between two vertices.
     * @param vertex1 the first vertex
     * @param vertex2 the second vertex
     * @return the EventoCorredor associated with the corridor
     */
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

        return new EventoCorredor(CorredorEvento.NONE, 0);
    }

    /**
     * Returns the current vertices array,
     * @return an Object containing all vertices stored in the graph
     */
    public Object[] getVertices() {
        Object[] verticesCopia = new Object[numVertices];
        for (int i = 0; i < numVertices; i++) {
            verticesCopia[i] = this.vertices[i];
        }
        return verticesCopia;
    }

    /**
     * Returns all neighbours of a given vertex as a list.
     * @param sala the vertex whose neighbours are requested
     * @return an ArrayUnorderedList with all adjacent vertices
     */
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

    //  MÉTODOS PARA ARMADILHAS

    /**
     * Updates the event of an existing edge, replacing the previous value.

     * @param vertex1    the first endpoint of the edge
     * @param vertex2    the second endpoint of the edge
     * @param novoEvento the new edge event to set
     */
    public void setCorredorEvento(T vertex1, T vertex2, EventoCorredor novoEvento) {
        int index1 = getIndex(vertex1);
        int index2 = getIndex(vertex2);

        if (indexIsValid(index1) && indexIsValid(index2)) {
            atualizarEventoUnico(index1, index2, novoEvento);
            atualizarEventoUnico(index2, index1, novoEvento);
        }
    }
    /**
     * Updates the event associated with an edge from one origin index to a destination index.
     * @param indexOrigem origin vertex index
     * @param indexDestino destination vertex index
     * @param novoEvento the new event to assign to that edge
     */
    private void atualizarEventoUnico(int indexOrigem, int indexDestino, EventoCorredor novoEvento) {
        if (edgeEvents[indexOrigem] != null) {
            Iterator<MapaNode<Integer, EventoCorredor>> it = edgeEvents[indexOrigem].iterator();
            while (it.hasNext()) {
                MapaNode<Integer, EventoCorredor> node = it.next();
                if (node.getKey().equals(indexDestino)) {
                    node.setValue(novoEvento); // Substitui o evento antigo pelo novo
                    return;
                }
            }
        }
    }

    /**
     * Relocates a trap from the given edge to a random safe edge.
     * @param v1 the first vertex of the original edge
     * @param v2 the second vertex of the original edge
     */
    public void relocalizarArmadilha(T v1, T v2) {

        EventoCorredor armadilha = getCorredorEvento(v1, v2);

        setCorredorEvento(v1, v2, new EventoCorredor(CorredorEvento.NONE, 0));
        System.out.println("   👻 A armadilha desapareceu deste corredor...");

        int tentativas = 50;
        while (tentativas > 0) {
            // Escolhe uma sala aleatória
            int idx1 = (int)(Math.random() * numVertices);
            T salaAleatoria = vertices[idx1];
            
            // Vê os vizinhos dessa sala
            ArrayUnorderedList<T> vizinhos = getVizinhos(salaAleatoria);
            
            if (!vizinhos.isEmpty()) {
                // Escolhe um vizinho aleatório
                int randViz = (int)(Math.random() * vizinhos.size());
                Iterator<T> it = vizinhos.iterator();
                for(int k=0; k<randViz; k++) it.next();
                T vizinhoAleatorio = it.next();
                
                // Verifica se este corredor está vazio
                EventoCorredor ev = getCorredorEvento(salaAleatoria, vizinhoAleatorio);

                if (ev.getTipo() == CorredorEvento.NONE) {
                    setCorredorEvento(salaAleatoria, vizinhoAleatorio, armadilha);
                    System.out.println("   👻 ...e mudou-se para o corredor entre [" + salaAleatoria.toString() + "] e [" + vizinhoAleatorio.toString() + "]!");
                    return;
                }
            }
            tentativas--;
        }
        System.out.println("   (A armadilha dissipou-se e não encontrou novo lugar).");
    }
}