package game;

import java.util.Iterator;

import Lists.ArrayUnorderedList;
import Queue.LinkedQueue;
import enums.CorredorEvento;
import enums.Dificuldade;
import enums.TipoDivisao;
import graph.LabyrinthGraph;
import ui.GameView;
/**
 * Represents an AI-controlled player (bot) in the labyrinth game.
 */
public class Bot extends Player {

    /**
     * Intelligence level (difficulty) of this bot.
     * Affects chance of solving riddles correctly.
     */
    private Dificuldade inteligencia;

    /**
     * Graph representing the labyrinth as known by the bot.
     * Used for pathfinding and movement decisions.
     */
    private LabyrinthGraph<Divisao> mapaConhecido;

    /**
     * List of lever memories, one entry per room with levers the bot has visited.
     * Each MemoriaAlavanca stores which levers were already tried.
     */
    private ArrayUnorderedList<MemoriaAlavanca> memoriasAlavancas = new ArrayUnorderedList<>();


    /**
     * Creates a new Bot with a given name, starting room, difficulty, and known map.
     *
     * @param nome         the bot's name
     * @param inicio       the starting division (room) of the bot
     * @param inteligencia the bot's intelligence/difficulty level
     * @param mapa         the labyrinth graph known by the bot
     */
    public Bot(String nome, Divisao inicio, Dificuldade inteligencia, LabyrinthGraph<Divisao> mapa) {
        super(nome, inicio);
        this.inteligencia = inteligencia;
        this.mapaConhecido = mapa;
    }

    // =================================================================
    // 1. BOT (MOVEMENT DECISION)
    // =================================================================
    /**
     * Decides the next move of the bot according to a priority strategy:
     * @return the next Divisao the bot wants to move to
     */
    public Divisao escolherMovimento() {

        Divisao passoParaTesouro = executarBFS(TipoDivisao.SALA_CENTRAL, false);

        if (passoParaTesouro != null) {
            return passoParaTesouro;
        }

        Divisao passoParaAlavanca = executarBFS(TipoDivisao.SALA_ALAVANCA, true);

        if (passoParaAlavanca != null) {
            return passoParaAlavanca;
        }

        return movimentoAleatorio();
    }

    /**
     * Executes a BFS (Breadth-First Search) from the bot's current location in order to find a room of a given type.
     * @param tipoAlvo             the target room type to search for
     * @param apenasAlavancasUteis if true, lever rooms already "solved"
     * @return the next Divisao step towards the found target room
     */
    private Divisao executarBFS(TipoDivisao tipoAlvo, boolean apenasAlavancasUteis) {
        LinkedQueue<Divisao> fila = new LinkedQueue<>();
        ArrayUnorderedList<Divisao> visitados = new ArrayUnorderedList<>();
        ArrayUnorderedList<Parente> arvoreGenealogica = new ArrayUnorderedList<>();

        Divisao inicio = getLocalAtual();
        fila.enqueue(inicio);
        visitados.addToRear(inicio);
        arvoreGenealogica.addToRear(new Parente(inicio, null));

        Divisao alvoEncontrado = null;

        while (!fila.isEmpty()) {
            Divisao atual;
            try { atual = fila.dequeue(); } catch (Exception e) { break; }

            // --- LÓGICA DE DECISÃO (A única parte que mudava) ---
            if (atual.getTipo() == tipoAlvo) {
                boolean encontrou = true;
                
                // Se estamos à procura de alavancas, verificar se é útil
                if (apenasAlavancasUteis && tipoAlvo == TipoDivisao.SALA_ALAVANCA) {
                    // Se já tivermos a chave para esta tranca, a alavanca não é útil (encontrou = false)
                    if (this.podePassarTranca(atual.getIdDesbloqueio())) {
                        encontrou = false; 
                    }
                }

                if (encontrou) {
                    alvoEncontrado = atual;
                    break;
                }
            }
            Iterator<Divisao> it = mapaConhecido.getVizinhos(atual).iterator();
            while (it.hasNext()) {
                Divisao vizinho = it.next();

                if (!visitados.contains(vizinho)) {
                    if (podePassar(atual, vizinho)) {
                        visitados.addToRear(vizinho);
                        fila.enqueue(vizinho);
                        arvoreGenealogica.addToRear(new Parente(vizinho, atual));
                    }
                }
            }
        }

        if (alvoEncontrado != null) {
            return reconstruirPrimeiroPasso(arvoreGenealogica, alvoEncontrado);
        }
        return null;
    }
    /**
     * Verifies whether the bot can pass from one division to another, based on the corridor event between them.
     *
     * @param origem  the origin division
     * @param destino the destination division
     * @return true if the bot can pass through the corridor or false otherwise
     */
    private boolean podePassar(Divisao origem, Divisao destino) {
        EventoCorredor evento = mapaConhecido.getCorredorEvento(origem, destino);
        
        if (evento.getTipo() == CorredorEvento.LOCKED) {
            int idChave = evento.getValor();
            return this.podePassarTranca(idChave);
        }
        return true;
    }

    /**
     * Reconstructs the first step towards a destination division using the generated BFS tree.
     *
     * @param arvore   list of relationships produced by BFS
     * @param destino  the target division
     * @return the first Divisao to move to from the current location in order to reach destination
     */
    private Divisao reconstruirPrimeiroPasso(ArrayUnorderedList<Parente> arvore, Divisao destino) {
        Divisao passo = destino;
        Divisao anterior = null;
        
        while (passo != getLocalAtual()) {
            anterior = passo;
            passo = obterPai(arvore, passo);
            if (passo == null) return null; 
        }
        return anterior;
    }
    /**
     * Chooses a random adjacent division from the bot's current location.
     * @return a randomly chosen neighbor division
     */
    private Divisao movimentoAleatorio() {
        ArrayUnorderedList<Divisao> vizinhos = mapaConhecido.getVizinhos(getLocalAtual());
        if (vizinhos.isEmpty()) return null;
        int index = (int) (Math.random() * vizinhos.size());
        Iterator<Divisao> it = vizinhos.iterator();
        for (int i = 0; i < index; i++) it.next();
        return it.next();
    }

    /**
     * Tries to solve a given riddle (enigma), based on the bot's difficulty level.
     * @param enigma the riddle to attempt
     * @return true if the bot solves the riddle, or false otherwise
     */
    public boolean tentarResolverEnigma(Enigma enigma) {
        double chanceAcerto = 0.0; 
        switch (inteligencia) {
            case FACIL: chanceAcerto = 0.25; break;
            case MEDIO: chanceAcerto = 0.50; break;
            case DIFICIL: chanceAcerto = 0.75; break;
        }
        return Math.random() <= chanceAcerto;
    }

    /**
     * Finds the parent division of a given child division inside the BFS tree.
     *
     * @param lista list of relationships
     * @param filho the child division
     * @return the parent division
     */
    private Divisao obterPai(ArrayUnorderedList<Parente> lista, Divisao filho) {
        Iterator<Parente> it = lista.iterator();
        while (it.hasNext()) {
            Parente p = it.next();
            if (p.filho.equals(filho)) return p.pai;
        }
        return null;
    }
    /**
     * Helper class used to store parent-child relationships during BFS.
     */
    private class Parente {

        Divisao filho, pai;
        /**
         * Creates a new parent-child pair.
         */
        public Parente(Divisao f, Divisao p) { this.filho = f; this.pai = p; }
    }

    /**
     * Returns the bot's difficulty level.
     * @return the difficulty level
     */
    public Dificuldade getInteligencia() { return inteligencia; }

    /**
     * Chooses a lever in a given room, avoiding levers that this bot has already tried in that specific room.
     *
     * @param sala         the room that contains the levers
     * @param numAlavancas the total number of levers
     * @return the chosen lever index in the range
     */
    public int escolherAlavanca(Divisao sala, int numAlavancas) {
        MemoriaAlavanca mem = obterMemoriaAlavanca(sala, numAlavancas);

        // contar quantas alavancas ainda não foram tentadas
        int disponiveis = 0;
        for (int i = 0; i < numAlavancas; i++) {
            if (!mem.tentadas[i]) {
                disponiveis++;
            }
        }

        // se já tentou todas, faz fallback aleatório
        if (disponiveis == 0) {
            return 1 + (int)(Math.random() * numAlavancas);
        }

        // escolher uma das não tentadas, de forma aleatória
        int salto = (int)(Math.random() * disponiveis);
        for (int i = 0; i < numAlavancas; i++) {
            if (!mem.tentadas[i]) {
                if (salto == 0) {
                    mem.tentadas[i] = true; // marca como tentada
                    return i + 1;
                }
                salto--;
            }
        }

        return 1;
    }

    /**
     * Retrieves (or creates) the lever memory associated with a given room.
     *
     * @param sala         the room whose lever memory is needed
     * @param numAlavancas the total number of levers in that room
     * @return an existing MemoriaAlavanca for the room, or a new one if none existed yet
     */
    private MemoriaAlavanca obterMemoriaAlavanca(Divisao sala, int numAlavancas) {
        Iterator<MemoriaAlavanca> it = memoriasAlavancas.iterator();
        while (it.hasNext()) {
            MemoriaAlavanca memoria = it.next();
            if (memoria.sala.equals(sala)) {
                return memoria;
            }
        }

        // se ainda não existe memória para esta sala, cria
        MemoriaAlavanca nova = new MemoriaAlavanca(sala, numAlavancas);
        memoriasAlavancas.addToRear(nova);
        return nova;
    }

    /**
     * Internal static class that stores which levers have already been tried in a specific room.
     */
    private static class MemoriaAlavanca {
        /**
         * Room associated with this memory.
         */
        Divisao sala;
        /**
         * Array indicating which levers were already tried.
         */
        boolean[] tentadas;
        /**
         * Creates a new lever memory for a given room.
         */
        MemoriaAlavanca(Divisao sala, int numAlavancas) {
            this.sala = sala;
            this.tentadas = new boolean[numAlavancas]; // tudo a false por defeito
        }
    }
    /**
     * Rolls the dice for the bot, notifies the view, and returns the result.
     * @param view the game view used to display feedback
     * @return the dice value in the range {@code 1..6}
     */
    @Override
    public int lancarDados(GameView view) {
        view.avisarBotLancaDados();
        try { Thread.sleep(1000); } catch (Exception e){}
        int val = (int)(Math.random() * 6) + 1;
        view.mostrarResultadoDados(true, val);
        return val;
    }

    /**
     * Chooses the destination division where the bot will move next among the list of adjacent divisions, using the bot's AI.
     * @param vizinhos list of neighboring divisions
     * @param view     the game view used to display the decision
     * @return the chosen destination division, or null if none
     */
    @Override
    public Divisao escolherDestino(ArrayUnorderedList<Divisao> vizinhos, GameView view) {
        Divisao d = this.escolherMovimento();
        if(d != null) view.mostrarBotDecisao(d.getNome());
        return d;
    }
    /**
     * Asks the bot to try to solve a riddle, notifying the view and then using the bot's AI to determine success.
     *
     * @param e    the riddle (enigma) to solve
     * @param view the game view used to display the analysis
     * @return true if the bot solves the riddle, false otherwise
     */
    @Override
    public boolean resolverEnigma(Enigma e, GameView view) {
        view.mostrarBotAnalisaEnigma(getNome(), getInteligencia().toString());
        return this.tentarResolverEnigma(e);
    }
    /**
     * Asks the bot to choose which lever to pull in a given room, using its lever memory, and notifies the view.
     * @param sala the room containing the levers
     * @param view the game view used to display the choice
     * @return the chosen lever index
     */
    @Override
    public int decidirAlavanca(Divisao sala, GameView view) {
        int escolha = this.escolherAlavanca(sala, 3);
        view.mostrarBotEscolheAlavanca(escolha);
        return escolha;
    }

}