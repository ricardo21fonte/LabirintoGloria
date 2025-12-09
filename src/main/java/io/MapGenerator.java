package io;

import java.util.Iterator;

import Lists.ArrayUnorderedList;
import Queue.LinkedQueue; // A tua classe
import enums.CorredorEvento;
import enums.TipoDivisao; // O teu Enum
import game.Divisao;
import game.EventoCorredor;
import game.LabyrinthGraph;

public class MapGenerator {


    private int proximaTrancaId;
    private int maxTrancas;
    // =========================================================================
    // 1. MODO AUTOMÁTICO
    // =========================================================================
    public LabyrinthGraph<Divisao> gerarMapaAleatorio(int dificuldade) {
        int trancas = (dificuldade == 3) ? 6 : (dificuldade == 2) ? 4 : 2;
        int enigmas = (dificuldade * 3) + 2;
        int normais = 15 + (dificuldade * 5);
        int entradas = 4;

        return gerarMapaTotalmenteCustomizado(entradas, enigmas, normais, trancas);
    }

    // =========================================================================
    // 2. MODO CUSTOMIZADO (CRESCIMENTO ORGÂNICO)
    // =========================================================================
    public LabyrinthGraph<Divisao> gerarMapaTotalmenteCustomizado(int numEntradas, int numEnigmas, int numNormais, int numTrancas) {
        System.out.println("🏗️ A gerar Grafo Orgânico (Labirinto Real)...");

        this.proximaTrancaId = 1;
        this.maxTrancas = numTrancas;

        LabyrinthGraph<Divisao> grafo = new LabyrinthGraph<>();
        
        // --- A. O CENTRO ---
        Divisao tesouro = new Divisao("Câmara do Tesouro", TipoDivisao.SALA_CENTRAL);
        grafo.addVertex(tesouro);

        // --- B. OS GUARDIÕES (sem trancas obrigatórias à volta do Tesouro) ---
        ArrayUnorderedList<Divisao> salasConectadas = new ArrayUnorderedList<>();

        // Podes continuar a usar numTrancas, ou então escolher um nº fixo de ante-câmaras.
        // Aqui mantenho a tua ideia original:
        int qtdGuardioes = (numTrancas > 0) ? numTrancas : 1;

        for (int i = 1; i <= qtdGuardioes; i++) {
            Divisao guardiao = new Divisao("Ante-Câmara " + i, TipoDivisao.SALA_NORMAL);
            grafo.addVertex(guardiao);
            salasConectadas.addToRear(guardiao);

            // AGORA: o evento desta ligação é sorteado como qualquer outro corredor
            EventoCorredor evento = sortearEvento();
            grafo.addCorridor(guardiao, tesouro, evento);
        }


        // --- C. O SACO DE SALAS ---
        ArrayUnorderedList<Divisao> sacoDeSalas = new ArrayUnorderedList<>();

        // 1. Alavancas
        for (int i = 1; i <= numTrancas; i++) {
            Divisao alavanca = new Divisao("Sala de Controlo #" + i, TipoDivisao.SALA_ALAVANCA);
            alavanca.setIdDesbloqueio(i);
            grafo.addVertex(alavanca);
            sacoDeSalas.addToRear(alavanca);
        }
        // 2. Enigmas
        for (int i = 1; i <= numEnigmas; i++) {
            Divisao enigma = new Divisao("Sala Misteriosa " + i, TipoDivisao.SALA_ENIGMA);
            grafo.addVertex(enigma);
            sacoDeSalas.addToRear(enigma);
        }
        // 3. Normais
        if (numNormais < numEntradas) numNormais = numEntradas + 2;
        for (int i = 1; i <= numNormais; i++) {
            Divisao normal = new Divisao("Corredor " + i, TipoDivisao.SALA_NORMAL);
            grafo.addVertex(normal);
            sacoDeSalas.addToRear(normal);
        }

        sacoDeSalas = baralharLista(sacoDeSalas);

        // --- D. ALGORITMO DE CRESCIMENTO ---
        LinkedQueue<Divisao> fronteira = new LinkedQueue<>();
        
        Iterator<Divisao> itGuardioes = salasConectadas.iterator();
        while(itGuardioes.hasNext()) fronteira.enqueue(itGuardioes.next());

        Iterator<Divisao> itSaco = sacoDeSalas.iterator();
        
        while (itSaco.hasNext()) {
            Divisao novaSala = itSaco.next();
            Divisao pai;
            try { pai = fronteira.dequeue(); } catch (Exception e) { pai = tesouro; }

            // Usa o método auxiliar com os teus nomes
            grafo.addCorridor(pai, novaSala, sortearEvento());

            fronteira.enqueue(novaSala);
            if (Math.random() > 0.3) {
                fronteira.enqueue(pai);
            }
        }

        // --- E. COLOCAR AS ENTRADAS ---
        for (int i = 1; i <= numEntradas; i++) {
            Divisao spawn = new Divisao("Portão " + i, TipoDivisao.ENTRADA);
            grafo.addVertex(spawn);

            Divisao pontaDoGrafo;
            try { pontaDoGrafo = fronteira.dequeue(); } 
            catch (Exception e) { pontaDoGrafo = guardioes(salasConectadas); }

            EventoCorredor evento = new EventoCorredor(CorredorEvento.NONE, 0);
            if (Math.random() > 0.9) evento = new EventoCorredor(CorredorEvento.BLOCK_TURN, 0);

            grafo.addCorridor(spawn, pontaDoGrafo, evento);
        }

        criarCiclosAleatorios(grafo, sacoDeSalas);

        System.out.println("✅ Mapa Orgânico gerado com sucesso!");
        return grafo;
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    private void criarCiclosAleatorios(LabyrinthGraph<Divisao> grafo, ArrayUnorderedList<Divisao> todasSalas) {
        Divisao[] arr = toArray(todasSalas);
        int tentativas = arr.length / 2; 

        for (int k = 0; k < tentativas; k++) {
            int i1 = (int)(Math.random() * arr.length);
            int i2 = (int)(Math.random() * arr.length);

            if (i1 != i2) {
                grafo.addCorridor(arr[i1], arr[i2], sortearEvento());
            }
        }
    }

    // Aqui usamos o TEU Enum (CorredorEvento)
    private EventoCorredor sortearEvento() {
        double r = Math.random();
        // 1) Chance de criar uma TRANCA nova (LOCKED) se ainda não gastámos todas
        if (proximaTrancaId <= maxTrancas && r > 0.92) {
            EventoCorredor e = new EventoCorredor(CorredorEvento.LOCKED, proximaTrancaId);
            System.out.println("   🔒 Tranca #" + proximaTrancaId + " criada algures no labirinto.");
            proximaTrancaId++;
            return e;
        }

        // 2) Outros eventos de corredor
        if (r > 0.85) {
            return new EventoCorredor(CorredorEvento.MOVE_BACK, 2);
        }
        if (r > 0.80) {
            return new EventoCorredor(CorredorEvento.BLOCK_TURN, 1);
        }

        // 3) Corredor normal
        return new EventoCorredor(CorredorEvento.NONE, 0);
    }

    private ArrayUnorderedList<Divisao> baralharLista(ArrayUnorderedList<Divisao> listaOriginal) {
        Divisao[] temp = toArray(listaOriginal);
        // Shuffle
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

    private Divisao[] toArray(ArrayUnorderedList<Divisao> lista) {
        Divisao[] arr = new Divisao[lista.size()];
        Iterator<Divisao> it = lista.iterator();
        int i = 0;
        while(it.hasNext()) arr[i++] = it.next();
        return arr;
    }
    
    private Divisao guardioes(ArrayUnorderedList<Divisao> lista) {
        Iterator<Divisao> it = lista.iterator();
        if(it.hasNext()) return it.next();
        return null;
    }
}