package app;

import game.Divisao;
import game.LabyrinthGraph;

/**
 * Classe principal do programa.
 * Responsável apenas por inicializar o menu e delegar a execução do jogo ao GameEngine.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Apresentar menu e obter o grafo do labirinto
        Menu menuDoJogo = new Menu();
        LabyrinthGraph<Divisao> labirintoGraph = menuDoJogo.apresentarMenuPrincipal();

        // 2. Validar se o mapa foi carregado
        if (labirintoGraph == null || labirintoGraph.size() == 0) {
            System.out.println("Jogo cancelado ou mapa inválido.");
            return;
        }

        // 3. Delegar a execução do jogo para o GameEngine
        GameEngine engine = new GameEngine(labirintoGraph);
        engine.start();
    }

}