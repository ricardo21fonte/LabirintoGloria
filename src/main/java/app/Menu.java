package app;

import java.util.List;
import java.util.Scanner;

import game.Divisao;
import game.LabyrinthGraph;
import io.GameReport;
import io.GameReportLoader;
import io.MapLoader;

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

    // --- OPÇÃO 1: ORIGINAIS ---
    private LabyrinthGraph<Divisao> menuMapasOriginais() {
        System.out.println("\n MAPAS ORIGINAIS");
        System.out.println("1. O Início (Fácil)");
        System.out.println("2. A Masmorra (Médio)");
        System.out.println("3. O Pesadelo (Difícil)");
        System.out.print("Escolha: ");

        String ficheiro = "";
        int op = lerInteiro();
        if (op == 1) ficheiro = "mapa_Oinicio.json";
        else if (op == 2) ficheiro = "mapa_medio.json";
        else if (op == 3) ficheiro = "mapa_dificil.json";
        else return apresentarMenuPrincipal();

        return carregarFicheiro(ficheiro);
    }

    // --- OPÇÃO 2: JOGADOR ---// mudar isto para aparecer os mapas guardados e nao o jogador ter de os carregar manualmente
    private LabyrinthGraph<Divisao> menuMapasJogador() {
        System.out.println("\n MAPAS DO JOGADOR ");
        System.out.println("Escreva o nome do ficheiro JSON (ex: 'meu_mapa.json'):");
        System.out.print("> ");
        String nome = scanner.nextLine();
        return carregarFicheiro(nome);
    }

    // --- OPÇÃO 3: GERAR (Agora com perguntas específicas) ---
    private LabyrinthGraph<Divisao> menuGerarAleatorio() {
        io.MapGenerator gerador = new io.MapGenerator();

        System.out.println("\n Criação de um novo mapa!");
        System.out.println("1. Pequeno (Rápido)");
        System.out.println("2. Médio (Equilibrado)");
        System.out.println("3. Grande (Longo)");
        System.out.println("4. Personaliza o teu mapa"); // Nome novo
        System.out.print("Escolha: ");

        int op = lerInteiro();

        if (op >= 1 && op <= 3) {
            return gerador.gerarMapaAleatorio(op);
        } else if (op == 4) {
            // --- AQUI ESTÁ A TUA LÓGICA NOVA ---
            System.out.println("\n PERSONALIZAÇÃO DO MAPA");

            System.out.print("Quantos Spawns? ");
            int nJogadores = lerInteiro();
            if (nJogadores < 1) nJogadores = 1;

            System.out.print("Quantas Salas de Enigma? ");
            int nEnigmas = lerInteiro();
            if (nEnigmas < 0) nEnigmas = 0;

            System.out.print("Quantas Salas Normais? ");
            int nNormais = lerInteiro();
            if (nNormais < 0) nNormais = 0;

            System.out.print("Quantos Caminhos Fechados? ");
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
            System.out.println("Erro ao carregar mapa. Tente outra vez.");
            return apresentarMenuPrincipal();
        }
        return mapa;
    }

    private int lerInteiro() {
        try { return Integer.parseInt(scanner.nextLine()); }
        catch (Exception e) { return -1; }
    }

    private String lerString() {
        try {
            return scanner.nextLine().trim();
        } catch (Exception e) {
            return "";
        }
    }

    // --- OPÇÃO 4: RELATÓRIOS DE JOGOS ---
    private void menuRelatorios() {
        System.out.println("\n========== RELATÓRIOS DE JOGOS ==========");
        
        GameReportLoader loader = new GameReportLoader();
        List<String> relatorios = loader.listarRelatorios();

        if (relatorios.isEmpty()) {
            System.out.println("Nenhum jogo guardado.");
            System.out.println("Pressiona ENTER para voltar ao menu principal...");
            lerString();
            return;
        }

        System.out.println("\nJogos guardados (mais recentes primeiro):");
        for (int i = 0; i < relatorios.size(); i++) {
            String filename = relatorios.get(i);
            String timestamp = filename.substring(5, filename.length() - 5);
            if (timestamp.length() == 15) {
                String formatted = timestamp.substring(0, 4) + "-" + 
                                  timestamp.substring(4, 6) + "-" + 
                                  timestamp.substring(6, 8) + " " +
                                  timestamp.substring(9, 11) + ":" + 
                                  timestamp.substring(11, 13) + ":" + 
                                  timestamp.substring(13, 15);
                System.out.println("[" + (i + 1) + "] " + formatted);
            } else {
                System.out.println("[" + (i + 1) + "] " + filename);
            }
        }

        System.out.println("[0] Voltar ao Menu Principal");
        System.out.print("Escolha: ");
        int opcao = lerInteiro();

        if (opcao == 0) {
            return;
        }

        if (opcao < 1 || opcao > relatorios.size()) {
            System.out.println("Opção inválida.");
            menuRelatorios();
            return;
        }

        String selectedFilename = relatorios.get(opcao - 1);
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

    private void exibirRelatorio(GameReport report) {
        System.out.println("\n========== RELATÓRIO COMPLETO DO JOGO ==========");
        System.out.println("Vencedor: " + report.getVencedor());
        
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        System.out.println("Data/Hora: " + report.getDataHora().format(formatter));
        System.out.println("Duração: " + report.getDuracao() + " turnos");
        System.out.println("Mapa: " + report.getMapaNome());
        System.out.println("Dificuldade: " + report.getDificuldade());
        System.out.println("Total de Enigmas Resolvidos: " + report.getTotalEnigmasResolvidos());
        System.out.println("Total de Obstáculos Enfrentados: " + report.getTotalObstaculos());
        
        System.out.println("\n========== JOGADORES ==========");
        List<GameReport.PlayerReport> jogadores = report.getListaJogadores();
        for (GameReport.PlayerReport player : jogadores) {
            System.out.println("\n--- " + player.getNome() + " (" + player.getTipo() + ") ---");
            System.out.println("Vencedor: " + (player.isVencedor() ? "SIM" : "NÃO"));
            System.out.println("Localização Final: " + player.getLocalAtual());
            System.out.println("Turnos Jogados: " + player.getTurnosJogados());
            System.out.println("Enigmas Resolvidos: " + player.totalEnigmasResolvidos());
            
            // Percurso
            System.out.println("\nPERCURSO (Caminho):");
            List<String> percurso = player.getPercurso();
            if (percurso.isEmpty()) {
                System.out.println("  (sem movimentos)");
            } else {
                for (int i = 0; i < percurso.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + percurso.get(i));
                }
            }
            
            // Obstáculos
            System.out.println("\nOBSTÁCULOS ENFRENTADOS:");
            List<String> obstaculos = player.getObstaculos();
            if (obstaculos.isEmpty()) {
                System.out.println("  (sem obstáculos)");
            } else {
                for (String obstaculo : obstaculos) {
                    System.out.println("  - " + obstaculo);
                }
            }
            
            // Enigmas
            System.out.println("\nENIGMAS:");
            List<GameReport.EnigmaEvent> enigmas = player.getEnigmas();
            if (enigmas.isEmpty()) {
                System.out.println("  (sem enigmas)");
            } else {
                for (int i = 0; i < enigmas.size(); i++) {
                    GameReport.EnigmaEvent enigma = enigmas.get(i);
                    System.out.println("  Enigma " + (i + 1) + " (Sala: " + enigma.sala + "):");
                    System.out.println("    Pergunta: " + enigma.pergunta);
                    System.out.println("    Resposta: " + enigma.resposta);
                    System.out.println("    Resultado: " + (enigma.resolvido ? "CORRETO ✓" : "ERRADO ✗"));
                    System.out.println("    Efeito: " + enigma.efeito);
                }
            }
            
            // Efeitos aplicados
            System.out.println("\nEFEITOS APLICADOS:");
            List<String> efeitos = player.getEfeitosAplicados();
            if (efeitos.isEmpty()) {
                System.out.println("  (sem efeitos)");
            } else {
                for (String efeito : efeitos) {
                    System.out.println("  - " + efeito);
                }
            }
        }
        System.out.println("\n================================================");
    }
}