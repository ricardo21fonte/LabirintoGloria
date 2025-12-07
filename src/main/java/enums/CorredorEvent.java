package enums;

// O import da LinkedList não é necessário aqui, podes remover.

public class CorredorEvent {
    
    // O Enum define O QUE ACONTECE
    public enum Type {
        NONE,           // Nada acontece
        EXTRA_TURN,     // Ganhar jogada extra 
        SWAP_POSITION,  // Trocar com outro jogador 
        MOVE_BACK,      // Recuar casas 
        BLOCK_TURN , 
        LOCKED    // Ficar impedido de jogar 
    }

    private Type type;
    private int value; // Guarda a "intensidade" (ex: recuar 3 casas)

    public CorredorEvent(Type type, int value) {
        this.type = type;
        this.value = value;
    }

    // --- GETTERS (Necessários para o jogo ler o evento) ---
    public Type getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (type == Type.NONE) return "Corredor Seguro";
        return "Evento: " + type + " (Valor: " + value + ")";
    }
}