package io;

import game.LabyrinthGraph;
import game.Divisao;
import enums.TipoDivisao;
import enums.CorredorEvent;
import Lists.ArrayUnorderedList;

public class MapGenerator {

    /**
     * Gera um mapa onde os corredores são compostos por vários "passos".
     * @param dificuldadeTamanho 1 (Curto), 2 (Médio), 3 (Longo)
     */
    public LabyrinthGraph<Divisao> gerarMapaAleatorio(int dificuldadeTamanho) {
        LabyrinthGraph<Divisao> grafo = new LabyrinthGraph<>();
        ArrayUnorderedList<Divisao> todasSalas = new ArrayUnorderedList<>();

        System.out.println("🔨 A construir labirinto com corredores longos...");

        // Ajustar tamanho baseado na escolha (minimo de passos até ao tesouro)
        int distanciaMinima = 10; 
        if (dificuldadeTamanho == 2) distanciaMinima = 15;
        if (dificuldadeTamanho >= 3) distanciaMinima = 25;

        // 1. CRIAR PONTOS CHAVE
        Divisao entrada = new Divisao("Entrada Principal", TipoDivisao.ENTRADA);
        Divisao tesouro = new Divisao("Câmara do Tesouro", TipoDivisao.SALA_CENTRAL);
        
        grafo.addVertex(entrada); todasSalas.addToRear(entrada);
        grafo.addVertex(tesouro); todasSalas.addToRear(tesouro);

        // 2. CONSTRUIR O CAMINHO PRINCIPAL (A "Espinha Dorsal")
        // Vamos criar uma linha de salas "SALA_NORMAL" para simular a distância.
        
        Divisao anterior = entrada;
        
        for (int i = 1; i < distanciaMinima; i++) {
            // Cria um segmento de corredor
            Divisao segmento = new Divisao("Corredor Principal " + i, TipoDivisao.SALA_NORMAL);
            
            // Adicionar ao grafo
            grafo.addVertex(segmento);
            todasSalas.addToRear(segmento);

            // Ligar ao anterior (Sem eventos, apenas andar)
            grafo.addCorridor(anterior, segmento, new CorredorEvent(CorredorEvent.Type.NONE, 0));
            
            // Atualizar referência
            anterior = segmento;

            // 3. CRIAR RAMIFICAÇÕES (Salas interessantes nas laterais)
            // A cada 3 ou 4 passos, criamos uma sala especial ligada ao corredor
            if (i % 3 == 0) {
                criarRamificacao(grafo, segmento, i);
            }
        }

        // Ligar o último segmento ao Tesouro (Final do jogo)
        grafo.addCorridor(anterior, tesouro, new CorredorEvent(CorredorEvent.Type.NONE, 0));

        System.out.println("✅ Mapa gerado! Distância mínima: " + distanciaMinima + " passos.");
        return grafo;
    }

    /**
     * Cria uma sala especial (Enigma ou Alavanca) ligada a um corredor.
     */
    private void criarRamificacao(LabyrinthGraph<Divisao> grafo, Divisao corredorOrigem, int id) {
        // Decidir o que é esta sala lateral
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
            // Beco escuro pode ter armadilha
            eventoEntrada = new CorredorEvent(CorredorEvent.Type.MOVE_BACK, 2);
        }

        Divisao salaExtra = new Divisao(nome, tipo);



        // Se for alavanca, definir ID
        if (tipo == TipoDivisao.SALA_ALAVANCA) {
            salaExtra.setIdDesbloqueio((int)(Math.random() * 5) + 1);
        }

        grafo.addVertex(salaExtra);
        
        // Ligar corredor -> sala extra
        grafo.addCorridor(corredorOrigem, salaExtra, eventoEntrada);
    }
}