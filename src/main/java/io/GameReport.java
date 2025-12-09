package io;

import java.time.LocalDateTime;
import java.util.Iterator; // Necessário para o método totalEnigmasResolvidos

import Lists.ArrayUnorderedList; // A TUA LISTA

public class GameReport {
    private String vencedor;
    private LocalDateTime dataHora;
    private int duracao;
    private ArrayUnorderedList<PlayerReport> listaJogadores; // MUDOU AQUI
    private String mapaNome;
    private String dificuldade;
    private int totalEnigmasResolvidos;
    private int totalObstaculos;

    public GameReport() {
        this.listaJogadores = new ArrayUnorderedList<>(); // MUDOU AQUI
        this.dataHora = LocalDateTime.now();
    }

    // --- GETTERS & SETTERS ATUALIZADOS ---
    public ArrayUnorderedList<PlayerReport> getListaJogadores() { return listaJogadores; }
    public void setListaJogadores(ArrayUnorderedList<PlayerReport> lista) { this.listaJogadores = lista; }
    public void adicionarJogador(PlayerReport player) { this.listaJogadores.addToRear(player); }

    // (Mantém os outros getters/setters simples de String/int iguais)
    public String getVencedor() { return vencedor; }
    public void setVencedor(String v) { this.vencedor = v; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime d) { this.dataHora = d; }
    public int getDuracao() { return duracao; }
    public void setDuracao(int d) { this.duracao = d; }
    public String getMapaNome() { return mapaNome; }
    public void setMapaNome(String m) { this.mapaNome = m; }
    public String getDificuldade() { return dificuldade; }
    public void setDificuldade(String d) { this.dificuldade = d; }
    public int getTotalEnigmasResolvidos() { return totalEnigmasResolvidos; }
    public void setTotalEnigmasResolvidos(int t) { this.totalEnigmasResolvidos = t; }
    public int getTotalObstaculos() { return totalObstaculos; }
    public void setTotalObstaculos(int t) { this.totalObstaculos = t; }

    // --- PLAYER REPORT INNER CLASS ---
    public static class PlayerReport {
        private String nome;
        private String tipo;
        private String localAtual;
        private int turnosJogados;
        private boolean vencedor;
        
        // MUDANÇA PARA LISTAS CUSTOMIZADAS
        private ArrayUnorderedList<String> percurso;
        private ArrayUnorderedList<String> obstaculos;
        private ArrayUnorderedList<EnigmaEvent> enigmas;
        private ArrayUnorderedList<String> efeitosAplicados;

        public PlayerReport(String nome, String tipo) {
            this.nome = nome;
            this.tipo = tipo;
            this.percurso = new ArrayUnorderedList<>();
            this.obstaculos = new ArrayUnorderedList<>();
            this.enigmas = new ArrayUnorderedList<>();
            this.efeitosAplicados = new ArrayUnorderedList<>();
        }

        // Getters Atualizados
        public ArrayUnorderedList<String> getPercurso() { return percurso; }
        public void adicionarPercurso(String sala) { this.percurso.addToRear(sala); }

        public ArrayUnorderedList<String> getObstaculos() { return obstaculos; }
        public void adicionarObstaculo(String obs) { this.obstaculos.addToRear(obs); }

        public ArrayUnorderedList<EnigmaEvent> getEnigmas() { return enigmas; }
        public void adicionarEnigma(EnigmaEvent e) { this.enigmas.addToRear(e); }

        public ArrayUnorderedList<String> getEfeitosAplicados() { return efeitosAplicados; }
        public void adicionarEfeito(String ef) { this.efeitosAplicados.addToRear(ef); }

        public int totalEnigmasResolvidos() {
            int count = 0;
            Iterator<EnigmaEvent> it = enigmas.iterator();
            while(it.hasNext()) {
                if(it.next().resolvido) count++;
            }
            return count;
        }
        
        // Getters simples mantêm-se
        public String getNome() { return nome; }
        public String getTipo() { return tipo; }
        public String getLocalAtual() { return localAtual; }
        public void setLocalAtual(String l) { this.localAtual = l; }
        public int getTurnosJogados() { return turnosJogados; }
        public void setTurnosJogados(int t) { this.turnosJogados = t; }
        public boolean isVencedor() { return vencedor; }
        public void setVencedor(boolean v) { this.vencedor = v; }
    }

    public static class EnigmaEvent {
        public String pergunta, resposta, efeito, sala;
        public boolean resolvido;
        public EnigmaEvent(String p, String r, boolean res, String ef, String s) {
            this.pergunta = p; this.resposta = r; this.resolvido = res;
            this.efeito = ef; this.sala = s;
        }
    }
}