package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Lists.ArrayUnorderedList;
import enums.CorredorEvento;
import enums.TipoDivisao;

class LabyrinthGraphTest {

    private LabyrinthGraph<Divisao> grafo;
    private Divisao salaA, salaB, salaC;

    @BeforeEach
    void setUp() {
        grafo = new LabyrinthGraph<>();
        salaA = new Divisao("Sala A", TipoDivisao.ENTRADA);
        salaB = new Divisao("Sala B", TipoDivisao.SALA_NORMAL);
        salaC = new Divisao("Sala C", TipoDivisao.SALA_CENTRAL); // Ou SALA_CENTRAL dependendo do teu Enum

        grafo.addVertex(salaA);
        grafo.addVertex(salaB);
        grafo.addVertex(salaC);
    }

    @Test
    void testeAdicionarCorredor() {
        // 1. Criar um corredor normal entre A e B
        EventoCorredor evento = new EventoCorredor(CorredorEvento.NONE, 0);
        grafo.addCorridor(salaA, salaB, evento);

        // 2. Verificar se são vizinhos
        ArrayUnorderedList<Divisao> vizinhosA = grafo.getVizinhos(salaA);
        boolean encontrouB = false;
        for (Divisao d : vizinhosA) {
            if (d.equals(salaB)) encontrouB = true;
        }
        assertTrue(encontrouB, "A Sala B devia ser vizinha da Sala A");

        // 3. Verificar o evento do corredor
        EventoCorredor evRecuperado = grafo.getCorredorEvento(salaA, salaB);
        assertEquals(CorredorEvento.NONE, evRecuperado.getTipo(), "O corredor devia ser NONE");
    }

    @Test
    void testeRelocalizarArmadilha() {
        // 1. Ligar A-B (com armadilha) e B-C (livre)
        EventoCorredor armadilha = new EventoCorredor(CorredorEvento.MOVE_BACK, 2);
        grafo.addCorridor(salaA, salaB, armadilha);
        
        EventoCorredor livre = new EventoCorredor(CorredorEvento.NONE, 0);
        grafo.addCorridor(salaB, salaC, livre);

        // 2. Verificar estado inicial
        assertEquals(CorredorEvento.MOVE_BACK, grafo.getCorredorEvento(salaA, salaB).getTipo());
        assertEquals(CorredorEvento.NONE, grafo.getCorredorEvento(salaB, salaC).getTipo());

        // 3. Executar a relocalização (simular que o jogador caiu nela)
        System.out.println("--- A testar relocalização ---");
        grafo.relocalizarArmadilha(salaA, salaB);

        // 4. Verificar se saiu de A-B
        EventoCorredor novoEventoAB = grafo.getCorredorEvento(salaA, salaB);
        assertEquals(CorredorEvento.NONE, novoEventoAB.getTipo(), "A armadilha devia ter desaparecido de A-B");

        // 5. Verificar se foi para B-C (único outro sítio possível neste grafo pequeno)
        EventoCorredor novoEventoBC = grafo.getCorredorEvento(salaB, salaC);
        assertEquals(CorredorEvento.MOVE_BACK, novoEventoBC.getTipo(), "A armadilha devia ter ido para B-C");
    }
}