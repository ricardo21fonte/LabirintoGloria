package game;

import enums.TipoDivisao;

public class Divisao {
    
    // --- LÓGICA DE IDENTIDADE ÚNICA ---
    private static int nextId = 1; // Contador global partilhado
    private int id;                // ID único desta instância específica

    private String nome;
    private TipoDivisao tipo;
    
    // Objetos e Variáveis de Puzzle
    private Alavanca alavancaDoPuzzle; 
    private int idDaPortaQueAbre = -1; 

    public Divisao(String nome, TipoDivisao tipo) {
        this.id = nextId++; // Por defeito, atribui novo ID e incrementa
        this.nome = nome;
        this.tipo = tipo;
    }

    // --- MÉTODOS DE CORREÇÃO DE ID (CRÍTICOS PARA LOAD) ---

    /**
     * Permite ao MapLoader atualizar o contador global para evitar duplicados.
     * @param valor O novo valor para o próximo ID a ser gerado.
     */
    public static void setNextId(int valor) {
        nextId = valor;
    }

    /**
     * Permite forçar um ID específico ao carregar de ficheiro.
     * @param id O ID lido do JSON (ex: 50 se o código for "S50").
     */
    public void definirIdManual(int id) {
        this.id = id;
    }
    // -----------------------------------------------------

    public int getId() { return id; }

    public void setAlavanca(Alavanca a) { this.alavancaDoPuzzle = a; }
    public Alavanca getAlavanca() { return alavancaDoPuzzle; }

    public void setIdDesbloqueio(int id) { this.idDaPortaQueAbre = id; }
    public int getIdDesbloqueio() { return idDaPortaQueAbre; }

    public String getNome() { return nome; }
    public TipoDivisao getTipo() { return tipo; }

    @Override
    public String toString() {
        return nome + " [" + tipo + "] (ID:" + id + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Divisao outra = (Divisao) obj;
        return this.id == outra.id; 
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}