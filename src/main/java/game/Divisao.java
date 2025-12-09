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
    private int idDaPortaQueAbre = -1; // ID da tranca que esta sala abre (se for SALA_ALAVANCA)

    public Divisao(String nome, TipoDivisao tipo) {
        this.id = nextId++; // Atribui um ID novo e incrementa o contador
        this.nome = nome;
        this.tipo = tipo;
    }

    // --- GETTERS E SETTERS ---

    public void setAlavanca(Alavanca a) { 
        this.alavancaDoPuzzle = a; 
    }
    
    public Alavanca getAlavanca() { 
        return alavancaDoPuzzle; 
    }

    public void setIdDesbloqueio(int id) { 
        this.idDaPortaQueAbre = id; 
    }
    
    public int getIdDesbloqueio() { 
        return idDaPortaQueAbre; 
    }

    public String getNome() { 
        return nome; 
    }
    
    public TipoDivisao getTipo() { 
        return tipo; 
    }

    // --- MÉTODOS OBRIGATÓRIOS PARA O GRAFO ---

    @Override
    public String toString() {
        return nome + " [" + tipo + "] (ID:" + id + ")";
    }

    /**
     * O método EQUALS agora compara pelo ID ÚNICO.
     * Assim, "Corredor 1" (ID 5) é diferente de "Corredor 1" (ID 50).
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Divisao outra = (Divisao) obj;
        return this.id == outra.id; // Compara IDs, não nomes!
    }

    /**
     * Boa prática: se mudamos o equals, devemos mudar o hashCode.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}