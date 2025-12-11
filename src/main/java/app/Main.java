package app;

import game.Divisao;
import graph.LabyrinthGraph;
import engine.GameEngine;
import ui.GameView;
/**
 * Entry point of the application.
 */
public class Main {

    public static void main(String[] args) {
        Menu menuDoJogo = new Menu();
        LabyrinthGraph<Divisao> labirintoGraph = menuDoJogo.apresentarMenuPrincipal();

        if (labirintoGraph == null || labirintoGraph.size() == 0) {
            System.out.println("Jogo cancelado ou mapa inválido.");
            return;
        }

        String nomeMapa = menuDoJogo.getNomeMapaAtual();

        GameView view = new GameView(); 
        GameEngine engine = new GameEngine(labirintoGraph, view);

        engine.setNomeDoMapa(nomeMapa);
        
        engine.start();
    }
}