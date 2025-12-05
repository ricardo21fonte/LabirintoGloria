package app;

import java.util.Scanner;
import java.util.Iterator;

// Imports do Jogo
import game.labirinto;
import game.LabyrinthGraph;
import game.Divisao;
import game.Player;
import game.Enigma;

// Imports de Input/Output
import io.MapLoader;
import io.EnigmaLoader;
import io.GameExporter;

// Imports de Enums e Estruturas
import enums.TipoDivisao;
import enums.Dificuldade;
import enums.CorredorEvent;
import Lists.ArrayUnorderedList;
import Queue.LinkedQueue; // A tua Fila para os turnos

public class Main {

    // Scanner global para evitar problemas de fechar/abrir streams
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("      🏰 LABIRINTO DA GLÓRIA 🏰");
        System.out.println("      Modo: Multijogador com Dados 🎲");
        System.out.println("==========================================\n");

        // ===========================================================
        // 1. CARREGAR DADOS (Enigmas e Mapa)
        // ===========================================================
        System.out.println("--- A CARREGAR RECURSOS ---");
        
        // A. Enigmas
        EnigmaLoader enigmaLoader = new EnigmaLoader();
        ArrayUnorderedList<Enigma> todosEnigmas = enigmaLoader.loadEnigmas("enigmas.json");

        // B. Mapa
        MapLoader mapLoader = new MapLoader();
        // Certifica-te que mapa.json está na raiz do projeto
        LabyrinthGraph<Divisao> labirintoGraph = mapLoader.loadMap("mapa.json");

        if (labirintoGraph == null || labirintoGraph.size() == 0) {
            System.out.println("❌ ERRO CRÍTICO: Mapa não carregado.");
            return;
        }

        // Inicializar Motor
        labirinto motorJogo = new labirinto();
        motorJogo.setMapa(labirintoGraph);

        // ===========================================================
        // 2. CONFIGURAR DIFICULDADE
        // ===========================================================
        System.out.println("\nEscolha a Dificuldade:");
        System.out.println("1 - FÁCIL | 2 - MÉDIO | 3 - DIFÍCIL");
        System.out.print("Opção: ");
        int opDif = lerInteiro();
        
        Dificuldade difJogo = Dificuldade.FACIL;
        if (opDif == 2) difJogo = Dificuldade.MEDIO;
        if (opDif == 3) difJogo = Dificuldade.DIFICIL;

        // Filtrar Enigmas
        ArrayUnorderedList<Enigma> enigmasFiltrados = new ArrayUnorderedList<>();
        Iterator<Enigma> itEnigmas = todosEnigmas.iterator();
        while (itEnigmas.hasNext()) {
            Enigma e = itEnigmas.next();
            if (e.getDificuldade() == difJogo) enigmasFiltrados.addToRear(e);
        }
        motorJogo.setEnigmas(enigmasFiltrados);
        System.out.println("Dificuldade definida: " + difJogo + " (" + enigmasFiltrados.size() + " enigmas).");

        // ===========================================================
        // 3. CONFIGURAÇÃO DOS JOGADORES (FILA)
        // ===========================================================
        
        // A. Encontrar Spawns
        ArrayUnorderedList<Divisao> entradas = new ArrayUnorderedList<>();
        Object[] vertices = labirintoGraph.getVertices();
        for (Object obj : vertices) {
            Divisao d = (Divisao) obj;
            if (d.getTipo() == TipoDivisao.ENTRADA) entradas.addToRear(d);
        }

        if (entradas.isEmpty()) {
            System.out.println("❌ Erro: O mapa não tem Entradas!");
            return;
        }

        // B. Criar Jogadores
        System.out.print("\nQuantos jogadores (1-8)? ");
        int numJogadores = lerInteiro();
        if (numJogadores < 1) numJogadores = 1;
        
        LinkedQueue<Player> filaDeTurnos = new LinkedQueue<>();
        Iterator<Divisao> itSpawns = entradas.iterator();

        for (int i = 1; i <= numJogadores; i++) {
            System.out.print("Nome do Jogador " + i + ": ");
            String nome = scanner.nextLine();
            
            // Atribuir Spawn (Circular)
            if (!itSpawns.hasNext()) itSpawns = entradas.iterator();
            Divisao spawn = itSpawns.next();
            
            Player p = new Player(nome, spawn);
            motorJogo.adicionarJogador(p);
            filaDeTurnos.enqueue(p);
            System.out.println("   -> " + nome + " começa em: " + spawn.getNome());
        }

        System.out.println("\n⚔️ QUE COMECE A CORRIDA! ⚔️");
        esperarEnter();

        // ===========================================================
        // 4. CICLO DE JOGO (GAME LOOP)
        // ===========================================================
        boolean jogoAcorrer = true;

        while (jogoAcorrer && !filaDeTurnos.isEmpty()) {
            
            // 1. Tirar jogador da fila
            Player atual;
            try { atual = filaDeTurnos.dequeue(); } catch (Exception e) { break; }

            System.out.println("\n================================================");
            System.out.println("👤 VEZ DE: " + atual.getNome().toUpperCase());
            System.out.println("📍 Local: " + atual.getLocalAtual().getNome());

            // 2. Verificar Bloqueio (Evento PERDER_VEZ)
            if (atual.isBloqueado()) {
                System.out.println("🚫 Estás bloqueado e perdes este turno!");
                atual.setBloqueado(false);
                filaDeTurnos.enqueue(atual); // Volta para o fim da fila
                esperarEnter();
                continue;
            }

            // 3. LANÇAR DADOS 🎲
            System.out.println("Pressiona ENTER para lançar o dado...");
            scanner.nextLine();
            int movimentos = lancarDados();
            System.out.println("🎲 ROLASTE UM " + movimentos + "! Tens " + movimentos + " movimentos.");

            // 4. REALIZAR MOVIMENTOS
            boolean turnoQueimado = false;

            while (movimentos > 0 && !turnoQueimado) {
                System.out.println("\n--- Passos Restantes: " + movimentos + " ---");
                System.out.println("Estás em: " + atual.getLocalAtual().getNome());

                // Listar Vizinhos
                ArrayUnorderedList<Divisao> vizinhos = labirintoGraph.getVizinhos(atual.getLocalAtual());
                Divisao[] opcoes = new Divisao[10];
                int count = 0;
                Iterator<Divisao> it = vizinhos.iterator();
                while (it.hasNext()) {
                    Divisao v = it.next();
                    opcoes[count] = v;
                    System.out.println("   [" + (count + 1) + "] Ir para: " + v.getNome() + " (" + v.getTipo() + ")");
                    count++;
                }
                System.out.println("   [0] Parar e terminar turno");

                // Ler Escolha
                System.out.print("Escolha: ");
                int escolha = lerInteiro();

                if (escolha == 0) break; // Encerra turno voluntariamente

                if (escolha < 1 || escolha > count) {
                    System.out.println("⚠️ Opção inválida.");
                    continue;
                }

                Divisao destino = opcoes[escolha - 1];
                boolean podeEntrar = true;

                // --- LÓGICA DE ENIGMA ---
                if (destino.getTipo() == TipoDivisao.SALA_ENIGMA) {
                    System.out.println("\n🕵️ ENIGMA À VISTA!");
                    Enigma desafio = motorJogo.obterEnigma(difJogo);
                    
                    if (desafio != null) {
                        System.out.println("P: " + desafio.getPergunta());
                        String[] ops = desafio.getOpcoes();
                        for(int k=0; k<ops.length; k++) System.out.println("   ("+(k+1)+") "+ops[k]);
                        
                        System.out.print("Resp: ");
                        int r = lerInteiro();
                        
                        if (desafio.verificarResposta(r)) {
                            System.out.println("✅ Correto! Podes passar.");
                        } else {
                            System.out.println("❌ Errado! A porta tranca-se e perdes o resto do turno.");
                            podeEntrar = false;
                            turnoQueimado = true;
                        }
                    }
                }

                // --- MOVIMENTO ---
                if (podeEntrar) {
                    // Verificar Eventos antes de mover
                    CorredorEvent evento = labirintoGraph.getCorridorEvent(atual.getLocalAtual(), destino);
                    
                    motorJogo.realizarJogada(atual, destino); // Move efetivamente

                    // Verificar efeitos negativos que terminam o turno
                    if (evento.getType() == CorredorEvent.Type.BLOCK_TURN || 
                        evento.getType() == CorredorEvent.Type.MOVE_BACK) {
                        System.out.println("⛔ O evento interrompeu a tua caminhada!");
                        turnoQueimado = true;
                    }
                    
                    movimentos--; 

                    // --- VITÓRIA ---
                    if (atual.getLocalAtual().getTipo() == TipoDivisao.SALA_CENTRAL) {
                        System.out.println("\n🎉🎉 TEMOS UM VENCEDOR! 🎉🎉");
                        System.out.println("Parabéns, " + atual.getNome() + "!");
                        
                        // Exportar JSON
                        GameExporter exporter = new GameExporter();
                        exporter.exportarRelatorio(atual);
                        
                        jogoAcorrer = false;
                        turnoQueimado = true; // Para sair do while interno
                    }
                }
            } // Fim do while de movimentos

            if (jogoAcorrer) {
                System.out.println("Fim do turno de " + atual.getNome() + ".");
                filaDeTurnos.enqueue(atual); // Volta para o fim da fila
                esperarEnter();
            }
        }
        
        System.out.println("Obrigado por jogar!");
        scanner.close();
    }

    // --- MÉTODOS AUXILIARES ---

    private static int lancarDados() {
        // Gera número entre 1 e 6
        return (int) (Math.random() * 6) + 1;
    }

    private static int lerInteiro() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            return -1;
        }
    }

    private static void esperarEnter() {
        System.out.println("(Pressione Enter para continuar...)");
        scanner.nextLine();
    }
}