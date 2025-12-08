package app;

import java.util.Scanner;
import game.LabyrinthGraph;
import game.Divisao;
import io.MapGenerator;
import io.MapLoader; // Caso tenhas criado este, senão podes remover

public class Menu {
    private Scanner scanner;

    public Menu() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Mostra o menu principal e retorna o Mapa escolhido/gerado.
     */
    public LabyrinthGraph<Divisao> apresentarMenuPrincipal() {
        System.out.println("\n==========================================");
        System.out.println("      🏰 LABIRINTO DA GLÓRIA 🏰");
        System.out.println("==========================================");
        System.out.println("1. Mapas Originais (Campanha)");
        System.out.println("2. Mapas do Jogador (Guardados)");
        System.out.println("3. Gerar Mapa Aleatório");
        System.out.println("0. Sair");
        System.out.print("Escolha: ");

        int opcao = lerInteiro();

        switch (opcao) {
            case 1: return menuMapasOriginais();
            case 2: return menuMapasJogador();
            case 3: return menuGerarAleatorio();
            case 0: System.exit(0); return null;
            default: System.out.println("Opção inválida."); return apresentarMenuPrincipal();
        }
    }

    // --- OPÇÃO 1: ORIGINAIS ---
    private LabyrinthGraph<Divisao> menuMapasOriginais() {
        System.out.println("\n--- MAPAS ORIGINAIS ---");
        System.out.println("1. O Início (Fácil)");
        System.out.println("2. A Masmorra (Médio)");
        System.out.println("3. O Pesadelo (Difícil)");
        System.out.print("Escolha: ");

        String ficheiro = "";
        int op = lerInteiro();
        if (op == 1) ficheiro = "mapa_facil.json";
        else if (op == 2) ficheiro = "mapa_medio.json";
        else if (op == 3) ficheiro = "mapa_dificil.json";
        else return apresentarMenuPrincipal();

        return carregarFicheiro(ficheiro);
    }

    // --- OPÇÃO 2: JOGADOR ---
    private LabyrinthGraph<Divisao> menuMapasJogador() {
        System.out.println("\n--- MAPAS DO JOGADOR ---");
        System.out.println("Escreva o nome do ficheiro JSON (ex: 'meu_mapa.json'):");
        System.out.print("> ");
        String nome = scanner.nextLine();
        return carregarFicheiro(nome);
    }

    // --- OPÇÃO 3: GERAR (Agora com perguntas específicas) ---
    private LabyrinthGraph<Divisao> menuGerarAleatorio() {
        io.MapGenerator gerador = new io.MapGenerator();

        System.out.println("\n--- GERADOR DE MUNDOS ---");
        System.out.println("1. Pequeno (Rápido)");
        System.out.println("2. Médio (Equilibrado)");
        System.out.println("3. Grande (Longo)");
        System.out.println("4. TOTALMENTE PERSONALIZADO"); // Nome novo
        System.out.print("Escolha: ");

        int op = lerInteiro();

        if (op >= 1 && op <= 3) {
            return gerador.gerarMapaAleatorio(op);
        } else if (op == 4) {
            // --- AQUI ESTÁ A TUA LÓGICA NOVA ---
            System.out.println("\n--- CONSTRUTOR DE MAPAS ---");

            System.out.print("Quantos Spawns? (0-4): ");
            int nJogadores = lerInteiro();
            if (nJogadores < 1) nJogadores = 1;

            System.out.print("Quantas Salas de Enigma? ");
            int nEnigmas = lerInteiro();
            if (nEnigmas < 0) nEnigmas = 0;

            System.out.print("Quantas Salas Normais? ");
            int nNormais = lerInteiro();
            if (nNormais < 0) nNormais = 0;

            System.out.print("Quantos Caminhos Fechados (Trancas)? ");
            int nTrancas = lerInteiro();
            if (nTrancas < 0) nTrancas = 0;

            // Chama o novo método com os 4 ingredientes
            return gerador.gerarMapaTotalmenteCustomizado(nJogadores, nEnigmas, nNormais, nTrancas);
        }

        System.out.println("Opção inválida.");
        return apresentarMenuPrincipal();
    }

    // --- Auxiliares ---
    private LabyrinthGraph<Divisao> carregarFicheiro(String path) {
        MapLoader loader = new MapLoader();
        LabyrinthGraph<Divisao> mapa = loader.loadMap(path);
        if (mapa == null || mapa.size() == 0) {
            System.out.println("❌ Erro ao carregar mapa. Tente outro.");
            return apresentarMenuPrincipal();
        }
        return mapa;
    }

    private int lerInteiro() {
        try { return Integer.parseInt(scanner.nextLine()); }
        catch (Exception e) { return -1; }
    }
}