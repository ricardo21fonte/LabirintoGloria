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
import ui.GameView; // Nota: mudei para o package ui

public class GameInitializer {

    private GameView view;
    private LabyrinthGraph<Divisao> labyrinthGraph;

    public GameInitializer(GameView view, LabyrinthGraph<Divisao> labyrinthGraph) {
        this.view = view;
        this.labyrinthGraph = labyrinthGraph;
    }

    public GameData setupCompleto() {
        view.mostrarMensagemCarregar();

        // 1. Enigmas e Dificuldade
        ArrayUnorderedList<Enigma> allEnigmas = loadEnigmas();
        Dificuldade difficulty = setupDifficulty();
        ArrayUnorderedList<Enigma> enigmasFiltrados = filterEnigmasByDifficulty(allEnigmas, difficulty);

        // 2. Entradas do Mapa
        Divisao[] entrances = getMapEntrances();
        if (entrances.length == 0) return null;

        LinkedQueue<Player> turnQueue = new LinkedQueue<>();
        ArrayUnorderedList<Player> todosJogadores = new ArrayUnorderedList<>();

        // 3. Criar Jogadores
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

    // --- MÉTODOS DE APOIO (Copiados do teu GameEngine antigo) ---

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
            Bot bot = new Bot("Bot_" + i, spawn, dif, labyrinthGraph);
            
            turnQueue.enqueue(bot);
            listaGlobal.addToRear(bot);
            view.mostrarBotCriado(spawn.getNome());
        }
    }
}