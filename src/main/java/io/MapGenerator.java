package io;

import java.util.Iterator;

import Lists.ArrayUnorderedList;
import Queue.LinkedQueue;
import enums.CorredorEvento;
import enums.TipoDivisao;
import game.Divisao;
import game.EventoCorredor;
import graph.LabyrinthGraph;

/**
 * Class responsible for generating the labyrinth maps
 */
public class MapGenerator {

    /**
     * Generates a random map based on a difficulty level.
     * @param dificuldade difficulty level
     * @return a generated LabyrinthGraph with rooms and events
     */
    public LabyrinthGraph<Divisao> gerarMapaAleatorio(int dificuldade) {
        int trancas = 0;
        int enigmas = (dificuldade * 3) + 2;
        int normais = 15 + (dificuldade * 5);
        int entradas = 4;

        switch (dificuldade) {
            case 1: trancas = (int)(Math.random() * 2) + 1; break; // 1 a 2
            case 2: trancas = (int)(Math.random() * 2) + 3; break; // 3 a 4
            case 3: trancas = (int)(Math.random() * 2) + 5; break; // 5 a 6
            default: trancas = 1;
        }

        return gerarMapaTotalmenteCustomizado(entradas, enigmas, normais, trancas);
    }
    /**
     * Generates a labyrinth map costumized
     * @param numEntradas number of entrance rooms to create
     * @param numEnigmas  number of puzzle rooms to create
     * @param numNormais  number of normal rooms to create
     * @param numTrancas  number of locks to place in the map
     * @return a generated LabyrinthGraph
     */
    public LabyrinthGraph<Divisao> gerarMapaTotalmenteCustomizado(int numEntradas, int numEnigmas, int numNormais, int numTrancas) {
        System.out.println("A gerar Grafo Orgânico com " + numTrancas + " trancas aleatórias...");

        LabyrinthGraph<Divisao> grafo = new LabyrinthGraph<>();
        ArrayUnorderedList<Divisao[]> candidatosParaTrancas = new ArrayUnorderedList<>();

        Divisao tesouro = new Divisao("Câmara do Tesouro", TipoDivisao.SALA_CENTRAL);
        grafo.addVertex(tesouro);

        ArrayUnorderedList<Divisao> salasConectadas = new ArrayUnorderedList<>();
        int qtdGuardioes = (numTrancas > 0) ? numTrancas : 1;

        for (int i = 1; i <= qtdGuardioes; i++) {
            Divisao guardiao = new Divisao("Ante-Câmara " + i, TipoDivisao.SALA_NORMAL);
            grafo.addVertex(guardiao);
            salasConectadas.addToRear(guardiao);

            EventoCorredor evento = sortearArmadilhaOuNada();
            grafo.addCorredor(guardiao, tesouro, evento);
            candidatosParaTrancas.addToRear(new Divisao[]{guardiao, tesouro});
        }

        ArrayUnorderedList<Divisao> sacoDeSalas = new ArrayUnorderedList<>();
        for (int i = 1; i <= numTrancas; i++) {
            Divisao alavanca = new Divisao("Sala de Controlo #" + i, TipoDivisao.SALA_ALAVANCA);
            alavanca.setIdDesbloqueio(i);
            grafo.addVertex(alavanca);
            sacoDeSalas.addToRear(alavanca);
        }
        for (int i = 1; i <= numEnigmas; i++) {
            Divisao enigma = new Divisao("Sala Misteriosa " + i, TipoDivisao.SALA_ENIGMA);
            grafo.addVertex(enigma);
            sacoDeSalas.addToRear(enigma);
        }
        if (numNormais < numEntradas) numNormais = numEntradas + 2;
        for (int i = 1; i <= numNormais; i++) {
            Divisao normal = new Divisao("Corredor " + i, TipoDivisao.SALA_NORMAL);
            grafo.addVertex(normal);
            sacoDeSalas.addToRear(normal);
        }
        sacoDeSalas = baralharLista(sacoDeSalas);

        LinkedQueue<Divisao> fronteira = new LinkedQueue<>();
        Iterator<Divisao> itGuardioes = salasConectadas.iterator();
        while(itGuardioes.hasNext()) fronteira.enqueue(itGuardioes.next());

        Iterator<Divisao> itSaco = sacoDeSalas.iterator();
        while (itSaco.hasNext()) {
            Divisao novaSala = itSaco.next();
            Divisao pai;
            try { pai = fronteira.dequeue(); } catch (Exception e) { pai = tesouro; }

            grafo.addCorredor(pai, novaSala, sortearArmadilhaOuNada());
            candidatosParaTrancas.addToRear(new Divisao[]{pai, novaSala});

            fronteira.enqueue(novaSala);
            if (Math.random() > 0.3) fronteira.enqueue(pai);
        }

        // Gera os spawns
        for (int i = 1; i <= numEntradas; i++) {
            Divisao spawn = new Divisao("Portão " + i, TipoDivisao.ENTRADA);
            grafo.addVertex(spawn);
            Divisao ponta = obterSalaFallback(fronteira, salasConectadas);

            grafo.addCorredor(spawn, ponta, sortearArmadilhaOuNada());
        }
        criarCiclosAleatorios(grafo, sacoDeSalas, candidatosParaTrancas);
        aplicarTrancasAleatorias(grafo, candidatosParaTrancas, numTrancas);

        System.out.println("Mapa gerado com sucesso!");
        return grafo;
    }
    /**
     * Randomly assigns locked events
     * @param grafo      the labyrinth graph to modify
     * @param candidatos list of candidate pairs
     * @param qtd        number of locks to place
     */
    private void aplicarTrancasAleatorias(LabyrinthGraph<Divisao> grafo, ArrayUnorderedList<Divisao[]> candidatos, int qtd) {
        Divisao[][] arrCandidatos = toArrayPares(candidatos);
        for (int i = arrCandidatos.length - 1; i > 0; i--) {
            int index = (int)(Math.random() * (i + 1));
            Divisao[] temp = arrCandidatos[index];
            arrCandidatos[index] = arrCandidatos[i];
            arrCandidatos[i] = temp;
        }

        int aplicadas = 0;
        for (int i = 0; i < arrCandidatos.length && aplicadas < qtd; i++) {
            Divisao origem = arrCandidatos[i][0];
            Divisao destino = arrCandidatos[i][1];

            EventoCorredor evAtual = grafo.getCorredorEvento(origem, destino);
            
            if (evAtual.getTipo() == CorredorEvento.NONE) {
                int idChave = aplicadas + 1;
                EventoCorredor tranca = new EventoCorredor(CorredorEvento.LOCKED, idChave);
                grafo.setCorredorEvento(origem, destino, tranca);
                
                System.out.println("   Tranca #" + idChave + " colocada entre [" + origem.getNome() + "] e [" + destino.getNome() + "]");
                aplicadas++;
            }
        }
        
        if (aplicadas < qtd) {
            System.out.println("Aviso: Só foi possível colocar " + aplicadas + " de " + qtd + " trancas (mapa muito cheio de armadilhas).");
        }
    }
    /**
     * Creates shortcuts between random rooms
     * @param grafo      the labyrinth graph to modify
     * @param todasSalas list of all non-entrance rooms created
     * @param candidatos list of candidate corredores to later receive locks
     */
    private void criarCiclosAleatorios(LabyrinthGraph<Divisao> grafo, ArrayUnorderedList<Divisao> todasSalas, ArrayUnorderedList<Divisao[]> candidatos) {
        Divisao[] arr = toArray(todasSalas);
        int tentativas = arr.length / 2; 

        for (int k = 0; k < tentativas; k++) {
            int i1 = (int)(Math.random() * arr.length);
            int i2 = (int)(Math.random() * arr.length);

            if (i1 != i2) {
                grafo.addCorredor(arr[i1], arr[i2], sortearArmadilhaOuNada());
                candidatos.addToRear(new Divisao[]{arr[i1], arr[i2]});
            }
        }
    }
    /**
     * Randomly selects either a trap event or a safe corridor.
     * @return a randomly chosen EventoCorredor
     */
    private EventoCorredor sortearArmadilhaOuNada() {
        double r = Math.random();
        if (r > 0.85) return new EventoCorredor(CorredorEvento.MOVE_BACK, 2);
        if (r > 0.80) return new EventoCorredor(CorredorEvento.BLOCK_TURN, 1);
        return new EventoCorredor(CorredorEvento.NONE, 0);
    }
    /**
     * Shuffles a list of rooms and returns a new ArrayUnorderedList
     * @param listaOriginal original ordered list
     * @return a new shuffled list
     */
    private ArrayUnorderedList<Divisao> baralharLista(ArrayUnorderedList<Divisao> listaOriginal) {
        Divisao[] temp = toArray(listaOriginal);
        for (int i = temp.length - 1; i > 0; i--) {
            int index = (int)(Math.random() * (i + 1));
            Divisao a = temp[index];
            temp[index] = temp[i];
            temp[i] = a;
        }
        ArrayUnorderedList<Divisao> novaLista = new ArrayUnorderedList<>();
        for(Divisao d : temp) novaLista.addToRear(d);
        return novaLista;
    }
    /**
     * Converts a ArrayUnorderedList of an Divisao into a fixed-size array.
     * @param lista list of rooms
     * @return array with the same elements and size
     */
    private Divisao[] toArray(ArrayUnorderedList<Divisao> lista) {
        Divisao[] arr = new Divisao[lista.size()];
        Iterator<Divisao> it = lista.iterator();
        int i = 0;
        while(it.hasNext()) arr[i++] = it.next();
        return arr;
    }

    /**
     * Converts a ArrayUnorderedList of pairs an Divisao
     * @param lista list of room pairs
     * @return array with the same pairs in the same order
     */
    private Divisao[][] toArrayPares(ArrayUnorderedList<Divisao[]> lista) {
        Divisao[][] arr = new Divisao[lista.size()][2];
        Iterator<Divisao[]> it = lista.iterator();
        int i = 0;
        while(it.hasNext()) arr[i++] = it.next();
        return arr;
    }
    /**
     * Helper method to obtain a room from the frontier queue if possible;
     * @param fronteira    queue of frontier rooms
     * @param fallbackList list used as a fallback source of rooms
     * @return a room to be used as connection endpoint
     */
    private Divisao obterSalaFallback(LinkedQueue<Divisao> fronteira, ArrayUnorderedList<Divisao> fallbackList) {
        try { return fronteira.dequeue(); } 
        catch (Exception e) { 
            Iterator<Divisao> it = fallbackList.iterator();
            if(it.hasNext()) return it.next();
            return null;
        }
    }
}