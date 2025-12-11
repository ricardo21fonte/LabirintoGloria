package engine;

import Lists.ArrayUnorderedList;
import Queue.LinkedQueue;
import enums.Dificuldade;
import game.Enigma;
import game.Player;
/**
 * Class that groups together all core runtime data of a game session.
 */
public class GameData {
    /**
     * Queue of players that represents the current turn order.
     */
    public LinkedQueue<Player> turnQueue;

    /**
     * List containing all players participating in the game
     */
    public ArrayUnorderedList<Player> todosJogadores;

    /**
     * List of all available riddles that can be used during the game
     */
    public ArrayUnorderedList<Enigma> enigmasDisponiveis;

    /**
     * Difficulty level of the game
     */
    public Dificuldade dificuldade;
    /**
     * Creates a new GameData instance with all core game components.
     *
     * @param turnQueue          queue representing the turn order of players
     * @param todosJogadores     list with all players in the current game
     * @param enigmasDisponiveis list of riddles available to be used
     * @param dificuldade        selected global difficulty of the game
     */
    public GameData(LinkedQueue<Player> turnQueue, ArrayUnorderedList<Player> todosJogadores, 
                    ArrayUnorderedList<Enigma> enigmasDisponiveis, Dificuldade dificuldade) {
        this.turnQueue = turnQueue;
        this.todosJogadores = todosJogadores;
        this.enigmasDisponiveis = enigmasDisponiveis;
        this.dificuldade = dificuldade;
    }
}