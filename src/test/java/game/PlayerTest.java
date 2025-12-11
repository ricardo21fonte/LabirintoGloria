package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import enums.TipoDivisao;
import enums.TipoDivisao; // Adicionar este import
import ui.GameView;
class PlayerTest {

    @Test
    void testeBloqueioDeTurnos() {
        // 1. Preparar (Setup)
        Divisao inicio = new Divisao("Inicio", TipoDivisao.ENTRADA);
        Player p = new Player("Teste", inicio);

        // 2. Executar (Action)
        p.bloquear(2); // Bloqueia por 2 turnos

        // 3. Verificar (Assert)
        assertTrue(p.isBloqueado(), "O jogador devia estar bloqueado");
        assertEquals(2, p.getTurnosBloqueado(), "Devia ter 2 turnos de bloqueio");

        // Consumir 1 turno
        p.consumirUmTurnoBloqueado();
        assertTrue(p.isBloqueado(), "Ainda devia estar bloqueado (sobra 1)");
        
        // Consumir o último turno
        p.consumirUmTurnoBloqueado();
        assertFalse(p.isBloqueado(), "Já não devia estar bloqueado");
    }

    @Test
    void testeMovimentoERecuo() {
        Divisao inicio = new Divisao("Inicio", TipoDivisao.ENTRADA);
        Divisao corredor = new Divisao("Corredor", TipoDivisao.SALA_NORMAL);
        GameView view = new GameView();
        Player p = new Player("Corredor", inicio);
        
        // Mover para a frente
        p.moverPara(corredor);
        assertEquals(corredor, p.getLocalAtual(), "O jogador devia estar no Corredor");
        
        // Testar Recuo
        p.recuar(1, view);
        assertEquals(inicio, p.getLocalAtual(), "O jogador devia ter voltado ao Inicio");
    }
}