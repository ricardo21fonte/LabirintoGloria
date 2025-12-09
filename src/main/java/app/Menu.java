package app;

import Lists.ArrayUnorderedList; 
import java.util.Iterator;
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

    // ... (MÉTODOS OPÇÃO 1, 2 e 3 MANTÊM-SE IGUAIS - Cópia do teu código) ...
    // Vou omitir para poupar espaço, mantém o que tinhas para Originais, Jogador e GerarAleatorio.
    // Se precisares deles diz, mas o erro está no Relatórios abaixo.

    // --- MÉTODOS MANTIDOS (Resumo) ---
    private LabyrinthGraph<Divisao> menuMapasOriginais() {
        // (O teu código original aqui)
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

    private LabyrinthGraph<Divisao> menuMapasJogador() {
        System.out.println("\n MAPAS DO JOGADOR ");
        System.out.println("Escreva o nome do ficheiro JSON (ex: 'meu_mapa.json'):");
        System.out.print("> ");
        String nome = scanner.nextLine();
        return carregarFicheiro(nome);
    }

    private LabyrinthGraph<Divisao> menuGerarAleatorio() {
        io.MapGenerator gerador = new io.MapGenerator();
        System.out.println("\n Criação de um novo mapa!");
        System.out.println("1. Pequeno (Rápido)");
        System.out.println("2. Médio (Equilibrado)");
        System.out.println("3. Grande (Longo)");
        System.out.println("4. Personaliza o teu mapa");
        System.out.print("Escolha: ");
        int op = lerInteiro();
        if (op >= 1 && op <= 3) {
            return gerador.gerarMapaAleatorio(op);
        } else if (op == 4) {
            System.out.println("\n PERSONALIZAÇÃO DO MAPA");
            System.out.print("Quantos Spawns? ");
            int nJogadores = lerInteiro();
            if (nJogadores < 1) nJogadores = 1;
            System.out.print("Quantas Salas de Enigma? ");
            int nEnigmas = lerInteiro(); if (nEnigmas < 0) nEnigmas = 0;
            System.out.print("Quantas Salas Normais? ");
            int nNormais = lerInteiro(); if (nNormais < 0) nNormais = 0;
            System.out.print("Quantos Caminhos Fechados? ");
            int nTrancas = lerInteiro(); if (nTrancas < 0) nTrancas = 0;
            return gerador.gerarMapaTotalmenteCustomizado(nJogadores, nEnigmas, nNormais, nTrancas);
        }
        System.out.println("Opção inválida.");
        return apresentarMenuPrincipal();
    }
    
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
        try { return scanner.nextLine().trim(); } 
        catch (Exception e) { return ""; }
    }

    // --- AQUI ESTÁ A CORREÇÃO GRANDE ---

    private void menuRelatorios() {
        System.out.println("\n========== RELATÓRIOS DE JOGOS ==========");
        
        GameReportLoader loader = new GameReportLoader();
        // Agora recebe a lista correta
        ArrayUnorderedList<String> relatorios = loader.listarRelatorios();

        if (relatorios.isEmpty()) {
            System.out.println("Nenhum jogo guardado.");
            System.out.println("Pressiona ENTER para voltar ao menu principal...");
            lerString();
            return;
        }

        System.out.println("\nJogos guardados:");
        
        // CORREÇÃO: Usar Iterator para listar
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

        // CORREÇÃO: Usar Iterator para encontrar o ficheiro escolhido
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

    private void exibirRelatorio(GameReport report) {
        System.out.println("\n========== RELATÓRIO COMPLETO DO JOGO ==========");
        System.out.println("Vencedor: " + report.getVencedor());
        // ... (data e outros campos simples mantêm-se) ...
        System.out.println("Mapa: " + report.getMapaNome());
        System.out.println("Dificuldade: " + report.getDificuldade());
        System.out.println("Total de Enigmas Resolvidos: " + report.getTotalEnigmasResolvidos());
        
        System.out.println("\n========== JOGADORES ==========");
        
        // CORREÇÃO: Receber ArrayUnorderedList e usar Iterator
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