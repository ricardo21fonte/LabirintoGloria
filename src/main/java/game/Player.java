package game;

import java.util.Iterator;

import Exceptions.EmptyCollectionException;
import Lists.ArrayUnorderedList;
import Lists.UnorderedLinkedList;
import Stacks.LinkedStack;
import ui.GameView;

/**
 * Represents a player in the labyrinth.
 */
public class Player {
    /**
     * Player's string name.
     */
    private String nome;

    /**
     * Current division where the player is located.
     */
    private Divisao localAtual;

    /**
     * Movement and action history log for this player.
     */
    private UnorderedLinkedList<String> historico;

    /**
     * Number of extra moves the player has accumulated.
     */
    private int jogadasExtra;

    /**
     * Number of turns during which the player is blocked.
     */
    private int turnosBloqueado;

    /**
     * Stack representing the path the player has taken through the labyrinth.
     */
    private LinkedStack<Divisao> caminho;

    /**
     * Minimum allowed size of the path stack for back positions operations.
     */
    private int limiteRecuoMinSize;

    /**
     * List of IDs representing locks that this player has already unlocked.
     */
    private ArrayUnorderedList<Integer> trancasDesbloqueadas;

    /**
     * Creates a new player with the given name and starting division.
     * @param nome   the player's name
     * @param inicio the starting division for the player
     */
    public Player(String nome, Divisao inicio) {
        this.nome = nome;
        this.localAtual = inicio;
        this.historico = new UnorderedLinkedList<>();
        this.jogadasExtra = 0;
        this.turnosBloqueado = 0;
        this.caminho = new LinkedStack<>();
        caminho.push(inicio);
        this.limiteRecuoMinSize = 1;
        this.trancasDesbloqueadas = new ArrayUnorderedList<>();
        historico.addToRear("Inicio: " + inicio.getNome());
    }

    /**
     * Human behaviour for rolling the dice.
     * @param view the game view used to display prompts and results
     * @return the dice value in the range 1 to 6
     */
    public int lancarDados(GameView view) {
        view.pedirHumanoLancaDados();
        int val = (int)(Math.random() * 6) + 1;
        view.mostrarResultadoDados(false, val);
        return val;
    }

    /**
     * Human behaviour for choosing a destination.
     * @param vizinhos list of neighbouring divisions
     * @param view     the game view used to display options and read input
     * @return the chosen Divisao
     */
    public Divisao escolherDestino(ArrayUnorderedList<Divisao> vizinhos, GameView view) {
        if (vizinhos == null || vizinhos.isEmpty()) return null;

        Divisao[] opcoes = new Divisao[vizinhos.size()];
        int i = 0;
        Iterator<Divisao> it = vizinhos.iterator();
        while (it.hasNext()) {
            Divisao v = it.next();
            opcoes[i] = v;
            view.mostrarOpcaoMovimento(i + 1, v.getNome(), v.getTipo().toString());
            i++;
        }
        view.mostrarOpcaoParar();

        while (true) {
            int escolha = view.pedirEscolhaMovimento();
            if (escolha == 0) return null;
            if (escolha > 0 && escolha <= vizinhos.size()) {
                return opcoes[escolha - 1];
            }
            view.mostrarErroOpcaoInvalida(0, vizinhos.size());
        }
    }

    /**
     * Human behaviour for solving a riddle.
     * @param enigma the riddle to solve
     * @param view   the game view used to display the riddle and read input
     * @return true if the player answers correctly, or false otherwise
     */
    public boolean resolverEnigma(Enigma enigma, GameView view) {
        view.mostrarPergunta(enigma.getPergunta());
        String[] opcoes = enigma.getOpcoes();
        view.mostrarOpcoesEnigma(opcoes);

        int resposta;
        do {
            resposta = view.pedirRespostaEnigma();
            if (resposta < 1 || resposta > opcoes.length) {
                view.mostrarErroOpcaoInvalida(1, opcoes.length);
            }
        } while (resposta < 1 || resposta > opcoes.length);

        boolean acertou = enigma.verificarResposta(resposta);
        view.mostrarResultadoEnigma(acertou);
        return acertou;
    }


    /**
     * Human behaviour for deciding which lever to pull in a lever room.
     * @param sala the lever room
     * @param view the game view used to display options and read input
     * @return the chosen lever index
     */
    public int decidirAlavanca(Divisao sala, GameView view) {
        view.mostrarSalaAlavanca();
        view.mostrarOpcoesAlavanca();
        
        int escolha;
        do {
            escolha = view.pedirAlavanca();
            if (escolha < 1 || escolha > 3) {
                view.mostrarErroOpcaoInvalida(1, 3);
            }
        } while (escolha < 1 || escolha > 3);
        
        return escolha;
    }

    /**
     * Moves the player to the given room
     * @param novaSala the new division to move to
     */
    public void moverPara(Divisao novaSala) {
        this.localAtual = novaSala;
        historico.addToRear("Moveu para: " + novaSala.getNome());
        caminho.push(novaSala);
    }
    /**
     * Moves the player backwards along the path by a given number of steps.
     * @param casas number of steps to move back
     * @param view  the view used to display feedback about the recoil action
     */
    public void recuar(int casas, GameView view) {
        Divisao posicaoInicial = getLocalAtual();
        int passosRealizados = 0;

        if (caminho == null || caminho.size() <= limiteRecuoMinSize) {
            view.mostrarAvisoSemRecuo(this.nome);
            return;
        }

        int passos = casas;
        while (passos > 0 && caminho.size() > limiteRecuoMinSize) {
            try {
                caminho.pop();
                passosRealizados++;
            } catch (EmptyCollectionException e) {
                break;
            }
            passos--;
        }

        try {
            Divisao novaPosicao = caminho.peek();
            this.localAtual = novaPosicao;
            historico.addToRear("Recuou para: " + novaPosicao.getNome());
            view.mostrarRecuo(this.nome, casas, novaPosicao.getNome());

        } catch (EmptyCollectionException e) {
            this.localAtual = posicaoInicial;
            view.mostrarAvisoSemRecuo(this.nome);
        }
    }
    /**
     * Increases the number of turns this player will remain blocked.
     * @param turnos number of turns to add to the blocked counter
     */
    public void bloquear(int turnos) {
        this.turnosBloqueado += turnos;
    }
    /**
     * Sets the current room of the player and pushes it onto the path stack
     * @param novaSala the new current division
     */
    public void setLocalAtual(Divisao novaSala) {
        this.localAtual = novaSala;
        if (caminho == null) caminho = new LinkedStack<>();
        caminho.push(novaSala);
    }
    /**
     * Marks the current size of the path stack as the minimum allowed for future "recuar" operations.
     */
    public void marcarLimiteRecuo() {
        this.limiteRecuoMinSize = caminho.size();
    }

    /**
     * Marks a lock as unlocked for this player, if it is not already present.
     * @param id the ID of the lock to unlock
     */
    public void desbloquearTranca(int id) {
        if (!podePassarTranca(id)) {
            trancasDesbloqueadas.addToRear(id);
        }
    }
    /**
     * Checks whether this player can pass through a lock with the given ID.
     *
     * @param id the lock ID to check
     * @return true if the player has already unlocked this ID, or false otherwise
     */
    public boolean podePassarTranca(int id) {
        Iterator<Integer> it = trancasDesbloqueadas.iterator();
        while (it.hasNext()) {
            if (it.next().equals(id)) return true;
        }
        return false;
    }

    /**
     * Returns the player's name.
     * @return the name
     */
    public String getNome() { return nome; }

    /**
     * Returns the current division where the player is located.
     * @return the current division
     */
    public Divisao getLocalAtual() { return localAtual; }

    /**
     * Returns the number of extra moves the player currently has.
     * @return the extra moves count
     */
    public int getJogadasExtra() { return jogadasExtra; }

    /**
     * Sets the number of extra moves the player has.
     * @param n the new number of extra moves
     */
    public void setJogadasExtra(int n) { this.jogadasExtra = n; }

    /**
     * Adds extra moves to the player's current total.
     * @param n the number of extra moves to add
     */
    public void adicionarJogadasExtras(int n) { this.jogadasExtra += n; }

    /**
     * Returns whether the player is currently blocked from playing.
     *
     * @return true if turnosBloqueado is greater than zero, false otherwise
     */
    public boolean isBloqueado() { return turnosBloqueado > 0; }

    /**
     * Returns the number of blocked turns.
     * @return the number of blocked turns
     */
    public int getTurnosBloqueado() { return turnosBloqueado; }

    /**
     * Consumes one blocked turn, if there are any remaining.
     */
    public void consumirUmTurnoBloqueado() {
        if (turnosBloqueado > 0) turnosBloqueado--;
    }
    /**
     * Allows a human player to choose another player as the target for a position swap.
     * @param todosJogadores the list of all players currently in the game
     * @param view           the game view used to display options and read the user's choice
     * @return the chosen Player to swap positions with
     */
    public Player escolherAlvoParaTroca(ArrayUnorderedList<Player> todosJogadores, GameView view) {

        view.mostrarEscolhaAlvoTroca(todosJogadores, this.nome);

        Player[] opcoes = new Player[todosJogadores.size()];
        int i = 0;
        Iterator<Player> it = todosJogadores.iterator();
        while (it.hasNext()) {
            opcoes[i] = it.next();
            i++;
        }

        while (true) {
            int escolha = view.pedirEscolhaMovimento();

            if (escolha >= 1 && escolha <= opcoes.length) {
                Player escolhido = opcoes[escolha - 1];

                if (escolhido.equals(this)) {
                    view.mostrarErro("Não podes trocar contigo mesmo. Escolhe outro.");
                    continue;
                }
                return escolhido;
            }
            if (escolha == 0) return null; // Opção de 'Parar'

            view.mostrarErroOpcaoInvalida(1, opcoes.length);
        }
    }
    /**
     * Returns a string representation of the player
     * @return a string representation of this player
     */
    @Override
    public String toString() { return "Jogador " + nome + " @ " + localAtual.getNome(); }
}