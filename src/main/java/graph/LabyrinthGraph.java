package graph;

import java.util.Iterator;

import Lists.ArrayUnorderedList;
import enums.CorredorEvento;
import game.EventoCorredor;
import structures.GraphList;
import structures.MapaNode;

public class LabyrinthGraph<T> extends GraphList<T> {

    // Índice = origem, lista tem pares <destino, evento>
    private ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[] edgeEvents;

    public LabyrinthGraph() {
        super();
        this.edgeEvents = (ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[]) new ArrayUnorderedList[DEFAULT_CAPACITY];
    }

    @Override
    protected void expandCapacity() {
        // Deixa o GraphList aumentar vertices / adjLists
        super.expandCapacity();

        ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[] largerEdgeEvents = (ArrayUnorderedList<MapaNode<Integer, EventoCorredor>>[]) new ArrayUnorderedList[vertices.length];

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

    // Retorna o evento associado a um corredor específico
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

        // Nota: adjLists vem da classe pai GraphList
        java.util.Iterator<Integer> it = adjLists[index].iterator();
        while (it.hasNext()) {
            int vizinhoIndex = it.next();
            vizinhos.addToRear(vertices[vizinhoIndex]);
        }
        return vizinhos;
    }

    // =========================================================================
    //  MÉTODOS PARA ARMADILHAS DINÂMICAS (ATUALIZAÇÃO E RELOCALIZAÇÃO)
    // =========================================================================

    /**
     * Atualiza o evento de um corredor existente (substitui o valor antigo).
     */
    public void setCorredorEvento(T vertex1, T vertex2, EventoCorredor novoEvento) {
        int index1 = getIndex(vertex1);
        int index2 = getIndex(vertex2);

        if (indexIsValid(index1) && indexIsValid(index2)) {
            // Atualizar nos dois sentidos (Grafo não direcionado)
            atualizarEventoUnico(index1, index2, novoEvento);
            atualizarEventoUnico(index2, index1, novoEvento);
        }
    }

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
     * Move uma armadilha do corredor atual para um corredor aleatório livre.
     */
    public void relocalizarArmadilha(T v1, T v2) {
        // 1. Guardar a armadilha que estava aqui (para a pôr noutro lado)
        EventoCorredor armadilha = getCorredorEvento(v1, v2);
        
        // 2. Limpar o corredor atual (Define como NONE - Seguro)
        setCorredorEvento(v1, v2, new EventoCorredor(CorredorEvento.NONE, 0));
        System.out.println("   👻 A armadilha desapareceu deste corredor...");

        // 3. Encontrar um novo sítio aleatório para a armadilha
        int tentativas = 50; // Limite para evitar loops infinitos se o mapa estiver cheio
        while (tentativas > 0) {
            // Escolhe uma sala aleatória (origem)
            int idx1 = (int)(Math.random() * numVertices);
            T salaAleatoria = vertices[idx1];
            
            // Vê os vizinhos dessa sala
            ArrayUnorderedList<T> vizinhos = getVizinhos(salaAleatoria);
            
            if (!vizinhos.isEmpty()) {
                // Escolhe um vizinho aleatório (destino)
                int randViz = (int)(Math.random() * vizinhos.size());
                Iterator<T> it = vizinhos.iterator();
                for(int k=0; k<randViz; k++) it.next(); // Avança até ao índice sorteado
                T vizinhoAleatorio = it.next();
                
                // Verifica se este corredor está vazio (seguro)
                EventoCorredor ev = getCorredorEvento(salaAleatoria, vizinhoAleatorio);
                
                // Só muda a armadilha para aqui se for NONE (não sobrepõe trancas ou outras armadilhas)
                if (ev.getTipo() == CorredorEvento.NONE) {
                    setCorredorEvento(salaAleatoria, vizinhoAleatorio, armadilha);
                    System.out.println("   👻 ...e mudou-se para o corredor entre [" + salaAleatoria.toString() + "] e [" + vizinhoAleatorio.toString() + "]!");
                    return; // Sucesso, saímos do método
                }
            }
            tentativas--;
        }
        System.out.println("   (A armadilha dissipou-se e não encontrou novo lugar).");
    }
}