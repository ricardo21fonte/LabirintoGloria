package io;

import java.time.LocalDateTime;
import java.util.Iterator; 

import Lists.ArrayUnorderedList;
/**
 * Represents the summary of GameReport
 */
public class GameReport {
    /**
     * Name of the winning player.
     */
    private String vencedor;
    /**
     * Date and time when this report was created.
     */
    private LocalDateTime dataHora;
    /**
     * Duration of the game
     */
    private int duracao;
    /**
     * List containing a Player's entry
     */
    private ArrayUnorderedList<PlayerReport> listaJogadores;
    /**
     * Name of the map used in the match.
     */
    private String mapaNome;
    /**
     * Difficulty level label used in the match
     */
    private String dificuldade;
    /**
     * Global number of enigmas successfully solved.
     */
    private int totalEnigmasResolvidos;
    /**
     * Global number of enigmas that were attempted
     */
    private int totalEnigmasTentados;
    /**
     * Total number of obstacles triggered during the game
     */
    private int totalObstaculos;

    /**
     * Creates an empty GameReport with an empty player list
     */
    public GameReport() {
        this.listaJogadores = new ArrayUnorderedList<>();
        this.dataHora = LocalDateTime.now();
    }
    /**
     * Returns the total number of enigmas that were attempted during the game.
     * @return total attempted enigmas
     */
    public int getTotalEnigmasTentados() { 
        return totalEnigmasTentados; 
    }
    /**
     * Sets the total number of enigmas that were attempted.
     * @param total the total attempted enigmas
     */
    public void setTotalEnigmasTentados(int total) {
        this.totalEnigmasTentados = total;
    }
    /**
     * Returns the total number of enigmas that were solved successfully.
     * @return total solved enigmas
     */
    public int getTotalEnigmasResolvidos() { 
        return totalEnigmasResolvidos; 
    }
    /**
     * Sets the total number of enigmas solved successfully.
     * @param total the total solved enigmas
     */
    public void setTotalEnigmasResolvidos(int total) {
        this.totalEnigmasResolvidos = total;
    }

    /**
     * Returns the list of PlayerReport objects, one for each player in the match.
     * @return list of player reports
     */
    public ArrayUnorderedList<PlayerReport> getListaJogadores() { return listaJogadores; }
    /**
     * Replaces the current list of PlayerReport entries.
     * @param lista the new list of player reports
     */
    public void setListaJogadores(ArrayUnorderedList<PlayerReport> lista) { this.listaJogadores = lista; }
    /**
     * Adds a new PlayerReport to the list of players.
     * @param player report of a single player to be added
     */
    public void adicionarJogador(PlayerReport player) { this.listaJogadores.addToRear(player); }
    /**
     * Returns the name of the winner.
     * @return winner name
     */
    public String getVencedor() { return vencedor; }
    /**
     * Sets the name of the winner.
     * @param nome winner name
     */
    public void setVencedor(String nome) { this.vencedor = nome; }

    /**
     * Returns the date and time when the report was created.
     * @return date and time of this report
     */
    public LocalDateTime getDataHora() { return dataHora; }
    /**
     * Sets the date and time for this report.
     * @param date date and time value
     */
    public void setDataHora(LocalDateTime date) { this.dataHora = date; }
    /**
     * Returns the duration of the game.
     * @return duration of the game
     */
    public int getDuracao() { return duracao; }
    /**
     * Sets the duration of the game.
     *
     * @param duracao duration value
     */
    public void setDuracao(int duracao) { this.duracao = duracao; }
    /**
     * Returns the name of the map used for this game.
     * @return map name
     */
    public String getMapaNome() { return mapaNome; }
    /**
     * Sets the name of the map used for this game.
     * @param mapa map name
     */
    public void setMapaNome(String mapa) { this.mapaNome = mapa; }
    /**
     * Returns the difficulty label used for this game.
     * @return difficulty label
     */
    public String getDificuldade() { return dificuldade; }
    /**
     * Sets the difficulty label used for this game.
     * @param dificuldade difficulty label
     */
    public void setDificuldade(String dificuldade) { this.dificuldade = dificuldade; }
    /**
     * Returns the total number of obstacles triggered during this game.
     * @return total obstacles
     */
    public int getTotalObstaculos() { return totalObstaculos; }
    /**
     * Sets the total number of obstacles triggered during this game.
     *
     * @param total total obstacles
     */
    public void setTotalObstaculos(int total) { this.totalObstaculos = total; }

    /**
     * Class that stores all information about a single player in a game.
     */
    public static class PlayerReport {
        /**
         * Player name.
         */
        private String nome;

        /**
         * Player type.
         */
        private String tipo;

        /**
         * Name of the room where the player
         */
        private String localAtual;

        /**
         * Number of turns this player has played.
         */
        private int turnosJogados;

        /**
         * true if this player won the game, or false otherwise.
         */
        private boolean vencedor;

        /**
         * List of room names representing the path taken by this player.
         */
        private ArrayUnorderedList<String> percurso;

        /**
         * List of descriptions for every obstacle encountered by the player.
         */
        private ArrayUnorderedList<String> obstaculos;

        /**
         * List of EnigmaEvent objects describing each enigma the player has faced.
         */
        private ArrayUnorderedList<EnigmaEvent> enigmas;

        /**
         * List of descriptions of all effects applied to the player during the game.
         */
        private ArrayUnorderedList<String> efeitosAplicados;

        /**
         * Creates a PlayerReport for the given player name and type.
         * @param nome player name
         * @param tipo player type
         */
        public PlayerReport(String nome, String tipo) {
            this.nome = nome;
            this.tipo = tipo;
            this.percurso = new ArrayUnorderedList<>();
            this.obstaculos = new ArrayUnorderedList<>();
            this.enigmas = new ArrayUnorderedList<>();
            this.efeitosAplicados = new ArrayUnorderedList<>();
        }
        /**
         * Returns the path traversed by the player.
         * @return list of room names
         */
        public ArrayUnorderedList<String> getPercurso() { return percurso; }
        /**
         * Adds a room name to the player's path.
         * @param sala the name of the room visited
         */
        public void adicionarPercurso(String sala) { this.percurso.addToRear(sala); }
        /**
         * Returns the list of obstacles encountered by the player.
         * @return list of obstacle descriptions
         */
        public ArrayUnorderedList<String> getObstaculos() { return obstaculos; }
        /**
         * Adds a textual description of an obstacle encountered.
         * @param obstaculo description of the obstacle
         */
        public void adicionarObstaculo(String obstaculo) { this.obstaculos.addToRear(obstaculo); }

        /**
         * Returns the list of EnigmaEvent elements associated with the player.
         * @return list of enigma events
         */
        public ArrayUnorderedList<EnigmaEvent> getEnigmas() { return enigmas; }
        /**
         * Adds a new EnigmaEvent to the player's log.
         * @param event enigma event to add
         */
        public void adicionarEnigma(EnigmaEvent event) { this.enigmas.addToRear(event); }

        /**
         * Returns the list of effect descriptions applied to the player.
         * @return list of effects
         */
        public ArrayUnorderedList<String> getEfeitosAplicados() { return efeitosAplicados; }

        /**
         * Counts how many enigmas this player has successfully solved.
         * @return number of resolved enigmas
         */
        public int totalEnigmasResolvidos() {
            int count = 0;
            Iterator<EnigmaEvent> it = enigmas.iterator();
            while(it.hasNext()) {
                if(it.next().resolvido) count++;
            }
            return count;
        }
        /**
         * Returns the player name.
         * @return player name
         */
        public String getNome() { return nome; }
        /**
         * Returns the player type.
         * @return player type
         */
        public String getTipo() { return tipo; }
        /**
         * Returns the last known room name where this player was located.
         * @return current location name
         */
        public String getLocalAtual() { return localAtual; }
        /**
         * Sets the last known room name where this player was located.
         * @param location location name
         */
        public void setLocalAtual(String location) { this.localAtual = location; }
        /**
         * Returns how many turns this player has played.
         * @return number of turns played
         */
        public int getTurnosJogados() { return turnosJogados; }
        /**
         * Sets how many turns this player has played.
         * @param turnos number of turns
         */
        public void setTurnosJogados(int turnos) { this.turnosJogados = turnos; }
        /**
         * Returns true if this player is the winner,
         * @return whether the player won the game
         */
        public boolean isVencedor() { return vencedor; }
        /**
         * Sets whether this player is the winner.
         * @param vencedor true if winner
         */
        public void setVencedor(boolean vencedor) { this.vencedor = vencedor; }
    }
    /**
     * Represents a single enigma interaction for a player:
     * the room where it occurred, the question, the answer,
     * whether it was solved and the effect applied.
     */
    public static class EnigmaEvent {
        /**
         * Enigma question text.
         */
        public String pergunta;

        /**
         * Answer given by the player
         */
        public String resposta;

        /**
         * Effect code applied as a consequence
         */
        public String efeito;

        /**
         * Name of the room where the enigma was triggered.
         */
        public String sala;

        /**
         * Returns true if the enigma was resolved successfully,
         */
        public boolean resolvido;
        /**
         * Creates a new EnigmaEvent with all its properties.
         * @param p  the enigma question
         * @param r  the player's answer (or recorded answer)
         * @param res {@code true} if solved correctly, {@code false} otherwise
         * @param ef the effect code applied
         * @param s  the room where the enigma occurred
         */
        public EnigmaEvent(String p, String r, boolean res, String ef, String s) {
            this.pergunta = p; this.resposta = r; this.resolvido = res;
            this.efeito = ef; this.sala = s;
        }
    }
}