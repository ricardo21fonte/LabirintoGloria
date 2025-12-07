package game;

import enums.TipoDivisao;

public class Divisao {
    private String nome;
    private TipoDivisao tipo;
    private int idDesbloqueio;
    
    // Podes adicionar: private String desafio; (para enigmas)

    public Divisao(String nome, TipoDivisao tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public String getNome() { return nome; }
    public TipoDivisao getTipo() { return tipo; }

    public int getIdDesbloqueio() {
        return idDesbloqueio;
    }

    public void setIdDesbloqueio(int idDesbloqueio) {
        this.idDesbloqueio = idDesbloqueio;
    }

    // --- CRUCIAL PARA O GRAFO FUNCIONAR ---
    @Override
    public String toString() {
        return nome + " [" + tipo + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Divisao outra = (Divisao) obj;
        return this.nome.equals(outra.nome); // O nome define a unicidade
    }
}