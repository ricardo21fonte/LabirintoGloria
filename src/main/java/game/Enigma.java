package game; // <--- Isto diz que ele mora no pacote 'game'

import enums.Dificuldade;

public class Enigma {
    private String pergunta;
    private String[] opcoes;
    private int indiceCorreto;
    private Dificuldade dificuldade;

    public Enigma(String pergunta, String[] opcoes, int indiceCorreto, Dificuldade dificuldade) {
        this.pergunta = pergunta;
        this.opcoes = opcoes;
        this.indiceCorreto = indiceCorreto;
        this.dificuldade = dificuldade;
    }

    // Getters
    public String getPergunta() { return pergunta; }
    public String[] getOpcoes() { return opcoes; }
    public Dificuldade getDificuldade() { return dificuldade; }

    public boolean verificarResposta(int opcaoEscolhida) {
        return (opcaoEscolhida - 1) == indiceCorreto;
    }
}