package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import enums.CorredorEvento;
import enums.Dificuldade;
import enums.TipoDivisao;
import graph.LabyrinthGraph;

class BotLogicTest {

    @Test
    void testeBotEncontraCaminhoMaisCurto() {
        LabyrinthGraph<Divisao> mapa = new LabyrinthGraph<>();

        Divisao start = new Divisao("Inicio", TipoDivisao.ENTRADA);
        Divisao meio = new Divisao("Meio", TipoDivisao.SALA_NORMAL);
        Divisao fim = new Divisao("Tesouro", TipoDivisao.SALA_CENTRAL);
        Divisao longe1 = new Divisao("Longe1", TipoDivisao.SALA_NORMAL);

        mapa.addVertex(start);
        mapa.addVertex(meio);
        mapa.addVertex(fim);
        mapa.addVertex(longe1);

        mapa.addCorredor(start, meio, new EventoCorredor(CorredorEvento.NONE, 0));
        mapa.addCorredor(meio, fim, new EventoCorredor(CorredorEvento.NONE, 0));

        mapa.addCorredor(start, longe1, new EventoCorredor(CorredorEvento.NONE, 0));

        Bot bot = new Bot("Robo", start, Dificuldade.DIFICIL, mapa);

        Divisao escolha = bot.escolherMovimento();

        assertNotNull(escolha, "O bot deve encontrar um caminho");
        assertEquals(meio, escolha, "O Bot devia ter escolhido o caminho mais curto (Meio).");
    }

    @Test
    void testeBotEvitaTrancasSemChave() {
        // Cenário: A -> B (Trancado) -> Tesouro
        //          A -> C -> D -> Tesouro (Livre mas longo)
        LabyrinthGraph<Divisao> mapa = new LabyrinthGraph<>();
        Divisao start = new Divisao("Inicio", TipoDivisao.ENTRADA);
        Divisao trancada = new Divisao("Porta Trancada", TipoDivisao.SALA_NORMAL);
        Divisao livre = new Divisao("Caminho Livre", TipoDivisao.SALA_NORMAL);
        Divisao fim = new Divisao("Tesouro", TipoDivisao.SALA_CENTRAL);

        mapa.addVertex(start); mapa.addVertex(trancada); mapa.addVertex(livre); mapa.addVertex(fim);

        // Caminho Curto mas TRANCADO (ID 1)
        mapa.addCorredor(start, trancada, new EventoCorredor(CorredorEvento.LOCKED, 1));
        mapa.addCorredor(trancada, fim, new EventoCorredor(CorredorEvento.NONE, 0));

        // Caminho Longo mas LIVRE
        mapa.addCorredor(start, livre, new EventoCorredor(CorredorEvento.NONE, 0));
        mapa.addCorredor(livre, fim, new EventoCorredor(CorredorEvento.NONE, 0));

        Bot bot = new Bot("Robo", start, Dificuldade.DIFICIL, mapa);

        // O Bot NÃO deve escolher 'trancada' porque não tem a chave #1
        Divisao escolha = bot.escolherMovimento();
        assertEquals(livre, escolha, "O Bot devia evitar a porta trancada e ir pelo caminho livre.");
    }
}