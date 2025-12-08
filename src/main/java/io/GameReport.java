package io;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates game report data for saving and retrieving game sessions.
 * Contains complete game history: players' paths, obstacles, enigmas, effects.
 */
public class GameReport {
    private String vencedor;                    // Winner name
    private LocalDateTime dataHora;             // Timestamp
    private int duracao;                        // Duration in turns
    private List<PlayerReport> listaJogadores;  // Complete player reports
    private String mapaNome;                    // Map name
    private String dificuldade;                 // Difficulty level
    private int totalEnigmasResolvidos;         // Total enigmas solved in game
    private int totalObstaculos;                // Total obstacles encountered

    // ===== CONSTRUCTOR =====
    public GameReport() {
        this.listaJogadores = new ArrayList<>();
        this.dataHora = LocalDateTime.now();
        this.totalEnigmasResolvidos = 0;
        this.totalObstaculos = 0;
    }

    // ===== GETTERS & SETTERS =====
    public String getVencedor() {
        return vencedor;
    }

    public void setVencedor(String vencedor) {
        this.vencedor = vencedor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    public List<PlayerReport> getListaJogadores() {
        return listaJogadores;
    }

    public void setListaJogadores(List<PlayerReport> listaJogadores) {
        this.listaJogadores = listaJogadores;
    }

    public void adicionarJogador(PlayerReport player) {
        this.listaJogadores.add(player);
    }

    public String getMapaNome() {
        return mapaNome;
    }

    public void setMapaNome(String mapaNome) {
        this.mapaNome = mapaNome;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(String dificuldade) {
        this.dificuldade = dificuldade;
    }

    public int getTotalEnigmasResolvidos() {
        return totalEnigmasResolvidos;
    }

    public void setTotalEnigmasResolvidos(int total) {
        this.totalEnigmasResolvidos = total;
    }

    public int getTotalObstaculos() {
        return totalObstaculos;
    }

    public void setTotalObstaculos(int total) {
        this.totalObstaculos = total;
    }

    // ===== PLAYER REPORT INNER CLASS =====
    public static class PlayerReport {
        private String nome;
        private String tipo;                    // "Humano" or "Bot"
        private String localAtual;              // Current room name
        private int turnosJogados;              // Number of turns played
        private List<String> percurso;          // Path taken (list of room names)
        private List<String> obstaculos;        // Obstacles encountered
        private List<EnigmaEvent> enigmas;      // Enigmas encountered and results
        private List<String> efeitosAplicados;  // Effects applied
        private boolean vencedor;               // Whether player won

        public PlayerReport(String nome, String tipo) {
            this.nome = nome;
            this.tipo = tipo;
            this.percurso = new ArrayList<>();
            this.obstaculos = new ArrayList<>();
            this.enigmas = new ArrayList<>();
            this.efeitosAplicados = new ArrayList<>();
            this.vencedor = false;
        }

        // Getters
        public String getNome() { return nome; }
        public String getTipo() { return tipo; }
        public String getLocalAtual() { return localAtual; }
        public void setLocalAtual(String local) { this.localAtual = local; }
        
        public int getTurnosJogados() { return turnosJogados; }
        public void setTurnosJogados(int turnos) { this.turnosJogados = turnos; }

        public List<String> getPercurso() { return percurso; }
        public void adicionarPercurso(String sala) { this.percurso.add(sala); }

        public List<String> getObstaculos() { return obstaculos; }
        public void adicionarObstaculo(String obstaculo) { this.obstaculos.add(obstaculo); }

        public List<EnigmaEvent> getEnigmas() { return enigmas; }
        public void adicionarEnigma(EnigmaEvent enigma) { this.enigmas.add(enigma); }

        public List<String> getEfeitosAplicados() { return efeitosAplicados; }
        public void adicionarEfeito(String efeito) { this.efeitosAplicados.add(efeito); }

        public boolean isVencedor() { return vencedor; }
        public void setVencedor(boolean vencedor) { this.vencedor = vencedor; }

        public int totalEnigmasResolvidos() {
            return (int) enigmas.stream().filter(e -> e.resolvido).count();
        }
    }

    // ===== ENIGMA EVENT INNER CLASS =====
    public static class EnigmaEvent {
        public String pergunta;         // Question text
        public String resposta;         // User's answer
        public boolean resolvido;       // Whether correctly solved
        public String efeito;           // Effect applied (success/failure)
        public String sala;             // Which room (Enigma room name)

        public EnigmaEvent(String pergunta, String resposta, boolean resolvido, String efeito, String sala) {
            this.pergunta = pergunta;
            this.resposta = resposta;
            this.resolvido = resolvido;
            this.efeito = efeito;
            this.sala = sala;
        }
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("=== RELATÓRIO COMPLETO DO JOGO ===\n");
        sb.append("Vencedor: ").append(vencedor).append("\n");
        sb.append("Data/Hora: ").append(dataHora.format(formatter)).append("\n");
        sb.append("Duração: ").append(duracao).append(" turnos\n");
        sb.append("Mapa: ").append(mapaNome).append("\n");
        sb.append("Dificuldade: ").append(dificuldade).append("\n");
        sb.append("Total de Enigmas Resolvidos: ").append(totalEnigmasResolvidos).append("\n");
        sb.append("Total de Obstáculos Enfrentados: ").append(totalObstaculos).append("\n");
        sb.append("Número de Jogadores: ").append(listaJogadores.size()).append("\n");
        return sb.toString();
    }
}
