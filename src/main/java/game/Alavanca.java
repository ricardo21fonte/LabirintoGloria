package game;

import enums.AlavancaEnum;
/**
 * Represents a lever puzzle with a fixed number of levers, each associated
 * with a specific effect.
 */
public class Alavanca {
    /**
     * Total number of levers in this puzzle.
     */
    private static final int NUM_ALAVANCAS = 3;

    /**
     * Array of lever effects.
     */
    private AlavancaEnum[] efeitos;


    /**
     * Creates a new lever puzzle with exactly one lever of each type:
     * The effects are then shuffled so that the mapping between lever number and effect is random for each new instance.
     */
    public Alavanca() {

        efeitos = new AlavancaEnum[NUM_ALAVANCAS];

        efeitos[0] = AlavancaEnum.ABRIR_PORTA;
        efeitos[1] = AlavancaEnum.PENALIZAR;
        efeitos[2] = AlavancaEnum.NADA;

        // Shuffle positions
        baralhar();
    }
    /**
     * Randomly shuffles the internal array of lever effects.
     */
    private void baralhar() {
        for (int i = 0; i < efeitos.length; i++) {
            int j = (int)(Math.random() * efeitos.length);
            AlavancaEnum tmp = efeitos[i];
            efeitos[i] = efeitos[j];
            efeitos[j] = tmp;
        }
    }

    /**
     * Activates one of the levers based on the player's choice
     * @param escolha the lever number chosen by the player;
     * @return the  effect associated with the chosen lever;
     */
    public AlavancaEnum ativar(int escolha) {
            if (escolha < 1 || escolha > NUM_ALAVANCAS) {
                return AlavancaEnum.NADA;
            }
            return efeitos[escolha - 1];
        }


}