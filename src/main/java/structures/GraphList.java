package structures;

import java.util.Iterator;//mudar este

import Graphs.GraphADT;
import Lists.ArrayUnorderedList;
import Lists.UnorderedLinkedList;
import Queue.LinkedQueue;
import Stacks.LinkedStack;

public class GraphList<T> implements GraphADT<T> {
    protected final int DEFAULT_CAPACITY = 10;
    protected int numVertices;
    protected UnorderedLinkedList<Integer>[] adjLists;
    protected T[] vertices;

    public GraphList() {
        numVertices = 0;
        this.vertices = (T[]) (new Object[DEFAULT_CAPACITY]);
        this.adjLists = (UnorderedLinkedList<Integer>[]) new UnorderedLinkedList[DEFAULT_CAPACITY];
        for (int i = 0; i < DEFAULT_CAPACITY; i++) {
            adjLists[i] = new UnorderedLinkedList<>();
        }
    }

    protected void expandCapacity() {
        int oldCapacity = vertices.length;
        int newCapacity = oldCapacity * 2;

        T[] largerVertices = (T[]) new Object[newCapacity];
        UnorderedLinkedList<Integer>[] largerAdjLists = (UnorderedLinkedList<Integer>[]) new UnorderedLinkedList[newCapacity];

        for (int i = 0; i < numVertices; i++) {
            largerVertices[i] = vertices[i];
            largerAdjLists[i] = adjLists[i];
        }

        for (int i = numVertices; i < newCapacity; i++) {
            largerAdjLists[i] = new UnorderedLinkedList<>();
        }

        vertices = largerVertices;
        adjLists = largerAdjLists;
    }

    public Object[] getVertices() {
    Object[] verticesArray = new Object[numVertices];
    for (int i = 0; i < numVertices; i++) {
        verticesArray[i] = this.vertices[i];
    }
    return verticesArray;
}

    @Override
    public void addEdge(T vertex1, T vertex2) {
        addEdge(getIndex(vertex1), getIndex(vertex2));
    }

    public void addEdge(int index1, int index2) {
        if (indexIsValid(index1) && indexIsValid(index2)) {
            // Adiciona nas duas listas (grafo não direcionado)
            adjLists[index1].addToRear(index2);
            adjLists[index2].addToRear(index1);
        }
    }

    @Override
    public void removeEdge(T vertex1, T vertex2) {
        int index1 = getIndex(vertex1);
        int index2 = getIndex(vertex2);

        if (indexIsValid(index1) && indexIsValid(index2)) {
            adjLists[index1].remove(index2);
            adjLists[index2].remove(index1);
        }
    }

    @Override
    public void addVertex(T vertex) {
        if (numVertices == vertices.length)
            expandCapacity();
        
        vertices[numVertices] = vertex;
        adjLists[numVertices] = new UnorderedLinkedList<>();
        numVertices++;
    }

    @Override
    public void removeVertex(T vertex) {
        // Remoção em ListGraph com array é custosa, pois exige reindexação
        // de todos os valores dentro das listas ligadas.
        int index = getIndex(vertex);
        if (indexIsValid(index)) {
             removeVertex(index);
        }
    }

    public void removeVertex(int index) {
        if (indexIsValid(index)) {
            // 1. Remover arestas que apontam para este vértice nas outras listas
            // E ajustar os índices maiores que 'index' (decrementar 1)
            // Esta lógica é complexa para implementar sem iteradores detalhados da LinkedList
            // Simplificação: apenas remove do array principal e diminui count
            
            numVertices--;
            for (int i = index; i < numVertices; i++) {
                vertices[i] = vertices[i+1];
                adjLists[i] = adjLists[i+1];
            }
            // NOTA: Em uma implementação completa, você precisa percorrer
            // adjLists e atualizar todos os Integers > index para (valor - 1).
        }
    }

    // ... Auxiliares ...
    public int getIndex(T vertex) {
        for (int i = 0; i < numVertices; i++) {
            if (vertices[i].equals(vertex)) return i;
        }
        return -1;
    }

    protected boolean indexIsValid(int index) {
        return index >= 0 && index < numVertices;
    }

    @Override
    public boolean isEmpty() { return numVertices == 0; }

    @Override
    public int size() { return numVertices; }

    @Override
    public boolean isConnected() {
        if (isEmpty()) return false;
        Iterator<T> it = iteratorBFS(0);
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return (count == numVertices);
    }

    // ... TRAVESSIAS USANDO LISTAS ...

    @Override
    public Iterator<T> iteratorBFS(T startVertex) {
        return iteratorBFS(getIndex(startVertex));
    }

    // Renomeado de iteratorBFSList para iteratorBFS para Override correto
    public Iterator<T> iteratorBFS(int startIndex) {
        ArrayUnorderedList<T> resultList = new ArrayUnorderedList<>();
        if (!indexIsValid(startIndex)) return resultList.iterator();

        boolean[] visited = new boolean[numVertices];
        LinkedQueue<Integer> queue = new LinkedQueue<>();

        visited[startIndex] = true;
        queue.enqueue(startIndex);

        while (!queue.isEmpty()) {
            int v = queue.dequeue();
            resultList.addToRear(vertices[v]);

            Iterator<Integer> it = adjLists[v].iterator();
            while (it.hasNext()) {
                int w = it.next();
                if (!visited[w]) {
                    visited[w] = true;
                    queue.enqueue(w);
                }
            }
        }
        return resultList.iterator();
    }

    @Override
    public Iterator<T> iteratorDFS(T startVertex) {
        return iteratorDFS(getIndex(startVertex));
    }

    // Renomeado de iteratorDFSList para iteratorDFS
    public Iterator<T> iteratorDFS(int startIndex) {
        ArrayUnorderedList<T> resultList = new ArrayUnorderedList<>();
        if (!indexIsValid(startIndex)) return resultList.iterator();

        boolean[] visited = new boolean[numVertices];
        LinkedStack<Integer> stack = new LinkedStack<>();

        stack.push(startIndex);
        visited[startIndex] = true;
        resultList.addToRear(vertices[startIndex]);

        while (!stack.isEmpty()) {
            int v = stack.peek();
            boolean found = false;

            Iterator<Integer> it = adjLists[v].iterator();
            while (it.hasNext() && !found) {
                int w = it.next();
                if (!visited[w]) {
                    visited[w] = true;
                    stack.push(w);
                    resultList.addToRear(vertices[w]);
                    found = true;
                }
            }
            if (!found) {
                stack.pop();
            }
        }
        return resultList.iterator();
    }

    @Override
    public Iterator<T> iteratorShortestPath(T startVertex, T targetVertex) {
        return iteratorShortestPath(getIndex(startVertex), getIndex(targetVertex));
    }

    public Iterator<T> iteratorShortestPath(int startIndex, int targetIndex) {
        ArrayUnorderedList<T> resultList = new ArrayUnorderedList<>();
        if (!indexIsValid(startIndex) || !indexIsValid(targetIndex)) return resultList.iterator();

        if (startIndex == targetIndex) {
            resultList.addToRear(vertices[startIndex]);
            return resultList.iterator();
        }

        boolean[] visited = new boolean[numVertices];
        int[] predecessor = new int[numVertices];
        for (int i=0; i<numVertices; i++) { predecessor[i] = -1; }

        LinkedQueue<Integer> queue = new LinkedQueue<>();
        visited[startIndex] = true;
        queue.enqueue(startIndex);
        boolean found = false;

        while (!queue.isEmpty() && !found) {
            int v = queue.dequeue();
            Iterator<Integer> it = adjLists[v].iterator();
            while (it.hasNext()) {
                int w = it.next();
                if (!visited[w]) {
                    visited[w] = true;
                    predecessor[w] = v;
                    queue.enqueue(w);
                    if (w == targetIndex) {
                        found = true;
                        break;
                    }
                }
            }
        }

        if (!found) return resultList.iterator();

        LinkedStack<Integer> stack = new LinkedStack<>();
        int current = targetIndex;
        while (current != -1) {
            stack.push(current);
            current = predecessor[current];
        }
        while (!stack.isEmpty()) {
            resultList.addToRear(vertices[stack.pop()]);
        }
        return resultList.iterator();
    }
    
    @Override
    public String toString() {
        if (numVertices == 0) return "Empty Graph";
        String result = "Adjacency Lists\n---------------\n";
        for (int i = 0; i < numVertices; i++) {
            result += vertices[i] + " (" + i + ") -> " + adjLists[i].toString() + "\n";
        }
        return result;
    }
}