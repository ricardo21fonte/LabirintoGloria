package engine;

import Queue.LinkedQueue;
import game.Bot;
import game.Player;
import ui.GameView;
/**
 * Manages the turn order of the game.
 */
public class TurnManager {
    /** Queue that stores the players in turn order. */
    private LinkedQueue<Player> fila;

    /** View used to show messages and ask for input. */
    private GameView view;

    /**
     * Creates a TurnManager with the given queue and view.
     * @param fila queue that holds all players participating in the game
     * @param view UI component used for feedback and pauses between turns
     */
    public TurnManager(LinkedQueue<Player> fila, GameView view) {
        this.fila = fila;
        this.view = view;
    }

    /**
     * Returns the next player allowed to play.
     * @return the next Player that can act this turn
     */
    public Player proximoJogador() {
        if (fila.isEmpty()) return null;

        Player p = null;
        try {
            p = fila.dequeue();

            while (p.isBloqueado()) {
                view.mostrarBloqueado(p.getNome(), p.getTurnosBloqueado());
                p.consumirUmTurnoBloqueado();
                fila.enqueue(p);
                pausa(p);
                if (fila.isEmpty()) return null;
                p = fila.dequeue();
            }
        } catch (Exception e) {
            return null;
        }
        return p;
    }
    /**
     * Should be called at the end of a player's turn.
     * @param p the player whose turn has just finished
     */
    public void fimDoTurno(Player p) {
        fila.enqueue(p);
        pausa(p);
    }
    /**
     * Checks whether there are still players in the turn queue.
     * @return true if there is at least one player waiting,
     */
    public boolean temJogadores() {
        return !fila.isEmpty();
    }
    /**
     * Method to add a short pause between turns.
     * @param p the player whose context defines the type of pause
     */
    private void pausa(Player p) {
        if (p instanceof Bot) {
            try { Thread.sleep(1000); } catch (Exception e) {}
        } else {
            view.esperarEnter();
        }
    }
}