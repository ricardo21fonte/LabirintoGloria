package game;
import enums.TipoDivisao;

public class Divisao {
    private String nome;
    private TipoDivisao tipo;
    
    // Em vez de int id, guardamos o objeto complexo
    private Alavanca alavancaDoPuzzle; 
    // MAS precisamos na mesma do ID para saber QUE porta abrir
    private int idDaPortaQueAbre = -1;

    public Divisao(String nome, TipoDivisao tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    // --- GETTERS E SETTERS ---
    public void setAlavanca(Alavanca a) { this.alavancaDoPuzzle = a; }
    public Alavanca getAlavanca() { return alavancaDoPuzzle; }

    public void setIdDesbloqueio(int id) { this.idDaPortaQueAbre = id; }
    public int getIdDesbloqueio() { return idDaPortaQueAbre; }

    public String getNome() { return nome; }
    public TipoDivisao getTipo() { return tipo; }
}