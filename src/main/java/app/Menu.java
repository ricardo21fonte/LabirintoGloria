package app;

import java.util.Iterator;
import java.util.Scanner;

import Lists.ArrayUnorderedList;
import game.Divisao;
import graph.LabyrinthGraph;
import io.GameReport;
import io.GameReportLoader;
import io.MapLoader;
/**
 * Main text-based menu of the game.
 */
public class Menu {
    /** Scanner used to read user input from the console. */
    private Scanner scanner;

    /**
     * Logical name of the currently selected map.
     */
    private String nomeMapaAtual = "Mapa_Desconhecido";

    /**
     * Creates a new menu and initializes the input scanner.
     */
    public Menu() {
        this.scanner = new Scanner(System.in);
    }
    /**
     * Returns the logical name of the currently selected map.
     * @return current map name
     */
    public String getNomeMapaAtual() {
        return nomeMapaAtual;
    }
    /**
     * Displays main menu and returns a loaded or generated map,
     * @return a LabyrinthGraph to be used by the game engine, or {@code null} on exit
     */
    public LabyrinthGraph<Divisao> apresentarMenuPrincipal() {
        System.out.println("\n==========================================");
        System.out.println("      LABIRINTO DA GLÓRIA ");
        System.out.println("==========================================");
        System.out.println("1. Mapas Originais");
        System.out.println("2. Mapas Guardados");
        System.out.println("3. Criar Mapa Aleatório");
        System.out.println("4. Relatórios de Jogos");
        System.out.println("0. Sair");
        System.out.print("Escolha: ");

        int opcao = lerInteiro();

        switch (opcao) {
            case 1: return menuMapasOriginais();
            case 2: return menuMapasJogador();
            case 3: return menuGerarAleatorio();
            case 4: menuRelatorios(); return apresentarMenuPrincipal();
            case 0: System.exit(0); return null;
            default: System.out.println("Opção inválida."); return apresentarMenuPrincipal();
        }
    }

    /**
     * Menu for choosing one of the original predefined maps.
     * @return loaded original map, or returns to the main menu on invalid option
     */
    private LabyrinthGraph<Divisao> menuMapasOriginais() {
        System.out.println("\n MAPAS ORIGINAIS");
        System.out.println("1. O Início (Fácil)");
        System.out.println("2. O Minotauro (Médio)");
        System.out.println("3. A Tortura (Difícil)");
        System.out.print("Escolha: ");

        String ficheiro = "";
        int op = lerInteiro();
        if (op == 1) {
            ficheiro = "resources/mapas_originais/mapa_Oinicio.json";
            nomeMapaAtual = "O_Inicio";
        }
        else if (op == 2) {
            ficheiro = "resources/mapas_originais/mapa_Ominotauro.json";
            nomeMapaAtual = "O_Minotauro";
        }
        else if (op == 3) {
            ficheiro = "resources/mapas_originais/mapa_Atortura.json";
            nomeMapaAtual = "A_Tortura";
        }
        else return apresentarMenuPrincipal();

        return carregarFicheiro(ficheiro);
    }
    /**
     * Menu for loading player-created maps from the saved-games folder.
     * @return loaded map, or returns to the main menu on failure
     */
    private LabyrinthGraph<Divisao> menuMapasJogador() {
        System.out.println("\n MAPAS DO JOGADOR ");
        System.out.println("Escreva o nome do ficheiro JSON (ex: 'ze3.json'):");
        System.out.print("> ");
        String nome = lerString();

        nomeMapaAtual = nome.replace(".json", "");

        return carregarFicheiro("resources/saved_games/" + nome);
    }
    /**
     * Menu for generating a random map and optionally saving it to file.
     * @return the generated map, or returns to the main menu on invalid option
     */
    private LabyrinthGraph<Divisao> menuGerarAleatorio() {
        io.MapGenerator gerador = new io.MapGenerator();
        LabyrinthGraph<Divisao> mapaGerado = null;

        nomeMapaAtual = "Aleatorio_" + System.currentTimeMillis();

        System.out.println("\n Criação de um novo mapa!");
        System.out.println("1. Pequeno (Rápido)");
        System.out.println("2. Médio (Equilibrado)");
        System.out.println("3. Grande (Longo)");
        System.out.println("4. Personaliza o teu mapa");
        System.out.print("Escolha: ");

        int op = lerInteiro();

        if (op >= 1 && op <= 3) {
            mapaGerado = gerador.gerarMapaAleatorio(op);
        } else if (op == 4) {
            System.out.println("\n PERSONALIZAÇÃO DO MAPA");
            System.out.print("Quantos Spawns? ");
            int nJogadores = lerInteiro(); if (nJogadores < 1) nJogadores = 1;
            System.out.print("Quantas Salas de Enigma? ");
            int nEnigmas = lerInteiro(); if (nEnigmas < 0) nEnigmas = 0;
            System.out.print("Quantas Salas Normais? ");
            int nNormais = lerInteiro(); if (nNormais < 0) nNormais = 0;
            System.out.print("Quantos Caminhos Fechados? ");
            int nTrancas = lerInteiro(); if (nTrancas < 0) nTrancas = 0;
            mapaGerado = gerador.gerarMapaTotalmenteCustomizado(nJogadores, nEnigmas, nNormais, nTrancas);
        } else {
            System.out.println("Opção inválida.");
            return apresentarMenuPrincipal();
        }

        if (mapaGerado != null) {
            System.out.println("\n Mapa gerado! Desejas gravá-lo para jogar mais tarde?");
            System.out.println("1 - Sim, gravar");
            System.out.println("2 - Não, jogar apenas agora");
            System.out.print("> ");

            int escolhaGravar = lerInteiro();
            if (escolhaGravar == 1) {
                System.out.print("Nome do ficheiro (ex: ze3.json): ");
                String nomeFicheiro = lerString();
                if (!nomeFicheiro.endsWith(".json")) nomeFicheiro += ".json";

                io.MapExporter exporter = new io.MapExporter();

                exporter.exportarMapa(mapaGerado, "Mapa Custom " + java.time.LocalDateTime.now(), "resources/saved_games/" + nomeFicheiro);

                nomeMapaAtual = nomeFicheiro.replace(".json", "");

                System.out.println("Podes carregar este mapa no Menu Principal > Opção 2.");
            }
        }
        return mapaGerado;
    }
    /**
     * Method that loads a map from a given JSON file path and performs validation.
     *
     * @param path full path to the JSON file
     * @return loaded map, or a new map chosen by the user
     */
    private LabyrinthGraph<Divisao> carregarFicheiro(String path) {
        MapLoader loader = new MapLoader();
        LabyrinthGraph<Divisao> mapa = loader.loadMap(path);

        if (mapa == null || mapa.size() == 0) {

            System.out.println(" Falha crítica: O mapa não pôde ser carregado.");
            System.out.println("   Verifique se o ficheiro existe em: " + path);
            return apresentarMenuPrincipal();
        }
        return mapa;
    }
    /**
     * Reads an integer from the console.
     * @return parsed integer
     */
    private int lerInteiro() {
        try { return Integer.parseInt(scanner.nextLine()); }
        catch (Exception e) { return -1; }
    }
    /**
     * Reads a line of text from the console and trims it.
     * @return trimmed string, or empty string on error
     */
    private String lerString() {
        try { return scanner.nextLine().trim(); }
        catch (Exception e) { return ""; }
    }
    /**
     * Displays the reports menu, allowing the user to list and inspect previously saved game reports.
     */
    private void menuRelatorios() {
        System.out.println("\n========== RELATÓRIOS DE JOGOS ==========");

        GameReportLoader loader = new GameReportLoader();
        ArrayUnorderedList<String> relatorios = loader.listarRelatorios();

        if (relatorios.isEmpty()) {
            System.out.println("Nenhum jogo guardado.");
            System.out.println("Pressiona ENTER para voltar ao menu principal...");
            lerString();
            return;
        }

        System.out.println("\nJogos guardados:");
        Iterator<String> it = relatorios.iterator();
        int index = 1;
        while (it.hasNext()) {
            String filename = it.next();
            System.out.println("[" + index + "] " + filename);
            index++;
        }

        System.out.println("[0] Voltar ao Menu Principal");
        System.out.print("Escolha: ");
        int opcao = lerInteiro();

        if (opcao == 0) return;

        if (opcao < 1 || opcao > relatorios.size()) {
            System.out.println("Opção inválida.");
            menuRelatorios();
            return;
        }

        String selectedFilename = null;
        Iterator<String> itSelect = relatorios.iterator();
        int current = 1;
        while (itSelect.hasNext()) {
            String f = itSelect.next();
            if (current == opcao) {
                selectedFilename = f;
                break;
            }
            current++;
        }

        GameReport report = loader.carregarRelatorio(selectedFilename);

        if (report != null) {
            exibirRelatorio(report);
        } else {
            System.out.println("Erro ao carregar relatório.");
        }

        System.out.println("\nPressiona ENTER para voltar...");
        lerString();
        menuRelatorios();
    }
    /**
     * Prints a detailed game report to the console
     * @param report the game report to display
     */
    private void exibirRelatorio(GameReport report) {
        System.out.println("\n========== RELATÓRIO COMPLETO DO JOGO ==========");
        System.out.println("Vencedor: " + report.getVencedor());
        System.out.println("Mapa: " + report.getMapaNome());
        System.out.println("Dificuldade: " + report.getDificuldade());
        int resolvidos = report.getTotalEnigmasResolvidos();
        int tentados = report.getTotalEnigmasTentados();
        int falhados = tentados - resolvidos;

        System.out.println("Total de Enigmas Tentados: " + tentados);
        System.out.println(" -> Resolvidos: " + resolvidos);
        System.out.println(" -> Falhados: " + falhados);

        System.out.println("\n========== JOGADORES ==========");

        ArrayUnorderedList<GameReport.PlayerReport> jogadores = report.getListaJogadores();
        Iterator<GameReport.PlayerReport> itJogadores = jogadores.iterator();

        while (itJogadores.hasNext()) {
            GameReport.PlayerReport player = itJogadores.next();

            System.out.println("\n--- " + player.getNome() + " (" + player.getTipo() + ") ---");
            System.out.println("Vencedor: " + (player.isVencedor() ? "SIM" : "NÃO"));
            System.out.println("Localização Final: " + player.getLocalAtual());
            System.out.println("Turnos Jogados: " + player.getTurnosJogados());
            System.out.println("Enigmas Resolvidos: " + player.totalEnigmasResolvidos());

            // Percurso
            System.out.println("\nPERCURSO (Caminho):");
            ArrayUnorderedList<String> percurso = player.getPercurso();
            if (percurso.isEmpty()) {
                System.out.println("  (sem movimentos)");
            } else {
                Iterator<String> itP = percurso.iterator();
                int i = 1;
                while (itP.hasNext()) {
                    System.out.println("  " + i + ". " + itP.next());
                    i++;
                }
            }

            // Obstáculos
            System.out.println("\nOBSTÁCULOS:");
            ArrayUnorderedList<String> obstaculos = player.getObstaculos();
            Iterator<String> itO = obstaculos.iterator();
            while (itO.hasNext()) {
                System.out.println("  - " + itO.next());
            }

            // Enigmas
            System.out.println("\nENIGMAS:");
            ArrayUnorderedList<GameReport.EnigmaEvent> enigmas = player.getEnigmas();
            Iterator<GameReport.EnigmaEvent> itE = enigmas.iterator();
            int i = 1;
            while (itE.hasNext()) {
                GameReport.EnigmaEvent e = itE.next();
                System.out.println("  Enigma " + i + " (Sala: " + e.sala + "): " + (e.resolvido ? "CORRETO" : "ERRADO"));
                i++;
            }
        }
        System.out.println("\n================================================");
    }
}
