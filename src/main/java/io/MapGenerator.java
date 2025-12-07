package io;

import game.LabyrinthGraph;
import game.Divisao;
import enums.TipoDivisao;
import enums.CorredorEvent;
import Lists.ArrayUnorderedList; // Mantém o teu import original

public class MapGenerator {

    /**
     * MODO PADRÃO (1, 2, 3): Gera um mapa "Orgânico" com corredores e ramificações.
     * Ideal para jogabilidade clássica.
     */
    public LabyrinthGraph<Divisao> gerarMapaAleatorio(int dificuldadeTamanho) {
        LabyrinthGraph<Divisao> grafo = new LabyrinthGraph<>();
        ArrayUnorderedList<Divisao> todasSalas = new ArrayUnorderedList<>();

        System.out.println("🔨 A construir labirinto (Modo Corredores)...");

        int distanciaMinima = 10; 
        if (dificuldadeTamanho == 2) distanciaMinima = 15;
        if (dificuldadeTamanho >= 3) distanciaMinima = 25;

        // 1. Pontos Chave
        Divisao entrada = new Divisao("Entrada Principal", TipoDivisao.ENTRADA);
        Divisao tesouro = new Divisao("Câmara do Tesouro", TipoDivisao.SALA_CENTRAL);
        
        grafo.addVertex(entrada); todasSalas.addToRear(entrada);
        grafo.addVertex(tesouro); todasSalas.addToRear(tesouro);

        // 2. Caminho Principal
        Divisao anterior = entrada;
        
        for (int i = 1; i < distanciaMinima; i++) {
            Divisao segmento = new Divisao("Corredor Principal " + i, TipoDivisao.SALA_NORMAL);
            grafo.addVertex(segmento);
            todasSalas.addToRear(segmento);

            grafo.addCorridor(anterior, segmento, new CorredorEvent(CorredorEvent.Type.NONE, 0));
            anterior = segmento;

            // Ramificações (Salas laterais)
            if (i % 3 == 0) {
                criarRamificacao(grafo, segmento, i);
            }
        }

        grafo.addCorridor(anterior, tesouro, new CorredorEvent(CorredorEvent.Type.NONE, 0));
        System.out.println("✅ Mapa gerado com sucesso.");
        return grafo;
    }

    /**
     * MODO AVANÇADO: Cria uma "TEIA DE ARANHA" com 8 ENTRADAS.
     * @param numSalas Total de salas (será ajustado para caber na teia).
     * @param densidade Quantidade de ligações laterais (1=Pouca, 3=Muita).
     */
    public LabyrinthGraph<Divisao> gerarMapaPersonalizado(int numSalas, int densidade) {
        // Mínimo de 20 salas para suportar a estrutura de 8 pontas
        if (numSalas < 20) numSalas = 20;

        System.out.println("🕸️ A gerar mapa em 'Teia' com 8 Entradas...");
        
        LabyrinthGraph<Divisao> grafo = new LabyrinthGraph<>();
        
        // 1. O CENTRO (Tesouro)
        Divisao tesouro = new Divisao("Câmara do Tesouro", TipoDivisao.SALA_CENTRAL);
        grafo.addVertex(tesouro);

        int numRaios = 8; // 8 Caminhos para 8 Jogadores
        // Calcula quantas salas tem cada caminho (profundidade)
        int profundidade = (numSalas - 1) / numRaios; 
        if (profundidade < 1) profundidade = 1;

        // Matriz auxiliar para ligar salas lateralmente [Raio][Nivel]
        Divisao[][] grelha = new Divisao[numRaios][profundidade];

        // 2. CONSTRUIR OS RAIOS (Do Centro para Fora)
        for (int r = 0; r < numRaios; r++) {
            Divisao anterior = tesouro;

            for (int p = 0; p < profundidade; p++) {
                // A última sala do raio é sempre a ENTRADA
                boolean isPonta = (p == profundidade - 1);
                
                TipoDivisao tipo;
                String nome;

                if (isPonta) {
                    tipo = TipoDivisao.ENTRADA;
                    nome = "Portão " + (r + 1); // Portão 1 a 8
                } else {
                    tipo = sortearTipo();
                    nome = "Setor " + (r + 1) + "-" + (p + 1);
                }

                Divisao sala = new Divisao(nome, tipo);
                
                // Configurar Alavanca se calhar
                if (tipo == TipoDivisao.SALA_ALAVANCA) {
                    sala.setIdDesbloqueio((int)(Math.random() * 5) + 1);
                }

                grafo.addVertex(sala);
                grelha[r][p] = sala; // Guardar na grelha

                // Ligar à anterior (em direção ao centro)
                CorredorEvent evento = new CorredorEvent(CorredorEvent.Type.NONE, 0);
                
                // Proteção extra perto do tesouro (camada 0)
                if (p == 0 && Math.random() > 0.8) {
                    evento = new CorredorEvent(CorredorEvent.Type.MOVE_BACK, 2);
                }

                grafo.addCorridor(sala, anterior, evento);
                anterior = sala;
            }
        }

        // 3. LIGAÇÕES LATERAIS (Onde a densidade importa)
        // Ligar o Raio 1 ao Raio 2, Raio 2 ao 3, etc.
        for (int p = 0; p < profundidade; p++) {
            for (int r = 0; r < numRaios; r++) {
                Divisao salaAtual = grelha[r][p];
                Divisao salaVizinha = grelha[(r + 1) % numRaios][p]; // Vizinho circular

                boolean criarLigacao = false;
                
                // Densidade decide a probabilidade de ligação lateral
                if (densidade == 1 && Math.random() > 0.7) criarLigacao = true; // Raro
                if (densidade == 2 && Math.random() > 0.4) criarLigacao = true; // Comum
                if (densidade >= 3) criarLigacao = true; // Quase sempre (Labirinto aberto)

                // Não ligar duas Entradas diretamente (opcional, para forçar entrada no labirinto)
                if (salaAtual.getTipo() == TipoDivisao.ENTRADA) criarLigacao = false;

                if (criarLigacao) {
                    grafo.addCorridor(salaAtual, salaVizinha, sortearEvento());
                }
            }
        }

        return grafo;
    }

    // --- Métodos Auxiliares ---

    private void criarRamificacao(LabyrinthGraph<Divisao> grafo, Divisao corredorOrigem, int id) {
        double sorte = Math.random();
        TipoDivisao tipo;
        String nome;
        CorredorEvent eventoEntrada = new CorredorEvent(CorredorEvent.Type.NONE, 0);

        if (sorte < 0.4) {
            tipo = TipoDivisao.SALA_ENIGMA;
            nome = "Sala Misteriosa " + id;
        } else if (sorte < 0.7) {
            tipo = TipoDivisao.SALA_ALAVANCA;
            nome = "Sala de Controlo " + id;
        } else {
            tipo = TipoDivisao.SALA_NORMAL;
            nome = "Beco Escuro " + id;
            eventoEntrada = new CorredorEvent(CorredorEvent.Type.MOVE_BACK, 2);
        }

        Divisao salaExtra = new Divisao(nome, tipo);
        if (tipo == TipoDivisao.SALA_ALAVANCA) {
            salaExtra.setIdDesbloqueio((int)(Math.random() * 5) + 1);
        }

        grafo.addVertex(salaExtra);
        grafo.addCorridor(corredorOrigem, salaExtra, eventoEntrada);
    }

    private TipoDivisao sortearTipo() {
        double r = Math.random();
        if (r < 0.6) return TipoDivisao.SALA_NORMAL;
        if (r < 0.85) return TipoDivisao.SALA_ENIGMA;
        return TipoDivisao.SALA_ALAVANCA;
    }

    private CorredorEvent sortearEvento() {
        double r = Math.random();
        if (r < 0.85) return new CorredorEvent(CorredorEvent.Type.NONE, 0);
        else return new CorredorEvent(CorredorEvent.Type.BLOCK_TURN, 0);
    }
}