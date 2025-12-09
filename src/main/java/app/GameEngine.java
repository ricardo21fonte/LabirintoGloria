package app;

import java.util.Iterator;
import java.util.Scanner;

import Lists.ArrayUnorderedList;
import Queue.LinkedQueue;
import enums.AlavancaEnum;
import enums.CorredorEvento;
import enums.Dificuldade;
import enums.TipoDivisao;
import game.Alavanca;
import game.Bot;
import game.Divisao;
import game.Enigma;
import game.EventoCorredor;
import game.LabyrinthGraph;
import game.Player;
import game.labirinto;
import io.EnigmaLoader;
import io.GameExporter;
import io.GameReport;

/**
 * Motor principal do jogo.
 * Centraliza toda a lógica: setup, gameplay, utilities e UI.
 */
public class GameEngine {

    private static final Scanner scanner = new Scanner(System.in);
    private LabyrinthGraph<Divisao> labyrinthGraph;
    private String vencedor;
    private int turnoCount;
    private long startTime;
    private java.util.Map<String, GameReport.PlayerReport> playerReports;
    private int totalEnigmasResolvidos;
    private int totalObstaculos;

    public GameEngine(LabyrinthGraph<Divisao> labyrinthGraph) {
        this.labyrinthGraph = labyrinthGraph;
    }

    /**
     * Inicia o jogo: setup, carregamento e loop principal.
     */
    public void start() {
        // 1. Carregar enigmas
        ArrayUnorderedList<Enigma> allEnigmas = loadEnigmas();

        // 2. Criar motor do jogo
        labirinto labyrinthGame = new labirinto();
        labyrinthGame.setMapa(labyrinthGraph);

        // 3. Setup de dificuldade
        Dificuldade difficulty = setupDifficulty();

        // 4. Filtrar enigmas por dificuldade
        ArrayUnorderedList<Enigma> filteredEnigmas = filterEnigmasByDifficulty(allEnigmas, difficulty);
        labyrinthGame.setEnigmas(filteredEnigmas);

        // 5. Obter spawn points
        Divisao[] entrances = getMapEntrances();
        if (entrances.length == 0) {
            return;
        }

        // 6. Fila de turnos
        LinkedQueue<Player> turnQueue = new LinkedQueue<>();

        // 7. Setup de jogadores
        setupHumanPlayers(labyrinthGame, entrances, turnQueue);
        setupBots(labyrinthGame, entrances, turnQueue);

        // 8. Validar se há jogadores
        if (turnQueue.isEmpty()) {
            System.out.println("Sem jogadores. Fim.");
            return;
        }

        // 9. Começar jogo
        System.out.println("\n QUE COMECE A CORRIDA! ");
        waitForEnter();

        // Initialize game tracking and reports
        vencedor = null;
        turnoCount = 0;
        startTime = System.currentTimeMillis();
        playerReports = new java.util.HashMap<>();
        totalEnigmasResolvidos = 0;
        totalObstaculos = 0;
        
        // Initialize player reports for all players in queue
        LinkedQueue<Player> tempQueue = new LinkedQueue<>();
        while (!turnQueue.isEmpty()) {
            try {
                Player p = turnQueue.dequeue();
                tempQueue.enqueue(p);
                GameReport.PlayerReport pReport = new GameReport.PlayerReport(p.getNome(), p instanceof Bot ? "Bot" : "Humano");
                playerReports.put(p.getNome(), pReport);
            } catch (Exception e) {
                break;
            }
        }
        // Restore queue
        while (!tempQueue.isEmpty()) {
            try {
                turnQueue.enqueue(tempQueue.dequeue());
            } catch (Exception e) {
                break;
            }
        }

        // 10. Game loop
        runGameLoop(labyrinthGame, turnQueue, difficulty);

        // 11. Fechar
        scanner.close();
    }

    // ========== SETUP ==========

    private ArrayUnorderedList<Enigma> loadEnigmas() {
        System.out.println("\nA CARREGAR RECURSOS DO JOGO");
        EnigmaLoader enigmaLoader = new EnigmaLoader();
        return enigmaLoader.loadEnigmas("enigmas.json");
    }

    private Dificuldade setupDifficulty() {
        int option = -1;
        do {
            System.out.println("\nEscolha a Dificuldade dos Enigmas:");
            System.out.println("1 - FÁCIL | 2 - MÉDIO | 3 - DIFÍCIL");
            System.out.print("Opção: ");
            option = readInteger();

            if (option < 1 || option > 3) {
                System.out.println("Opção inválida. Por favor escolha 1, 2 ou 3.");
            }
        } while (option < 1 || option > 3);

        if (option == 1) return Dificuldade.FACIL;
        if (option == 2) return Dificuldade.MEDIO;
        return Dificuldade.DIFICIL;
    }

    private ArrayUnorderedList<Enigma> filterEnigmasByDifficulty(
            ArrayUnorderedList<Enigma> allEnigmas, Dificuldade difficulty) {
        ArrayUnorderedList<Enigma> filtered = new ArrayUnorderedList<>();
        Iterator<Enigma> it = allEnigmas.iterator();
        while (it.hasNext()) {
            Enigma enigma = it.next();
            if (enigma.getDificuldade() == difficulty) {
                filtered.addToRear(enigma);
            }
        }
        System.out.println("Dificuldade definida: " + difficulty + " (" + filtered.size() + " enigmas).");
        return filtered;
    }

    private Divisao[] getMapEntrances() {
        ArrayUnorderedList<Divisao> entrances = new ArrayUnorderedList<>();
        Object[] vertices = labyrinthGraph.getVertices();

        for (Object obj : vertices) {
            Divisao division = (Divisao) obj;
            if (division.getTipo() == TipoDivisao.ENTRADA) {
                entrances.addToRear(division);
            }
        }

        if (entrances.isEmpty()) {
            System.out.println("Erro: O mapa não tem Entradas!");
            return new Divisao[0];
        }

        int totalEntrances = entrances.size();
        Divisao[] arrayEntrances = new Divisao[totalEntrances];
        Iterator<Divisao> it = entrances.iterator();
        int idx = 0;
        while (it.hasNext()) {
            arrayEntrances[idx++] = it.next();
        }

        return arrayEntrances;
    }

    private void setupHumanPlayers(labirinto labyrinthGame, Divisao[] entrances, LinkedQueue<Player> turnQueue) {
        int numHumans;
        do {
            System.out.print("\nQuantos Humanos? (0-4): ");
            numHumans = readInteger();
            if (numHumans < 0 || numHumans > 4) {
                System.out.println("Número inválido. Introduza um número entre 0 e 4.");
            }
        } while (numHumans < 0 || numHumans > 4);

        for (int i = 1; i <= numHumans; i++) {
            String name;
            do {
                System.out.print("Nome do Jogador " + i + ": ");
                name = readString();
                if (name == null || name.isEmpty()) {
                    System.out.println("Nome inválido. Tente novamente.");
                }
            } while (name == null || name.isEmpty());

            int randomSpawn = (int) (Math.random() * entrances.length);
            Divisao spawn = entrances[randomSpawn];

            Player player = new Player(name, spawn);
            labyrinthGame.adicionarJogador(player);
            turnQueue.enqueue(player);
            System.out.println("  Spawn: " + spawn.getNome());
        }
    }

    private void setupBots(labirinto labyrinthGame, Divisao[] entrances, LinkedQueue<Player> turnQueue) {
        int numBots;
        do {
            System.out.print("\nQuantos Bots? (0-4): ");
            numBots = readInteger();
            if (numBots < 0 || numBots > 4) {
                System.out.println("Número inválido. Introduza um número entre 0 e 4.");
            }
        } while (numBots < 0 || numBots > 4);

        for (int i = 1; i <= numBots; i++) {
            int randomSpawn = (int) (Math.random() * entrances.length);
            Divisao spawn = entrances[randomSpawn];

            int botDiffOption;
            do {
                System.out.println("Dificuldade do Bot " + i + ": [1-Fácil, 2-Médio, 3-Difícil]");
                System.out.print("> ");
                botDiffOption = readInteger();
                if (botDiffOption < 1 || botDiffOption > 3) {
                    System.out.println("Opção inválida. Escolha 1, 2 ou 3.");
                }
            } while (botDiffOption < 1 || botDiffOption > 3);

            Dificuldade botDifficulty = Dificuldade.FACIL;
            if (botDiffOption == 2) botDifficulty = Dificuldade.MEDIO;
            if (botDiffOption == 3) botDifficulty = Dificuldade.DIFICIL;

            Bot bot = new Bot("Bot_" + i, spawn, botDifficulty, labyrinthGraph);
            labyrinthGame.adicionarJogador(bot);
            turnQueue.enqueue(bot);
            System.out.println("  Bot criado no " + spawn.getNome());
        }
    }

    // ========== GAME LOOP ==========

    private void runGameLoop(labirinto labyrinthGame, LinkedQueue<Player> turnQueue, Dificuldade difficulty) {
        boolean gameRunning = true;

        while (gameRunning && !turnQueue.isEmpty()) {
            Player currentPlayer;
            try {
                currentPlayer = turnQueue.dequeue();
            } catch (Exception e) {
                break;
            }
            
            turnoCount++;

            printTurnStart(currentPlayer);

            // Verificar bloqueio
            if (currentPlayer.isBloqueado()) {
                printPlayerBlocked(currentPlayer);
                currentPlayer.consumirUmTurnoBloqueado();
                turnQueue.enqueue(currentPlayer);
                pauseForHuman(currentPlayer);
                continue;
            }

            // Lançar dados
            int movements = rollDiceAndGetMovements(currentPlayer);

            // Aplicar bónus
            if (currentPlayer.getJogadasExtra() > 0) {
                int extra = currentPlayer.getJogadasExtra();
                movements += extra;
                currentPlayer.setJogadasExtra(0);
                printExtraMovements(extra, movements);
            }

            // Processar movimentos
            boolean turnEnded = false;
            while (movements > 0 && !turnEnded) {
                pauseForBot(currentPlayer);

                printMovementStatus(currentPlayer, movements);

                ArrayUnorderedList<Divisao> neighbors = labyrinthGraph.getVizinhos(currentPlayer.getLocalAtual());
                Divisao destination = chooseDestination(currentPlayer, neighbors);

                if (destination == null) {
                    break;
                }

                // Enigma na porta
                boolean canEnter = true;
                if (destination.getTipo() == TipoDivisao.SALA_ENIGMA) {
                    Enigma enigma = labyrinthGame.obterEnigma(difficulty);
                    boolean solved = presentAndSolveEnigma(currentPlayer, enigma);

                    if (solved) {
                        int bonus = applyEffect(enigma.getEfeitoSucesso(), currentPlayer, turnQueue);
                        if (bonus > 0) {
                            movements += bonus;
                            System.out.println("   (Tens agora " + movements + " movimentos!)");
                        }
                    } else {
                        canEnter = false;
                        turnEnded = true;
                        applyEffect(enigma.getEfeitoFalha(), currentPlayer, turnQueue);
                    }
                }

                // Movimento
                if (canEnter) {
                    if (!checkCorridorEvent(currentPlayer, destination)) {
                        // Log obstacle
                        GameReport.PlayerReport pReport = playerReports.get(currentPlayer.getNome());
                        if (pReport != null) {
                            pReport.adicionarObstaculo("Tranca ou bloqueio em " + destination.getNome());
                            totalObstaculos++;
                        }
                        continue;
                    }

                    labyrinthGame.realizarJogada(currentPlayer, destination);
                    
                    // Log movement in percurso
                    GameReport.PlayerReport pReport = playerReports.get(currentPlayer.getNome());
                    if (pReport != null) {
                        pReport.adicionarPercurso(destination.getNome());
                    }

                    if (isTrapActivated(currentPlayer, destination)) {
                        // Log trap
                        if (pReport != null) {
                            pReport.adicionarObstaculo("Armadilha em " + destination.getNome());
                            totalObstaculos++;
                        }
                        turnEnded = true;
                    }

                    movements--;

                    // Sala de alavanca
                    if (!turnEnded && destination.getTipo() == TipoDivisao.SALA_ALAVANCA) {
                        if (solveLeverRoom(currentPlayer, destination)) {
                            turnEnded = true;
                        }
                    }

                    // Verificar vitória
                    if (currentPlayer.getLocalAtual().getTipo() == TipoDivisao.SALA_CENTRAL) {
                        printWinner(currentPlayer.getNome());
                        vencedor = currentPlayer.getNome();
                        
                        // Update player report as winner
                        if (pReport != null) {
                            pReport.setVencedor(true);
                            pReport.setLocalAtual(currentPlayer.getLocalAtual().getNome());
                            pReport.setTurnosJogados(turnoCount);
                        }
                        
                        // Create and export game report
                        GameReport report = createGameReport(currentPlayer, difficulty);
                        GameExporter exporter = new GameExporter();
                        exporter.exportarJogo(report);
                        
                        gameRunning = false;
                        turnEnded = true;
                    }
                }
            }

            if (gameRunning) {
                printTurnEnd(currentPlayer.getNome());
                turnQueue.enqueue(currentPlayer);
                pauseForHuman(currentPlayer);
            }
        }

        printGameEnd();
    }

    // ========== MOVIMENTO ==========

    private Divisao chooseDestination(Player player, ArrayUnorderedList<Divisao> neighbors) {
        if (player instanceof Bot) {
            Bot bot = (Bot) player;
            return bot.escolherMovimento();
        } else {
            return chooseDestinationHuman(neighbors);
        }
    }

    private Divisao chooseDestinationHuman(ArrayUnorderedList<Divisao> neighbors) {
        if (neighbors == null || neighbors.size() == 0) return null;

        int total = neighbors.size();
        Divisao[] options = new Divisao[total];
        int count = 0;

        Iterator<Divisao> it = neighbors.iterator();
        while (it.hasNext() && count < total) {
            Divisao neighbor = it.next();
            options[count] = neighbor;
            System.out.println("   [" + (count + 1) + "] Ir para: " + neighbor.getNome() + " (" + neighbor.getTipo() + ")");
            count++;
        }

        System.out.println("   [0] Parar");
        int choice;
        while (true) {
            System.out.print("Escolha: ");
            choice = readInteger();
            if (choice == 0) return null;
            if (choice > 0 && choice <= count) return options[choice - 1];
            System.out.println("Opção inválida. Introduza um número entre 0 e " + count + ".");
        }
    }

    private boolean checkCorridorEvent(Player player, Divisao destination) {
        EventoCorredor event = labyrinthGraph.getCorredorEvento(player.getLocalAtual(), destination);

        if (event.getTipo() == CorredorEvento.LOCKED) {
            int lockId = event.getValor();
            if (!player.podePassarTranca(lockId)) {
                System.out.println(" Portão trancado (Tranca #" + lockId + ").");
                System.out.println(" Ativa primeiro a Sala de Controlo #" + lockId + " com este jogador.");
                return false;
            } else {
                System.out.println(" Tranca #" + lockId + " já foi desbloqueada para " + player.getNome() + ". Passas.");
                return true;
            }
        }

        return true;
    }

    private boolean isTrapActivated(Player player, Divisao destination) {
        EventoCorredor event = labyrinthGraph.getCorredorEvento(player.getLocalAtual(), destination);
        if (event.getTipo() == CorredorEvento.BLOCK_TURN || event.getTipo() == CorredorEvento.MOVE_BACK) {
            System.out.println("Armadilha ativada! Turno encerrado.");
            return true;
        }
        return false;
    }

    // ========== ENIGMAS ==========

    private boolean presentAndSolveEnigma(Player player, Enigma enigma) {
        if (enigma == null) return true;

        System.out.println("\n ENIGMA NA PORTA!");
        System.out.println("P: " + enigma.getPergunta());

        boolean correct = false;
        int answerGiven = -1;

        if (player instanceof Bot) {
            Bot bot = (Bot) player;
            correct = bot.tentarResolverEnigma(enigma);
            answerGiven = correct ? 1 : 2; // Placeholder for bot answer
        } else {
            String[] options = enigma.getOpcoes();
            for (int i = 0; i < options.length; i++) {
                System.out.println("   (" + (i + 1) + ") " + options[i]);
            }
            int answer;
do {
    System.out.print("Resp: ");
    answer = readInteger();
    if (answer < 1 || answer > options.length) {
        System.out.println("Opção inválida. Introduza um número inteiro entre 1 e " + options.length + ".");
    }
} while (answer < 1 || answer > options.length);
correct = enigma.verificarResposta(answer);
            answerGiven = answer;
        }

        if (correct) {
            System.out.println("Correto! Podes passar.");
            totalEnigmasResolvidos++;
        } else {
            System.out.println("Errado! A porta fecha-se na tua cara.");
        }

        // Register enigma in player report
        GameReport.PlayerReport pReport = playerReports.get(player.getNome());
        if (pReport != null) {
            String answerStr = answerGiven >= 0 ? String.valueOf(answerGiven) : "Bot";
            GameReport.EnigmaEvent event = new GameReport.EnigmaEvent(
                enigma.getPergunta(),
                answerStr,
                correct,
                correct ? enigma.getEfeitoSucesso() : enigma.getEfeitoFalha(),
                player.getLocalAtual().getNome()
            );
            pReport.adicionarEnigma(event);
        }

        return correct;
    }

    private int applyEffect(String effect, Player player, LinkedQueue<Player> turnQueue) {
        if (effect == null || effect.equals("NONE")) return 0;

        System.out.println("EFEITO ATIVADO: " + effect);

        if (effect.equals("EXTRA_TURN")) {
            System.out.println("BÓNUS IMEDIATO! Ganhaste +1 movimento agora!");
            return 1;
        } else if (effect.equals("BLOCK")) {
            System.out.println("CASTIGO! Ficas bloqueado no próximo turno.");
            player.bloquear(1);
        } else if (effect.startsWith("BACK:")) {
            try {
                int steps = Integer.parseInt(effect.split(":")[1]);
                System.out.println("RECUAR! Voltas " + steps + " casas.");
                player.recuar(steps);
            } catch (Exception e) {
                // ignorar
            }
        } else if (effect.equals("SWAP")) {
            System.out.println("TROCA! Trocaste de lugar com o próximo jogador.");
            try {
                Player other = turnQueue.dequeue();
                if (other != player) {
                    Divisao posPlayer = player.getLocalAtual();
                    Divisao posOther = other.getLocalAtual();
                    player.setLocalAtual(posOther);
                    other.setLocalAtual(posPlayer);
                    System.out.println("   -> Trocaste com " + other.getNome());
                }
                turnQueue.enqueue(other);
            } catch (Exception e) {
                // ignorar
            }
        }

        return 0;
    }

    // ========== ALAVANCAS ==========

    private boolean solveLeverRoom(Player player, Divisao room) {
        if (room.getAlavanca() == null) {
            room.setAlavanca(new Alavanca());
        }

        Alavanca lever = room.getAlavanca();

        System.out.println("\nEsta sala tem 3 alavancas.");
        System.out.println("Uma abre O CAMINHO, outra penaliza, outra não faz nada.");
        System.out.println("[1] Alavanca 1");
        System.out.println("[2] Alavanca 2");
        System.out.println("[3] Alavanca 3");
        System.out.print("Escolhe 1, 2 ou 3: ");

        int choice;
if (player instanceof Bot) {
    choice = 1 + (int) (Math.random() * lever.getNumAlavancas());
    System.out.println("O bot escolhe a alavanca " + choice);
} else {
    do {
        choice = readInteger();
        if (choice < 1 || choice > 3) {
            System.out.println("Opção inválida. Escolha 1, 2 ou 3.");
        }
    } while (choice < 1 || choice > 3);
}

        AlavancaEnum result = lever.ativar(choice);

        switch (result) {
            case ABRIR_PORTA:
                int lockId = room.getIdDesbloqueio();
                if (lockId > 0) {
                    player.desbloquearTranca(lockId);
                    System.out.println("A alavanca certa! A tranca #" + lockId
                            + " foi desbloqueada para o jogador " + player.getNome() + ".");
                } else {
                    System.out.println("A alavanca certa! Ouves mecanismos ao longe...");
                }
                return false;

            case PENALIZAR:
                System.out.println("Era uma armadilha! Recuas 2 casas.");
                player.recuar(2);
                return true;

            case NADA:
            default:
                System.out.println("Nada acontece. Alavanca inútil.");
                return false;
        }
    }

    // ========== UTILITIES / INPUT ==========

    private int rollDiceAndGetMovements(Player player) {
        if (player instanceof Bot) {
            System.out.println("O Bot vai lançar os dados...");
            pauseForBot(player);
            int result = rollDice();
            System.out.println("O Bot rolou um " + result + "!");
            return result;
        } else {
            System.out.println("Pressiona ENTER para lançar o dado...");
            readString();
            int result = rollDice();
            System.out.println("ROLASTE UM " + result + "!");
            return result;
        }
    }

    private int rollDice() {
        return (int) (Math.random() * 6) + 1;
    }

    private int readInteger() {
    try {
        if (scanner.hasNextLine()) {
            return Integer.parseInt(scanner.nextLine());
        }
        return -1;
    } catch (Exception e) {
        return -1;
    }
}

    private String readString() {
    try {
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return "";
    } catch (Exception e) {
        return "";
    }
}

    private void waitForEnter() {
        System.out.println("(Enter para continuar...)");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }

    private void pauseForBot(Player player) {
        if (player instanceof Bot) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                // ignorar
            }
        }
    }

    private void pauseForHuman(Player player) {
        if (!(player instanceof Bot)) {
            waitForEnter();
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignorar
            }
        }
    }

    // ========== UI PRINTS ==========

    private void printTurnStart(Player player) {
        System.out.println("\n================================================");
        System.out.println("VEZ DE: " + player.getNome().toUpperCase());
        System.out.println("Local: " + player.getLocalAtual().getNome());
    }

    private void printPlayerBlocked(Player player) {
        System.out.println(player.getNome() + " está bloqueado!");
        System.out.println("Faltam " + player.getTurnosBloqueado() + " turno(s) de bloqueio.");
    }

    private void printExtraMovements(int extra, int total) {
        System.out.println("BÓNUS: tens " + extra + " movimento(s) extra acumulado(s)!");
        System.out.println("Total de movimentos neste turno: " + total);
    }

    private void printMovementStatus(Player player, int stepsLeft) {
        if (player instanceof Bot) {
            System.out.println("\n[Bot] Passos: " + stepsLeft + " | Local: " + player.getLocalAtual().getNome());
        } else {
            System.out.println("\n--- Passos Restantes: " + stepsLeft + " ---");
            System.out.println("Estás em: " + player.getLocalAtual().getNome());
        }
    }

    private void printTurnEnd(String playerName) {
        System.out.println("Fim do turno de " + playerName + ".");
    }

    private void printWinner(String playerName) {
        System.out.println("\nVENCEDOR: " + playerName + "! PARABÉNS!");
    }

    private void printGameEnd() {
        System.out.println("Obrigado por jogar!");
    }

    // ========== EFEITOS ESPECIAIS ==========

    /**
     * Aplica um efeito especial de um enigma (EXTRA_TURN, BLOCK, BACK, SWAP).
     * Retorna o número de movimentos extra ganhos (ou 0 se não houver).
     */
    private int aplicarEfeitoEnigma(String efeito, Player player, LinkedQueue<Player> turnQueue) {
        if (efeito == null || efeito.equals("NONE")) return 0;

        System.out.println("EFEITO ATIVADO: " + efeito);

        if (efeito.equals("EXTRA_TURN")) {
            System.out.println("BÓNUS IMEDIATO! Ganhaste +1 movimento agora!");
            return 1;
        }
        else if (efeito.equals("BLOCK")) {
            System.out.println("CASTIGO! Ficas bloqueado no próximo turno.");
            player.bloquear(1);
        }
        else if (efeito.startsWith("BACK:")) {
            try {
                int casas = Integer.parseInt(efeito.split(":")[1]);
                System.out.println("RECUAR! Voltas " + casas + " casas.");
                player.recuar(casas);
            } catch (Exception e) {}
        }
        else if (efeito.equals("SWAP")) {
            System.out.println("TROCA! Trocaste de lugar com o próximo jogador.");
            try {
                Player outro = turnQueue.dequeue();
                if (outro != player) {
                    Divisao posAtual = player.getLocalAtual();
                    Divisao posOutro = outro.getLocalAtual();

                    player.setLocalAtual(posOutro);
                    outro.setLocalAtual(posAtual);
                    System.out.println("   -> Trocaste com " + outro.getNome());
                }
                turnQueue.enqueue(outro);
            } catch(Exception e) {}
        }

        return 0;
    }

    /**
     * Trata a interação com uma sala de alavanca.
     * Retorna true se o turno deve acabar.
     */
    private boolean resolverSalaAlavanca(Player jogador, Divisao sala, LabyrinthGraph<Divisao> mapa) {
        if (sala.getAlavanca() == null) {
            sala.setAlavanca(new Alavanca());
        }

        Alavanca alavanca = sala.getAlavanca();

        System.out.println("\nEsta sala tem 3 alavancas.");
        System.out.println("Uma abre um mecanismo, outra penaliza, outra não faz nada.");
        System.out.println("[1] Alavanca 1");
        System.out.println("[2] Alavanca 2");
        System.out.println("[3] Alavanca 3");
        System.out.print("Escolhe 1, 2 ou 3: ");

        int escolha;
        if (jogador instanceof Bot) {
            escolha = 1 + (int)(Math.random() * alavanca.getNumAlavancas());
            System.out.println("O bot escolhe a alavanca " + escolha);
        } else {
            do {
                escolha = readInteger();
            } while (escolha < 1 || escolha > 3);
        }

        AlavancaEnum resultado = alavanca.ativar(escolha);

        switch (resultado) {
            case ABRIR_PORTA:
                int idTranca = sala.getIdDesbloqueio();
                if (idTranca > 0) {
                    jogador.desbloquearTranca(idTranca);
                    System.out.println("A alavanca certa! A tranca #" + idTranca
                            + " foi desbloqueada para o jogador " + jogador.getNome() + ".");
                } else {
                    System.out.println("A alavanca certa! Ouves mecanismos ao longe...");
                }
                return false;

            case PENALIZAR:
                System.out.println("Era uma armadilha! Recuas 2 casas.");
                jogador.recuar(2);
                return true;

            case NADA:
            default:
                System.out.println("Nada acontece. Alavanca inútil.");
                return false;
        }
    }

    /**
     * Creates a GameReport from the current game state.
     */
    private GameReport createGameReport(Player vencedorPlayer, Dificuldade difficulty) {
        GameReport report = new GameReport();
        report.setVencedor(vencedor);
        report.setDuracao(turnoCount);
        report.setDificuldade(difficulty.toString());
        report.setMapaNome("Mapa do Jogo");
        report.setTotalEnigmasResolvidos(totalEnigmasResolvidos);
        report.setTotalObstaculos(totalObstaculos);
        
        // Add all player reports
        for (GameReport.PlayerReport pReport : playerReports.values()) {
            report.adicionarJogador(pReport);
        }
        
        return report;
    }
}
