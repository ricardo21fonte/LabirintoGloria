package game;

import enums.AlavancaEnum;

public class Alavanca {

    private static final int NUM_ALAVANCAS = 3;

    // posição 0 → alavanca 1
    // posição 1 → alavanca 2
    // posição 2 → alavanca 3
    private AlavancaEnum[] efeitos;

    public Alavanca() {
        efeitos = new AlavancaEnum[NUM_ALAVANCAS];

        // Definir 1 de cada tipo
        efeitos[0] = AlavancaEnum.ABRIR_PORTA;
        efeitos[1] = AlavancaEnum.PENALIZAR;
        efeitos[2] = AlavancaEnum.NADA;

        // Baralhar posições para não ser sempre a mesma ordem
        baralhar();
    }

    private void baralhar() {
        for (int i = 0; i < efeitos.length; i++) {
            int j = (int)(Math.random() * efeitos.length);
            AlavancaEnum tmp = efeitos[i];
            efeitos[i] = efeitos[j];
            efeitos[j] = tmp;
        }
    }

    /**
     
@param escolha 1, 2 ou 3*/
  public AlavancaEnum ativar(int escolha) {
      if (escolha < 1 || escolha > NUM_ALAVANCAS) {
          return AlavancaEnum.NADA;}
      return efeitos[escolha - 1];}

    public int getNumAlavancas() {
        return NUM_ALAVANCAS;
    }
}