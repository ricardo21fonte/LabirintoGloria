package app;

import java.util.Iterator;

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
import io.EnigmaLoader;
import io.GameExporter;
import io.GameReport;

public class GameEngine {

    private GameView view;
    private LabyrinthGraph<Divisao> labyrinthGraph;
    private String vencedor;
    private int turnoCount;
    private ArrayUnorderedList<GameReport.PlayerReport> playerReports;
    
    // --- VARIÁVEIS DE ESTATÍSTICA ---
    private int totalEnigmasResolvidos;
    private int totalEnigmasTentados; 
    private int totalObstaculos;
    private String nomeDoMapaEscolhido = "Mapa do Jogo";

    // --- LISTA DE ENIGMAS (Trazida do antigo labirinto.java) ---
    private ArrayUnorderedList<Enigma> enigmasDisponiveis;

    public GameEngine(LabyrinthGraph<Divisao> labyrinthGraph) {
        this.labyrinthGraph = labyrinthGraph;
        this.view = new GameView(); 
    }
    
    public void setNomeDoMapa(String nome) {
        this.nomeDoMapaEscolhido = nome;
    }

    public void start() {
        view.mostrarMensagemCarregar();
        
        // 1. Carregar e guardar enigmas localmente
        ArrayUnorderedList<Enigma> allEnigmas = loadEnigmas();
        
        Dificuldade difficulty = setupDifficulty();

        // 2. Filtrar enigmas para a dificuldade escolhida
        this.enigmasDisponiveis = filterEnigmasByDifficulty(allEnigmas, difficulty);

        Divisao[] entrances = getMapEntrances();
        if (entrances.length == 0) return;

        LinkedQueue<Player> turnQueue = new LinkedQueue<>();

        // 3. Setup dos jogadores (já não usa labyrinthGame.adicionarJogador)
        int numHumans = setupHumanPlayers(entrances, turnQueue);
        setupBots(entrances, turnQueue, numHumans);

        if (turnQueue.isEmpty()) {
            view.mostrarSemJogadores();
            return;
        }

        view.mostrarInicioJogo();
        view.esperarEnter();

        // Inicializar Estado
        vencedor = null;
        turnoCount = 0;
        playerReports = new ArrayUnorderedList<>();
        
        // Resetar contadores
        totalEnigmasResolvidos = 0;
        totalEnigmasTentados = 0; 
        totalObstaculos = 0;
        
        // Criar relatórios iniciais
        LinkedQueue<Player> tempQueue = new LinkedQueue<>();
        while (!turnQueue.isEmpty()) {
            try {
                Player p = turnQueue.dequeue();
                tempQueue.enqueue(p);
                GameReport.PlayerReport pReport = new GameReport.PlayerReport(p.getNome(), p instanceof Bot ? "Bot" : "Humano");
                playerReports.addToRear(pReport);
            } catch (Exception e) { break; }
        }
        while (!tempQueue.isEmpty()) {
            try { turnQueue.enqueue(tempQueue.dequeue()); } catch (Exception e) { break; }
        }

        runGameLoop(turnQueue, difficulty);

        view.fechar();
    }

    // ========== SETUP ==========

    private ArrayUnorderedList<Enigma> loadEnigmas() {
        EnigmaLoader enigmaLoader = new EnigmaLoader();
        return enigmaLoader.loadEnigmas("resources/enigmas/enigmas.json");
    }

    private Dificuldade setupDifficulty() {
        int option = -1;
        do {
            option = view.pedirDificuldade();
            if (option < 1 || option > 3) view.mostrarErroOpcaoInvalida(1, 3);
        } while (option < 1 || option > 3);
        if (option == 1) return Dificuldade.FACIL;
        if (option == 2) return Dificuldade.MEDIO;
        return Dificuldade.DIFICIL;
    }

    private ArrayUnorderedList<Enigma> filterEnigmasByDifficulty(ArrayUnorderedList<Enigma> allEnigmas, Dificuldade difficulty) {
        ArrayUnorderedList<Enigma> filtered = new ArrayUnorderedList<>();
        Iterator<Enigma> it = allEnigmas.iterator();
        while (it.hasNext()) {
            Enigma enigma = it.next();
            if (enigma.getDificuldade() == difficulty) filtered.addToRear(enigma);
        }
        view.mostrarDificuldadeDefinida(difficulty.toString(), filtered.size());
        return filtered;
    }

    private Divisao[] getMapEntrances() {
        ArrayUnorderedList<Divisao> entrances = new ArrayUnorderedList<>();
        Object[] vertices = labyrinthGraph.getVertices();
        for (Object obj : vertices) {
            Divisao division = (Divisao) obj;
            if (division.getTipo() == TipoDivisao.ENTRADA) entrances.addToRear(division);
        }
        if (entrances.isEmpty()) {
            view.mostrarErroSemEntradas();
            return new Divisao[0];
        }
        Divisao[] arrayEntrances = new Divisao[entrances.size()];
        Iterator<Divisao> it = entrances.iterator();
        int idx = 0;
        while (it.hasNext()) arrayEntrances[idx++] = it.next();
        return arrayEntrances;
    }

    private int setupHumanPlayers(Divisao[] entrances, LinkedQueue<Player> turnQueue) {
        int numHumans;
        int maxPlayers = 8;
        do {
            numHumans = view.pedirQuantidadeHumanos(maxPlayers);
            if (numHumans < 0 || numHumans > maxPlayers) view.mostrarErroOpcaoInvalida(0, maxPlayers);
        } while (numHumans < 0 || numHumans > maxPlayers);

        for (int i = 1; i <= numHumans; i++) {
            String name;
            do {
                name = view.pedirNomeJogador(i);
                if (name.isEmpty()) System.out.println("Nome inválido.");
            } while (name.isEmpty());
            int rnd = (int) (Math.random() * entrances.length);
            Divisao spawn = entrances[rnd];
            Player player = new Player(name, spawn);
            turnQueue.enqueue(player);
            view.mostrarSpawn(spawn.getNome());
        }
        return numHumans;
    }

    private void setupBots(Divisao[] entrances, LinkedQueue<Player> turnQueue, int numHumans) {
        int maxBots = 8 - numHumans;
        if (maxBots <= 0) {
            view.mostrarAvisoJogoCheio();
            return;
        }
        int numBots;
        do {
            numBots = view.pedirQuantidadeBots(maxBots);
            if (numBots < 0 || numBots > maxBots) view.mostrarErroOpcaoInvalida(0, maxBots);
        } while (numBots < 0 || numBots > maxBots);

        for (int i = 1; i <= numBots; i++) {
            int rnd = (int) (Math.random() * entrances.length);
            Divisao spawn = entrances[rnd];
            int opt = -1;
            do {
                opt = view.pedirDificuldadeBot(i);
                if(opt < 1 || opt > 3) view.mostrarErroOpcaoInvalida(1, 3);
            } while(opt < 1 || opt > 3);
            Dificuldade dif = (opt == 2) ? Dificuldade.MEDIO : (opt == 3) ? Dificuldade.DIFICIL : Dificuldade.FACIL;
            Bot bot = new Bot("Bot_" + i, spawn, dif, labyrinthGraph);
            turnQueue.enqueue(bot);
            view.mostrarBotCriado(spawn.getNome());
        }
    }

    // ========== GAME LOOP ==========

    private void runGameLoop(LinkedQueue<Player> turnQueue, Dificuldade difficulty) {
        boolean gameRunning = true;
        while (gameRunning && !turnQueue.isEmpty()) {
            Player currentPlayer;
            try { currentPlayer = turnQueue.dequeue(); } catch (Exception e) { break; }
            
            turnoCount++;
            view.mostrarInicioTurno(currentPlayer.getNome(), currentPlayer.getLocalAtual().getNome());

            if (currentPlayer.isBloqueado()) {
                view.mostrarBloqueado(currentPlayer.getNome(), currentPlayer.getTurnosBloqueado());
                currentPlayer.consumirUmTurnoBloqueado();
                turnQueue.enqueue(currentPlayer);
                pauseForHuman(currentPlayer);
                continue;
            }

            int movements = rollDiceAndGetMovements(currentPlayer);
            if (currentPlayer.getJogadasExtra() > 0) {
                int extra = currentPlayer.getJogadasExtra();
                movements += extra;
                currentPlayer.setJogadasExtra(0);
                view.mostrarBonusJogadas(extra, movements);
            }

            boolean turnEnded = false;
            while (movements > 0 && !turnEnded) {
                pauseForBot(currentPlayer);
                view.mostrarStatusMovimento(currentPlayer instanceof Bot, movements, currentPlayer.getLocalAtual().getNome());

                ArrayUnorderedList<Divisao> neighbors = labyrinthGraph.getVizinhos(currentPlayer.getLocalAtual());
                Divisao destination = chooseDestination(currentPlayer, neighbors);
                if (destination == null) break;

                boolean canEnter = true;
                
                // --- LÓGICA DE ENIGMA INTEGRADA ---
                if (destination.getTipo() == TipoDivisao.SALA_ENIGMA) {
                    Enigma enigma = obterEnigmaAleatorio(difficulty);
                    
                    if (enigma == null) {
                        view.mostrarMensagemCarregar(); 
                        canEnter = true;
                    } else {
                        boolean solved = presentAndSolveEnigma(currentPlayer, enigma);
                        if (solved) {
                            int bonus = applyEffect(enigma.getEfeitoSucesso(), currentPlayer, turnQueue);
                            if (bonus > 0) {
                                movements += bonus;
                                view.mostrarGanhouMovimentos(movements);
                            }
                        } else {
                            canEnter = false;
                            turnEnded = true;
                            applyEffect(enigma.getEfeitoFalha(), currentPlayer, turnQueue);
                        }
                    }
                }

                if (canEnter) {
                    Divisao salaOrigem = currentPlayer.getLocalAtual();
                    if (!checkCorridorEvent(currentPlayer, destination)) {
                        GameReport.PlayerReport pReport = getPlayerReport(currentPlayer.getNome());
                        if (pReport != null) {
                            pReport.adicionarObstaculo("Tranca ou bloqueio em " + destination.getNome());
                            totalObstaculos++;
                        }
                        continue;
                    }

                    // --- MOVIMENTO REAL DO JOGADOR (Única vez que mexe!) ---
                    currentPlayer.moverPara(destination);
                    
                    GameReport.PlayerReport pReport = getPlayerReport(currentPlayer.getNome());
                    if (pReport != null) pReport.adicionarPercurso(destination.getNome());

                    if (isTrapActivated(salaOrigem, destination)) {
                        if (pReport != null) {
                            pReport.adicionarObstaculo("Armadilha em " + destination.getNome());
                            totalObstaculos++;
                        }
                        turnEnded = true;
                    }

                    movements--;

                    if (!turnEnded && destination.getTipo() == TipoDivisao.SALA_ALAVANCA) {
                        if (solveLeverRoom(currentPlayer, destination)) turnEnded = true;
                    }

                    if (currentPlayer.getLocalAtual().getTipo() == TipoDivisao.SALA_CENTRAL) {
                        view.mostrarVencedor(currentPlayer.getNome());
                        vencedor = currentPlayer.getNome();
                        
                        if (pReport != null) {
                            pReport.setVencedor(true);
                            pReport.setLocalAtual(currentPlayer.getLocalAtual().getNome());
                            pReport.setTurnosJogados(turnoCount);
                        }
                        
                        GameReport report = createGameReport(currentPlayer, difficulty);
                        GameExporter exporter = new GameExporter();
                        exporter.exportarJogo(report);
                        
                        gameRunning = false;
                        turnEnded = true;
                    }
                }
            }
            if (gameRunning) {
                view.mostrarFimTurno(currentPlayer.getNome());
                turnQueue.enqueue(currentPlayer);
                pauseForHuman(currentPlayer);
            }
        }
        view.mostrarFimJogo();
    }

    // === MÉTODOS DE LÓGICA E CONTAGEM ===
    
    // Método trazido do labirinto.java para escolher enigmas
    private Enigma obterEnigmaAleatorio(Dificuldade difAlvo) {
        if (enigmasDisponiveis == null || enigmasDisponiveis.isEmpty()) return null;

        ArrayUnorderedList<Enigma> candidatos = new ArrayUnorderedList<>();
        Iterator<Enigma> it = enigmasDisponiveis.iterator();
        while (it.hasNext()) {
            Enigma e = it.next();
            if (e.getDificuldade() == difAlvo) {
                candidatos.addToRear(e);
            }
        }

        if (candidatos.isEmpty()) return null;

        int totalCandidatos = candidatos.size();
        int indiceSorteado = (int) (Math.random() * totalCandidatos);

        Enigma enigmaEscolhido = null;
        Iterator<Enigma> itCandidatos = candidatos.iterator();
        int idx = 0;
        while (itCandidatos.hasNext()) {
            Enigma e = itCandidatos.next();
            if (idx == indiceSorteado) {
                enigmaEscolhido = e;
                break;
            }
            idx++;
        }

        if (enigmaEscolhido != null) {
            enigmasDisponiveis.remove(enigmaEscolhido);
        }
        return enigmaEscolhido;
    }

    private boolean presentAndSolveEnigma(Player player, Enigma enigma) {
        if (enigma == null) return true;
        
        totalEnigmasTentados++; 

        view.mostrarEnigmaNaPorta();
        view.mostrarPergunta(enigma.getPergunta());
        boolean correct = false;
        String answerStr = "Bot";
        if (player instanceof Bot) {
            Bot bot = (Bot) player;
            view.mostrarBotAnalisaEnigma(bot.getNome(), bot.getInteligencia().toString());
            correct = bot.tentarResolverEnigma(enigma);
            answerStr = correct ? "Correto" : "Errado";
        } else {
            String[] options = enigma.getOpcoes();
            view.mostrarOpcoesEnigma(options);
            int answer;
            do {
                answer = view.pedirRespostaEnigma();
                if (answer < 1 || answer > options.length) view.mostrarErroOpcaoInvalida(1, options.length);
            } while (answer < 1 || answer > options.length);
            correct = enigma.verificarResposta(answer);
            answerStr = String.valueOf(answer);
        }
        view.mostrarResultadoEnigma(correct);
        
        if (correct) totalEnigmasResolvidos++; 

        GameReport.PlayerReport pReport = getPlayerReport(player.getNome());
        if (pReport != null) {
            GameReport.EnigmaEvent event = new GameReport.EnigmaEvent(
                enigma.getPergunta(), answerStr, correct,
                correct ? enigma.getEfeitoSucesso() : enigma.getEfeitoFalha(),
                player.getLocalAtual().getNome()
            );
            pReport.adicionarEnigma(event);
        }
        return correct;
    }

    private int applyEffect(String effect, Player player, LinkedQueue<Player> turnQueue) {
        if (effect == null || effect.equals("NONE")) return 0;
        view.mostrarEfeito(effect);
        if (effect.equals("EXTRA_TURN")) return 1;
        else if (effect.equals("BLOCK")) player.bloquear(1);
        else if (effect.startsWith("BACK:")) {
            try {
                int steps = Integer.parseInt(effect.split(":")[1]);
                player.recuar(steps);
            } catch (Exception e) {}
        } else if (effect.equals("SWAP")) {
            try {
                Player other = turnQueue.dequeue();
                if (other != player) {
                    Divisao pos1 = player.getLocalAtual();
                    Divisao pos2 = other.getLocalAtual();
                    player.setLocalAtual(pos2);
                    other.setLocalAtual(pos1);
                }
                turnQueue.enqueue(other);
            } catch (Exception e) {}
        }
        return 0;
    }

    private boolean solveLeverRoom(Player player, Divisao room) {
        if (room.getAlavanca() == null) room.setAlavanca(new Alavanca());
        Alavanca lever = room.getAlavanca();
        view.mostrarSalaAlavanca();
        view.mostrarOpcoesAlavanca();
        int choice;
        if (player instanceof Bot) {
            Bot bot = (Bot) player;
            choice = bot.escolherAlavanca(room, lever.getNumAlavancas()); // Inteligência do Bot
            view.mostrarBotEscolheAlavanca(choice);
        } else {
            do {
                choice = view.pedirAlavanca();
                if(choice < 1 || choice > 3) view.mostrarErroOpcaoInvalida(1, 3);
            } while(choice < 1 || choice > 3);
        }
        AlavancaEnum result = lever.ativar(choice);
        int lockId = room.getIdDesbloqueio();
        view.mostrarResultadoAlavanca(result, lockId, player.getNome());
        if (result == AlavancaEnum.ABRIR_PORTA && lockId > 0) {
            player.desbloquearTranca(lockId);
            return false;
        } else if (result == AlavancaEnum.PENALIZAR) {
            player.recuar(2);
            return true;
        }
        return false;
    }

    private GameReport createGameReport(Player vencedorPlayer, Dificuldade difficulty) {
        GameReport report = new GameReport();
        report.setVencedor(vencedor);
        report.setDuracao(turnoCount);
        report.setDificuldade(difficulty.toString());
        report.setMapaNome(nomeDoMapaEscolhido);
        report.setTotalEnigmasResolvidos(totalEnigmasResolvidos);
        report.setTotalEnigmasTentados(totalEnigmasTentados);
        report.setTotalObstaculos(totalObstaculos);
        report.setListaJogadores(playerReports);
        return report;
    }

    private GameReport.PlayerReport getPlayerReport(String nome) {
        Iterator<GameReport.PlayerReport> it = playerReports.iterator();
        while (it.hasNext()) {
            GameReport.PlayerReport pr = it.next();
            if (pr.getNome().equals(nome)) return pr;
        }
        return null;
    }

    private int rollDiceAndGetMovements(Player player) {
        if (player instanceof Bot) {
            view.avisarBotLancaDados();
            pauseForBot(player);
            int val = (int) (Math.random() * 6) + 1;
            view.mostrarResultadoDados(true, val);
            return val;
        } else {
            view.pedirHumanoLancaDados();
            int val = (int) (Math.random() * 6) + 1;
            view.mostrarResultadoDados(false, val);
            return val;
        }
    }

    private Divisao chooseDestination(Player player, ArrayUnorderedList<Divisao> neighbors) {
        if (player instanceof Bot) {
            Bot bot = (Bot) player;
            Divisao dest = bot.escolherMovimento();
            if(dest != null) view.mostrarBotDecisao(dest.getNome());
            return dest;
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
            Divisao v = it.next();
            options[count] = v;
            view.mostrarOpcaoMovimento(count + 1, v.getNome(), v.getTipo().toString());
            count++;
        }
        view.mostrarOpcaoParar();
        int choice;
        while (true) {
            choice = view.pedirEscolhaMovimento();
            if (choice == 0) return null;
            if (choice > 0 && choice <= count) return options[choice - 1];
            view.mostrarErroOpcaoInvalida(0, count);
        }
    }

    private boolean checkCorridorEvent(Player player, Divisao destination) {
        EventoCorredor event = labyrinthGraph.getCorredorEvento(player.getLocalAtual(), destination);
        if (event.getTipo() == CorredorEvento.LOCKED) {
            int lockId = event.getValor();
            if (!player.podePassarTranca(lockId)) {
                view.mostrarPortaoTrancado(lockId);
                return false;
            } else {
                view.mostrarTrancaJaAberta(lockId, player.getNome());
                return true;
            }
        }
        return true;
    }

    private boolean isTrapActivated(Divisao origem, Divisao destination) {
        EventoCorredor event = labyrinthGraph.getCorredorEvento(origem, destination);
        if (event.getTipo() == CorredorEvento.BLOCK_TURN || event.getTipo() == CorredorEvento.MOVE_BACK) {
            view.mostrarArmadilhaAtivada();
            labyrinthGraph.relocalizarArmadilha(origem, destination);
            return true;
        }
        return false;
    }

    private void pauseForBot(Player player) {
        if (player instanceof Bot) {
            try { Thread.sleep(1500); } catch (InterruptedException e) {}
        }
    }

    private void pauseForHuman(Player player) {
        if (!(player instanceof Bot)) {
            view.esperarEnter();
        } else {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
    }
}