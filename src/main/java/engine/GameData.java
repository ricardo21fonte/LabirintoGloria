package engine;

import Lists.ArrayUnorderedList;
import Queue.LinkedQueue;
import enums.Dificuldade;
import game.Enigma;
import game.Player;

public class GameData {
    public LinkedQueue<Player> turnQueue;
    public ArrayUnorderedList<Player> todosJogadores;
    public ArrayUnorderedList<Enigma> enigmasDisponiveis;
    public Dificuldade dificuldade;

    public GameData(LinkedQueue<Player> turnQueue, ArrayUnorderedList<Player> todosJogadores, 
                    ArrayUnorderedList<Enigma> enigmasDisponiveis, Dificuldade dificuldade) {
        this.turnQueue = turnQueue;
        this.todosJogadores = todosJogadores;
        this.enigmasDisponiveis = enigmasDisponiveis;
        this.dificuldade = dificuldade;
    }
}