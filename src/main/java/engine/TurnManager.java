package engine;

import Queue.LinkedQueue;
import game.Bot;
import game.Player;
import ui.GameView;

public class TurnManager {
    private LinkedQueue<Player> fila;
    private GameView view;

    public TurnManager(LinkedQueue<Player> fila, GameView view) {
        this.fila = fila;
        this.view = view;
    }

    /**
     * Retorna o próximo jogador APTO a jogar.
     * Se um jogador estiver bloqueado, processa o bloqueio e passa ao próximo.
     */
    public Player proximoJogador() {
        if (fila.isEmpty()) return null;

        Player p = null;
        try {
            // Tenta tirar o próximo
            p = fila.dequeue();

            // Lógica de bloqueio (Loop até achar um desbloqueado ou a fila rodar toda)
            while (p.isBloqueado()) {
                view.mostrarBloqueado(p.getNome(), p.getTurnosBloqueado());
                p.consumirUmTurnoBloqueado();
                
                // Volta para o fim da fila
                fila.enqueue(p);
                pausa(p); // Pequena pausa visual

                // Tenta o próximo
                if (fila.isEmpty()) return null;
                p = fila.dequeue();
            }
        } catch (Exception e) {
            return null;
        }
        return p;
    }

    public void fimDoTurno(Player p) {
        fila.enqueue(p);
        pausa(p);
    }

    public boolean temJogadores() {
        return !fila.isEmpty();
    }
    
    // Método auxiliar de pausa (movido do Engine)
    private void pausa(Player p) {
        if (p instanceof Bot) {
            try { Thread.sleep(1000); } catch (Exception e) {}
        } else {
            view.esperarEnter();
        }
    }
}