package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import enums.CorredorEvento;
import enums.Dificuldade;
import enums.TipoDivisao;
import graph.LabyrinthGraph;

class GameSimulationTest {

    @Test
    void simulacaoBotChegaAoTesouro() {
        // 1. Criar Mundo: Inicio -> Corredor -> Tesouro
        LabyrinthGraph<Divisao> mapa = new LabyrinthGraph<>();
        Divisao inicio = new Divisao("Start", TipoDivisao.ENTRADA);
        Divisao meio = new Divisao("Corredor", TipoDivisao.SALA_NORMAL);
        Divisao fim = new Divisao("Fim", TipoDivisao.SALA_CENTRAL);

        mapa.addVertex(inicio);
        mapa.addVertex(meio);
        mapa.addVertex(fim);

        mapa.addCorridor(inicio, meio, new EventoCorredor(CorredorEvento.NONE, 0));
        mapa.addCorridor(meio, fim, new EventoCorredor(CorredorEvento.NONE, 0));

        Bot bot = new Bot("AutoBot", inicio, Dificuldade.DIFICIL, mapa);

        // 2. Loop de Jogo Simulado (Max 10 turnos para não ser infinito)
        boolean ganhou = false;
        int turnos = 0;

        System.out.println("--- INÍCIO DA SIMULAÇÃO ---");

        while (turnos < 10 && !ganhou) {
            turnos++;
            System.out.println("Turno " + turnos + ": Estou em " + bot.getLocalAtual().getNome());

            // Bot decide
            Divisao destino = bot.escolherMovimento();

            if (destino != null) {
                // Simula movimento (sem verificações complexas de engine, só teste lógico)
                bot.moverPara(destino);

                if (destino.getTipo() == TipoDivisao.SALA_CENTRAL) {
                    ganhou = true;
                    System.out.println("🎉 Bot chegou ao tesouro!");
                }
            } else {
                System.out.println("Bot não soube o que fazer.");
            }
        }

        // 3. Verificação
        assertTrue(ganhou, "O Bot devia ter chegado ao tesouro em menos de 10 turnos.");
        assertEquals(fim, bot.getLocalAtual(), "O Bot devia estar na sala final.");
    }
}
