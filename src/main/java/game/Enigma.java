package game;

import enums.Dificuldade;

public class Enigma {
    private String pergunta;
    private String[] opcoes;
    private int indiceCorreto;
    private Dificuldade dificuldade;

    // --- NOVOS CAMPOS PARA OS EFEITOS ---
    // Guardam o código do que acontece (ex: "SWAP", "BACK:2", "EXTRA_TURN")
    private String efeitoSucesso;
    private String efeitoFalha;

    public Enigma(String pergunta, String[] opcoes, int indiceCorreto, Dificuldade dificuldade) {
        this.pergunta = pergunta;
        this.opcoes = opcoes;
        this.indiceCorreto = indiceCorreto;
        this.dificuldade = dificuldade;

        // Valores por defeito (para não dar erro se o JSON não tiver nada)
        this.efeitoSucesso = "NONE";   // Se acertar, não acontece nada extra
        this.efeitoFalha = "BLOCK";    // Se errar, perde o turno (padrão)
    }

    // --- MÉTODOS NOVOS ---
    public void setEfeitos(String sucesso, String falha) {
        this.efeitoSucesso = sucesso;
        this.efeitoFalha = falha;
    }

    public String getEfeitoSucesso() { return efeitoSucesso; }
    public String getEfeitoFalha() { return efeitoFalha; }

    // --- MÉTODOS ANTIGOS (Mantêm-se iguais) ---
    public String getPergunta() { return pergunta; }
    public String[] getOpcoes() { return opcoes; }
    public Dificuldade getDificuldade() { return dificuldade; }

    public boolean verificarResposta(int opcaoEscolhida) {
        // O utilizador escolhe 1, 2, 3... nós convertemos para índice 0, 1, 2...
        return (opcaoEscolhida - 1) == indiceCorreto;
    }
}