package io;

import java.util.Iterator;

import Lists.ArrayUnorderedList;
import Queue.LinkedQueue;
import enums.CorredorEvento;
import enums.TipoDivisao;
import game.Divisao;
import game.EventoCorredor;
import graph.LabyrinthGraph;

public class MapGenerator {

    // =========================================================================
    // 1. MODO AUTOMÁTICO (Mantém os intervalos de dificuldade)
    // =========================================================================
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

    // =========================================================================
    // 2. MODO CUSTOMIZADO
    // =========================================================================
    public LabyrinthGraph<Divisao> gerarMapaTotalmenteCustomizado(int numEntradas, int numEnigmas, int numNormais, int numTrancas) {
        System.out.println("🏗️ A gerar Grafo Orgânico com " + numTrancas + " trancas aleatórias...");

        LabyrinthGraph<Divisao> grafo = new LabyrinthGraph<>();
        
        // Lista para guardar TODOS os corredores onde podemos pôr trancas
        ArrayUnorderedList<Divisao[]> candidatosParaTrancas = new ArrayUnorderedList<>();

        // --- A. O CENTRO ---
        Divisao tesouro = new Divisao("Câmara do Tesouro", TipoDivisao.SALA_CENTRAL);
        grafo.addVertex(tesouro);

        // --- B. OS GUARDIÕES ---
        ArrayUnorderedList<Divisao> salasConectadas = new ArrayUnorderedList<>();
        int qtdGuardioes = (numTrancas > 0) ? numTrancas : 1;

        for (int i = 1; i <= qtdGuardioes; i++) {
            Divisao guardiao = new Divisao("Ante-Câmara " + i, TipoDivisao.SALA_NORMAL);
            grafo.addVertex(guardiao);
            salasConectadas.addToRear(guardiao);

            // Cria corredor normal (pode ter armadilha, mas NÃO tranca ainda)
            EventoCorredor evento = sortearArmadilhaOuNada();
            grafo.addCorridor(guardiao, tesouro, evento);
            
            // Adiciona este corredor à lista de candidatos (pode vir a ter tranca no sorteio final)
            candidatosParaTrancas.addToRear(new Divisao[]{guardiao, tesouro});
        }

        // --- C. CONTEÚDO (Saco de Salas) ---
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

        // --- D. CRESCIMENTO ORGÂNICO ---
        LinkedQueue<Divisao> fronteira = new LinkedQueue<>();
        Iterator<Divisao> itGuardioes = salasConectadas.iterator();
        while(itGuardioes.hasNext()) fronteira.enqueue(itGuardioes.next());

        Iterator<Divisao> itSaco = sacoDeSalas.iterator();
        while (itSaco.hasNext()) {
            Divisao novaSala = itSaco.next();
            Divisao pai;
            try { pai = fronteira.dequeue(); } catch (Exception e) { pai = tesouro; }

            // Ligar
            grafo.addCorridor(pai, novaSala, sortearArmadilhaOuNada());
            
            // Adicionar à lista de candidatos para trancas
            candidatosParaTrancas.addToRear(new Divisao[]{pai, novaSala});

            fronteira.enqueue(novaSala);
            if (Math.random() > 0.3) fronteira.enqueue(pai);
        }

        // --- E. SPAWNS (Entradas) ---
        for (int i = 1; i <= numEntradas; i++) {
            Divisao spawn = new Divisao("Portão " + i, TipoDivisao.ENTRADA);
            grafo.addVertex(spawn);
            Divisao ponta = obterSalaFallback(fronteira, salasConectadas);

            // Spawns NÃO entram na lista 'candidatosParaTrancas' 
            // (para não trancar o jogador logo no início sem ele poder mexer-se)
            grafo.addCorridor(spawn, ponta, sortearArmadilhaOuNada());
        }

        // --- F. CICLOS (Atalhos) ---
        criarCiclosAleatorios(grafo, sacoDeSalas, candidatosParaTrancas);

        // =====================================================================
        // 🚀 PASSO FINAL: DISTRIBUIR TRANCAS GARANTIDAS
        // =====================================================================
        aplicarTrancasAleatorias(grafo, candidatosParaTrancas, numTrancas);

        System.out.println("✅ Mapa gerado com sucesso!");
        return grafo;
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    private void aplicarTrancasAleatorias(LabyrinthGraph<Divisao> grafo, ArrayUnorderedList<Divisao[]> candidatos, int qtd) {
        // 1. Converter lista de candidatos para array para poder baralhar
        Divisao[][] arrCandidatos = toArrayPares(candidatos);
        
        // 2. Baralhar os corredores (Shuffle)
        for (int i = arrCandidatos.length - 1; i > 0; i--) {
            int index = (int)(Math.random() * (i + 1));
            Divisao[] temp = arrCandidatos[index];
            arrCandidatos[index] = arrCandidatos[i];
            arrCandidatos[i] = temp;
        }

        // 3. Aplicar trancas nos primeiros 'qtd' corredores
        int aplicadas = 0;
        for (int i = 0; i < arrCandidatos.length && aplicadas < qtd; i++) {
            Divisao origem = arrCandidatos[i][0];
            Divisao destino = arrCandidatos[i][1];

            // Só substitui se o evento atual for NONE (não queremos apagar armadilhas fixes)
            EventoCorredor evAtual = grafo.getCorredorEvento(origem, destino);
            
            if (evAtual.getTipo() == CorredorEvento.NONE) {
                int idChave = aplicadas + 1;
                EventoCorredor tranca = new EventoCorredor(CorredorEvento.LOCKED, idChave);
                grafo.setCorredorEvento(origem, destino, tranca);
                
                System.out.println("   🔒 Tranca #" + idChave + " colocada entre [" + origem.getNome() + "] e [" + destino.getNome() + "]");
                aplicadas++;
            }
        }
        
        if (aplicadas < qtd) {
            System.out.println("⚠️ Aviso: Só foi possível colocar " + aplicadas + " de " + qtd + " trancas (mapa muito cheio de armadilhas).");
        }
    }

    private void criarCiclosAleatorios(LabyrinthGraph<Divisao> grafo, ArrayUnorderedList<Divisao> todasSalas, ArrayUnorderedList<Divisao[]> candidatos) {
        Divisao[] arr = toArray(todasSalas);
        int tentativas = arr.length / 2; 

        for (int k = 0; k < tentativas; k++) {
            int i1 = (int)(Math.random() * arr.length);
            int i2 = (int)(Math.random() * arr.length);

            if (i1 != i2) {
                grafo.addCorridor(arr[i1], arr[i2], sortearArmadilhaOuNada());
                // Ciclos também podem ter trancas!
                candidatos.addToRear(new Divisao[]{arr[i1], arr[i2]});
            }
        }
    }

    // Apenas sorteia armadilhas ou nada (NUNCA TRANCAS - isso é feito no fim)
    private EventoCorredor sortearArmadilhaOuNada() {
        double r = Math.random();
        if (r > 0.85) return new EventoCorredor(CorredorEvento.MOVE_BACK, 2);
        if (r > 0.80) return new EventoCorredor(CorredorEvento.BLOCK_TURN, 1);
        return new EventoCorredor(CorredorEvento.NONE, 0);
    }

    // --- Helpers de Listas e Arrays ---

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

    private Divisao[] toArray(ArrayUnorderedList<Divisao> lista) {
        Divisao[] arr = new Divisao[lista.size()];
        Iterator<Divisao> it = lista.iterator();
        int i = 0;
        while(it.hasNext()) arr[i++] = it.next();
        return arr;
    }

    // Helper específico para arrays de pares (Divisao[])
    private Divisao[][] toArrayPares(ArrayUnorderedList<Divisao[]> lista) {
        Divisao[][] arr = new Divisao[lista.size()][2];
        Iterator<Divisao[]> it = lista.iterator();
        int i = 0;
        while(it.hasNext()) arr[i++] = it.next();
        return arr;
    }
    
    private Divisao obterSalaFallback(LinkedQueue<Divisao> fronteira, ArrayUnorderedList<Divisao> fallbackList) {
        try { return fronteira.dequeue(); } 
        catch (Exception e) { 
            Iterator<Divisao> it = fallbackList.iterator();
            if(it.hasNext()) return it.next();
            return null;
        }
    }
}