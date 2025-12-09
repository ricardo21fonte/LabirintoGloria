package app;

import game.Divisao;
import game.LabyrinthGraph;

public class Main {

    public static void main(String[] args) {
        Menu menuDoJogo = new Menu();
        LabyrinthGraph<Divisao> labirintoGraph = menuDoJogo.apresentarMenuPrincipal();

        if (labirintoGraph == null || labirintoGraph.size() == 0) {
            System.out.println("❌ Jogo cancelado ou mapa inválido.");
            return;
        }

        // --- AQUI ESTÁ O QUE FALTA NO TEU MAIN ---
        // 1. Ir buscar o nome ao Menu
        String nomeMapa = menuDoJogo.getNomeMapaAtual();

        GameEngine engine = new GameEngine(labirintoGraph);
        
        // 2. Entregar o nome ao Engine
        engine.setNomeDoMapa(nomeMapa);
        
        engine.start();
    }
}