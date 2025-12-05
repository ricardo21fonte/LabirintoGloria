package game;

import Lists.UnorderedLinkedList; // A tua lista para o histórico
import enums.TipoDivisao;

public class Player {
    private String nome;
    private Divisao localAtual;
    private UnorderedLinkedList<String> historico; // Requisito do relatório 
    private int jogadasExtra; // Para eventos
    private boolean bloqueado; // Para eventos

    public Player(String nome, Divisao inicio) {
        this.nome = nome;
        this.localAtual = inicio;
        this.historico = new UnorderedLinkedList<>();
        this.jogadasExtra = 0;
        this.bloqueado = false;
        
        // Regista o ponto de partida
        historico.addToRear("Inicio: " + inicio.getNome());
    }

    public void moverPara(Divisao novaSala) {
        this.localAtual = novaSala;
        historico.addToRear("Moveu para: " + novaSala.getNome());
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public Divisao getLocalAtual() { return localAtual; }
    public UnorderedLinkedList<String> getHistorico() { return historico; }
    
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }
    public boolean isBloqueado() { return bloqueado; }

    @Override
    public String toString() {
        return "Jogador " + nome + " na sala: " + localAtual.getNome();
    }
}