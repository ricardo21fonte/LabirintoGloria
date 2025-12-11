package engine;

import java.util.Iterator;

import Lists.ArrayUnorderedList;
import Queue.LinkedQueue;
import enums.Dificuldade;
import enums.TipoDivisao;
import game.Bot;
import game.Divisao;
import game.Enigma;
import game.Player;
import graph.LabyrinthGraph;
import io.EnigmaLoader;
import ui.GameView;
/**
 * Handles the initial game setup.
 */
public class GameInitializer {

    /** View used for user interaction (menus, messages, input). */
    private GameView view;

    /** Labyrinth graph where the game will take place. */
    private LabyrinthGraph<Divisao> labyrinthGraph;
    /**
     * Constructs a new GameInitializer using the provided view and labyrinth graph.
     * @param view           UI component responsible for I/O
     * @param labyrinthGraph graph that represents the labyrinth
     */
    public GameInitializer(GameView view, LabyrinthGraph<Divisao> labyrinthGraph) {
        this.view = view;
        this.labyrinthGraph = labyrinthGraph;
    }
    /**
     * Executes the complete setup flow for a new game.
     * @return a fully initialized GameData instance
     */
    public GameData setupCompleto() {
        view.mostrarMensagemCarregar();

        // Enigmas e Dificuldade
        ArrayUnorderedList<Enigma> allEnigmas = loadEnigmas();
        Dificuldade difficulty = setupDifficulty();
        ArrayUnorderedList<Enigma> enigmasFiltrados = filterEnigmasByDifficulty(allEnigmas, difficulty);

        // Entradas do Mapa
        Divisao[] entrances = getMapEntrances();
        if (entrances.length == 0) return null;

        LinkedQueue<Player> turnQueue = new LinkedQueue<>();
        ArrayUnorderedList<Player> todosJogadores = new ArrayUnorderedList<>();

        // Cria Jogadores
        int numHumans = setupHumanPlayers(entrances, turnQueue, todosJogadores);
        setupBots(entrances, turnQueue, todosJogadores, numHumans);

        if (turnQueue.isEmpty()) {
            view.mostrarSemJogadores();
            return null;
        }

        view.mostrarInicioJogo();
        view.esperarEnter();

        return new GameData(turnQueue, todosJogadores, enigmasFiltrados, difficulty);
    }

    /**
     * Loads enigmas from the default JSON file.
     * @return list of all enigmas found in the JSON file
     */
    private ArrayUnorderedList<Enigma> loadEnigmas() {
        EnigmaLoader enigmaLoader = new EnigmaLoader();
        return enigmaLoader.loadEnigmas("resources/enigmas/enigmas.json");
    }
    /**
     * Asks the user to choose the global difficulty of the game.
     * @return the chosen difficulty
     */
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
    /**
     * Filters the full list of enigmas, keeping only those that match the given difficulty.
     * @param all list with all loaded enigmas
     * @param dif difficulty to filter by
     * @return new list containing only enigmas of the given difficulty
     */
    private ArrayUnorderedList<Enigma> filterEnigmasByDifficulty(ArrayUnorderedList<Enigma> all, Dificuldade dif) {
        ArrayUnorderedList<Enigma> filtered = new ArrayUnorderedList<>();
        Iterator<Enigma> it = all.iterator();
        while (it.hasNext()) {
            Enigma e = it.next();
            if (e.getDificuldade() == dif) filtered.addToRear(e);
        }
        view.mostrarDificuldadeDefinida(dif.toString(), filtered.size());
        return filtered;
    }
    /**
     * Scans the labyrinth graph and collects all entrance rooms
     * @return an array of entrance rooms; if none are found, displays
     *         an error message and returns an empty array
     */
    private Divisao[] getMapEntrances() {
        ArrayUnorderedList<Divisao> entrances = new ArrayUnorderedList<>();
        Object[] vertices = labyrinthGraph.getVertices();
        for (Object obj : vertices) {
            Divisao d = (Divisao) obj;
            if (d.getTipo() == TipoDivisao.ENTRADA) entrances.addToRear(d);
        }
        if (entrances.isEmpty()) {
            view.mostrarErroSemEntradas();
            return new Divisao[0];
        }
        Divisao[] arr = new Divisao[entrances.size()];
        Iterator<Divisao> it = entrances.iterator();
        int i = 0;
        while (it.hasNext()) arr[i++] = it.next();
        return arr;
    }

    /**
     * Creates human players, asks for their names, assigns them
     * random entrance rooms and enqueues them into the turn queue.
     * @param entrances   array of available entrance rooms
     * @param turnQueue   queue that controls the turn order
     * @param listaGlobal global list of all players
     * @return the number of human players created
     */
    private int setupHumanPlayers(Divisao[] entrances, LinkedQueue<Player> turnQueue, ArrayUnorderedList<Player> listaGlobal) {
        int numHumans;
        int max = 8;
        do {
            numHumans = view.pedirQuantidadeHumanos(max);
            if (numHumans < 0 || numHumans > max) view.mostrarErroOpcaoInvalida(0, max);
        } while (numHumans < 0 || numHumans > max);

        for (int i = 1; i <= numHumans; i++) {
            String name;
            do {
                name = view.pedirNomeJogador(i);
            } while (name.isEmpty());
            
            Divisao spawn = entrances[(int) (Math.random() * entrances.length)];
            Player p = new Player(name, spawn);
            
            turnQueue.enqueue(p);
            listaGlobal.addToRear(p);
            view.mostrarSpawn(spawn.getNome());
        }
        return numHumans;
    }
    /**
     * Creates bot players, asks the user for each bot's difficulty,
     * assigns them random entrance rooms and enqueues them int the turn queue.
     * @param entrances   array of available entrance rooms
     * @param turnQueue   queue that controls the turn order
     * @param listaGlobal global list of all players
     * @param numHumans   number of human players already created
     */
    private void setupBots(Divisao[] entrances, LinkedQueue<Player> turnQueue, ArrayUnorderedList<Player> listaGlobal, int numHumans) {
        int maxBots = 8 - numHumans;
        if (maxBots <= 0) return;

        int numBots;
        do {
            numBots = view.pedirQuantidadeBots(maxBots);
            if (numBots < 0 || numBots > maxBots) view.mostrarErroOpcaoInvalida(0, maxBots);
        } while (numBots < 0 || numBots > maxBots);

        for (int i = 1; i <= numBots; i++) {
            Divisao spawn = entrances[(int) (Math.random() * entrances.length)];
            int opt;
            do {
                opt = view.pedirDificuldadeBot(i);
            } while(opt < 1 || opt > 3);
            
            Dificuldade dif = (opt == 2) ? Dificuldade.MEDIO : (opt == 3) ? Dificuldade.DIFICIL : Dificuldade.FACIL;
            Bot bot = new Bot("Bot " + i, spawn, dif, labyrinthGraph);
            
            turnQueue.enqueue(bot);
            listaGlobal.addToRear(bot);
            view.mostrarBotCriado(spawn.getNome());
        }
    }
}