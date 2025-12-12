package game;

import enums.Dificuldade;
/**
 * Represents a riddle (enigma) that can be presented to a player or bot.
 */
public class Enigma {
    /**
     * The question to be shown to the player.
     */
    private String pergunta;

    /**
     * Array of possible answer options.
     */
    private String[] opcoes;

    /**
     * Index of the correct option
     */
    private int indiceCorreto;

    /**
     * Difficulty level of the riddle.
     */
    private Dificuldade dificuldade;

    /**
     * Effect code applied when the player answers correctly.
     */
    private String efeitoSucesso;

    /**
     * Effect code applied when the player answers incorrectly.
     */
    private String efeitoFalha;
    /**
     * Creates a new Enigma with the given question, options, correct answer index and difficulty.
     * @param pergunta       the question text
     * @param opcoes         the list of possible options
     * @param indiceCorreto  the index of the correct option in opcoes
     * @param dificuldade    the difficulty level of this riddle
     */
    public Enigma(String pergunta, String[] opcoes, int indiceCorreto, Dificuldade dificuldade) {
        this.pergunta = pergunta;
        this.opcoes = opcoes;
        this.indiceCorreto = indiceCorreto;
        this.dificuldade = dificuldade;
        this.efeitoSucesso = "NONE";
        this.efeitoFalha = "BLOCK";
    }

    /**
     * Sets the effect codes for success and failure.
     * @param sucesso the effect code to apply when the riddle is answered correctly
     * @param falha   the effect code to apply when the riddle is answered incorrectly
     */
    public void setEfeitos(String sucesso, String falha) {
        this.efeitoSucesso = sucesso;
        this.efeitoFalha = falha;
    }

    /**
     * Returns the effect code applied when the riddle is answered correctly.
     * @return the success effect code
     */
    public String getEfeitoSucesso() { return efeitoSucesso; }
    /**
     * Returns the effect code applied when the riddle is answered incorrectly.
     * @return the failure effect code
     */
    public String getEfeitoFalha() { return efeitoFalha; }

    /**
     * Returns the question text.
     * @return the question
     */
    public String getPergunta() { return pergunta; }

    /**
     * Returns the array of possible options.
     * @return the options array
     */
    public String[] getOpcoes() { return opcoes; }

    /**
     * Returns the difficulty level of this riddle
     * @return the difficulty
     */
    public Dificuldade getDificuldade() { return dificuldade; }

    /**
     * Verifies whether the chosen option corresponds to the correct answer.
     * @param opcaoEscolhida the chosen option number (1-based)
     * @return true if the chosen option is the correct one, false otherwise
     */
    public boolean verificarResposta(int opcaoEscolhida) {
        return (opcaoEscolhida - 1) == indiceCorreto;
    }
}