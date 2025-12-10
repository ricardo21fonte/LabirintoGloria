package app;

import game.Divisao;
import graph.LabyrinthGraph;
import engine.GameEngine; // <--- NOVO: Importar do pacote engine
import ui.GameView;       // <--- NOVO: Importar do pacote ui

public class Main {

    public static void main(String[] args) {
        Menu menuDoJogo = new Menu();
        LabyrinthGraph<Divisao> labirintoGraph = menuDoJogo.apresentarMenuPrincipal();

        if (labirintoGraph == null || labirintoGraph.size() == 0) {
            System.out.println("❌ Jogo cancelado ou mapa inválido.");
            return;
        }

        // 1. Ir buscar o nome ao Menu
        String nomeMapa = menuDoJogo.getNomeMapaAtual();

        // --- CORREÇÃO AQUI ---
        // Agora tens de criar a View e passar para o Engine
        GameView view = new GameView(); 
        GameEngine engine = new GameEngine(labirintoGraph, view);
        
        // Se o método setNomeDoMapa der erro, vê a nota abaixo*
        engine.setNomeDoMapa(nomeMapa);
        
        engine.start();
    }
}