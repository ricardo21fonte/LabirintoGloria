package engine;

import java.util.Iterator;

import Lists.ArrayUnorderedList;
import enums.AlavancaEnum;
import enums.CorredorEvento;
import enums.Dificuldade;
import enums.TipoDivisao;
import enums.TipoEvento;
import game.Alavanca;
import game.Bot;
import game.Divisao;
import game.Enigma;
import game.EventoAleatorio;
import game.EventoCorredor;
import game.Player;
import graph.LabyrinthGraph;
import io.GameExporter;
import io.GameReport;
import ui.GameView;

/**
 * Core game engine that controls the main loop, player turns, movement,
 * puzzles enigmas, levers, traps and victory detection.
 */
public class GameEngine {

    /** View used for all console input/output. */
    private GameView view;

    /** Graph that represents the labyrinth structure. */
    private LabyrinthGraph<Divisao> graph;

    /** Name of the current map, used in reports. */
    private String nomeDoMapaEscolhido = "Mapa do Jogo";

    /** Manager that handles the players' turn order and rotation. */
    private TurnManager turnManager;

    /** List of all available riddles (enigmas) that can be used during the game. */
    private ArrayUnorderedList<Enigma> enigmasDisponiveis;

    /**
     * List of riddles that have already been shown to players.
     */
    private ArrayUnorderedList<Enigma> enigmasUsados;

    /** List with all players in the game eather human or bots. */
    private ArrayUnorderedList<Player> todosJogadores;

    /** Global difficulty level selected for the current game. */
    private Dificuldade dificuldade;
    /** Total number of turns played in the game. */
    private int turnoCount = 0;

    /** Total number of riddles successfully solved by all players. */
    private int totalResolvidos = 0;

    /** Total number of riddle attempts. */
    private int totalTentados = 0;

    /** Total number of obstacles (traps / corridor events) triggered. */
    private int totalObstaculos = 0;

    /**
     * Per-player report data, used to build the final GameReport
     */
    private ArrayUnorderedList<GameReport.PlayerReport> playerReports;
    /**
     * Creates a new GameEngine instance bound to a given
     * labyrinth graph and view.
     *
     * @param graph labyrinth structure where the game will take place
     * @param view  UI component responsible for user interaction
     */
    public GameEngine(LabyrinthGraph<Divisao> graph, GameView view) {
        this.graph = graph;
        this.view = view;
        this.playerReports = new ArrayUnorderedList<>();
        this.enigmasUsados = new ArrayUnorderedList<>();
    }
    /**
     * Sets the logical name of the map currently being played.
     * @param nome name of the map
     */
    public void setNomeDoMapa(String nome) {
        this.nomeDoMapaEscolhido = nome;
    }
    /**
     * Starts the game:
     */
    public void start() {
        GameInitializer init = new GameInitializer(view, graph);
        GameData dados = init.setupCompleto();

        if (dados == null) return;

        this.turnManager = new TurnManager(dados.turnQueue, view);
        this.todosJogadores = dados.todosJogadores;
        this.enigmasDisponiveis = dados.enigmasDisponiveis;
        this.dificuldade = dados.dificuldade;

        inicializarRelatorios();

        boolean jogoACorrer = true;
        while (jogoACorrer && turnManager.temJogadores()) {

            Player atual = turnManager.proximoJogador();
            if (atual == null) break;

            turnoCount++;
            view.mostrarInicioTurno(atual.getNome(), atual.getLocalAtual().getNome());

            // movimento
            int movimentos = atual.lancarDados(view);

            if (atual.getJogadasExtra() > 0) {
                int extra = atual.getJogadasExtra();
                movimentos += extra;
                atual.setJogadasExtra(0);
                view.mostrarBonusJogadas(extra, movimentos);
            }

            processarMovimento(atual, movimentos);

            if (verificarVitoria(atual)) {
                jogoACorrer = false;
            } else {
                view.mostrarFimTurno(atual.getNome());
                turnManager.fimDoTurno(atual);
            }
        }

        view.mostrarFimJogo();
    }
    /**
     * Processes all movement steps for the given player in the current turn.
     * @param player     player whose turn is being processed
     * @param movimentos number of movement points (steps) available
     * @return true if the turn should end immediately
     */
    private boolean processarMovimento(Player player, int movimentos) {
        while (movimentos > 0) {
            view.mostrarStatusMovimento(player instanceof Bot, movimentos, player.getLocalAtual().getNome());

            Divisao destino = player.escolherDestino(graph.getVizinhos(player.getLocalAtual()), view);
            if (destino == null) return true;

            if (destino.getTipo() == TipoDivisao.SALA_ENIGMA) {
                if (!processarEnigma(player)) {
                    return true;
                }
            }

            if (!podeEntrar(player, destino)) continue;


            Divisao origem = player.getLocalAtual();
            player.moverPara(destino);
            registarRelatorioMovimento(player, destino);

            if (destino.getTipo() == TipoDivisao.SALA_CENTRAL) return true;
            if (verificarArmadilha(player, origem, destino)) return true;

            EventoCorredor evCorredor = graph.getCorredorEvento(origem, destino);
            if (evCorredor.getTipo() == CorredorEvento.NONE) {

                if (Math.random() < 0.25) {
                    EventoAleatorio evento = EventoAleatorio.gerarAleatorio();

                    if (evento.getTipo() != TipoEvento.SEM_EVENTO) {

                        System.out.print("Evento Aleatorio: ");
                        System.out.println(evento.getDescricao());

                        evento.aplicar(player, todosJogadores, view);

                        if (evento.getTipo() == TipoEvento.RECUAR || evento.getTipo() == TipoEvento.BLOQUEAR_TURNOS) {
                            return true;
                        }
                    }
                }
            }

            movimentos--;

            // Alavancas
            if (destino.getTipo() == TipoDivisao.SALA_ALAVANCA) {
                if (processarAlavanca(player, destino)) return true;
            }
        }
        return false;
    }
    /**
     * Handles the entire riddle (enigma) process for a player entering
     * @param player player that is facing the riddle
     * @return true if the player answered correctly
     */
    private boolean processarEnigma(Player player) {
        Enigma enigma = obterEnigma();

        if (enigma == null) {
            view.mostrarMensagemCarregar();
            return true;
        }

        totalTentados++;
        boolean acertou = player.resolverEnigma(enigma, view);

        if (acertou) {
            totalResolvidos++;
            applyEffect(enigma.getEfeitoSucesso(), player);
        } else {
            applyEffect(enigma.getEfeitoFalha(), player);
        }
        return acertou;
    }
    /**
     * Handles the interaction with a lever room TipoDivisao.
     * @param player player interacting with the lever room
     * @param sala   room that contains the lever puzzle
     * @return true if the player's turn should end after this
     */
    private boolean processarAlavanca(Player player, Divisao sala) {
        if (sala.getAlavanca() == null) sala.setAlavanca(new Alavanca());

        int escolha = player.decidirAlavanca(sala, view);

        AlavancaEnum resultado = sala.getAlavanca().ativar(escolha);
        view.mostrarResultadoAlavanca(resultado, sala.getIdDesbloqueio(), player.getNome());

        if (resultado == AlavancaEnum.ABRIR_PORTA) {
            player.desbloquearTranca(sala.getIdDesbloqueio());
            return false;
        } else if (resultado == AlavancaEnum.PENALIZAR) {
            player.recuar(2, view);
            return true;
        }
        return false;
    }
    /**
     * Obtains a riddle from the available list
     * @return a non-used Enigma
     */
    private Enigma obterEnigma() {
        if (enigmasDisponiveis == null || enigmasDisponiveis.isEmpty()) {

            if (enigmasUsados != null && !enigmasUsados.isEmpty()) {
                System.out.println("A recarregar enigmas ja respondidos...");
                while (!enigmasUsados.isEmpty()) {
                    enigmasDisponiveis.addToRear(enigmasUsados.removeFirst());
                }
            } else {
                return null;
            }
        }

        Enigma e = enigmasDisponiveis.removeFirst();

        enigmasUsados.addToRear(e);

        return e;
    }
    /**
     * Applies a game effect string to a player.
     * @param effect effect code to apply
     * @param p      target player
     */
    private void applyEffect(String effect, Player p) {
        if (effect == null || effect.equals("NONE")) return;
        view.mostrarEfeito(effect);

        if (effect.equals("EXTRA_TURN")) {
            p.adicionarJogadasExtras(1);
        } else if (effect.startsWith("BACK:")) {
            try {
                String[] partes = effect.split(":");
                if (partes.length > 1) {
                    int steps = Integer.parseInt(partes[1]);
                    p.recuar(steps, view);
                } else {
                    view.mostrarErro("Efeito 'BACK' mal formatado no ficheiro JSON.");
                }
            } catch (NumberFormatException e) {
                view.mostrarErro("O valor de recuo no enigma nao e um numero valido.");
            }
        }else if (effect.equals("BLOCK")) {
            p.bloquear(1);
        }
    }
    /**
     * Checks whether the given player has won the game by reaching the treasure room.
     * @param p player to check
     * @return true if the player has reached the treasure room,
     */
    private boolean verificarVitoria(Player p) {
        if (p.getLocalAtual().getTipo() == TipoDivisao.SALA_CENTRAL) {
            view.mostrarVencedor(p.getNome());

            Iterator<GameReport.PlayerReport> it = playerReports.iterator();
            while(it.hasNext()) {
                GameReport.PlayerReport pr = it.next();

                pr.setTurnosJogados(turnoCount);

                if (pr.getNome().equals(p.getNome())) {
                    pr.setVencedor(true);
                    pr.setLocalAtual(p.getLocalAtual().getNome());
                }
            }

            GameReport report = new GameReport();
            report.setVencedor(p.getNome());
            report.setDuracao(turnoCount);
            report.setMapaNome(nomeDoMapaEscolhido);

            if (dificuldade != null) {
                report.setDificuldade(dificuldade.toString());
            } else {
                report.setDificuldade("DESCONHECIDA");
            }

            report.setListaJogadores(playerReports);
            report.setTotalEnigmasResolvidos(totalResolvidos);
            report.setTotalEnigmasTentados(totalTentados);
            report.setTotalObstaculos(totalObstaculos);

            //Exportar
            GameExporter exporter = new GameExporter();
            exporter.exportarJogo(report);

            return true;
        }
        return false;
    }
    /**
     * Initializes the per-player report list from the current list of players.
     */
    private void inicializarRelatorios() {
        Iterator<Player> it = todosJogadores.iterator();
        while(it.hasNext()) {
            Player p = it.next();
            playerReports.addToRear(new GameReport.PlayerReport(p.getNome(), p instanceof Bot ? "Bot" : "Humano"));
        }
    }
    /**
     * Registers a movement in the report for a given player and destination.
     * @param p player that moved
     * @param d destination room
     */
    private void registarRelatorioMovimento(Player p, Divisao d) {
        Iterator<GameReport.PlayerReport> it = playerReports.iterator();
        while(it.hasNext()) {
            GameReport.PlayerReport pr = it.next();
            if(pr.getNome().equals(p.getNome())) {
                pr.adicionarPercurso(d.getNome());
                pr.setLocalAtual(d.getNome());
                break;
            }
        }
    }

    /**
     * Checks whether the player is allowed to enter a target room considering
     * the corridor event between the current room and the destination.
     * @param p player attempting to move
     * @param d destination room
     * @return true if the player can enter
     */
    private boolean podeEntrar(Player p, Divisao d) {
        EventoCorredor ev = graph.getCorredorEvento(p.getLocalAtual(), d);
        if (ev.getTipo() == CorredorEvento.LOCKED) {
            int key = ev.getValor();
            if (!p.podePassarTranca(key)) {
                view.mostrarPortaoTrancado(key);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks and applies edge traps between two rooms.
     * @param p player moving through the corridor
     * @param o origin room
     * @param d destination room
     * @return true if a trap was activated and the turn should end
     */
    private boolean verificarArmadilha(Player p, Divisao o, Divisao d) {
        EventoCorredor ev = graph.getCorredorEvento(o, d);

        if (ev.getTipo() == CorredorEvento.BLOCK_TURN || ev.getTipo() == CorredorEvento.MOVE_BACK) {
            view.mostrarArmadilhaAtivada();
            totalObstaculos++;

            if (ev.getTipo() == CorredorEvento.BLOCK_TURN) {
                p.bloquear(ev.getValor());
            }
            if (ev.getTipo() == CorredorEvento.MOVE_BACK) {
                p.recuar(ev.getValor(), view);
            }
            graph.relocalizarArmadilha(o, d);

            // Regista no relatorio
            Iterator<GameReport.PlayerReport> it = playerReports.iterator();
            while(it.hasNext()) {
                GameReport.PlayerReport pr = it.next();
                if(pr.getNome().equals(p.getNome())) {
                    pr.adicionarObstaculo("Armadilha em " + d.getNome());
                    break;
                }
            }

            return true;
        }
        return false;
    }
}
