package game;

import Lists.ArrayUnorderedList; // A tua lista para guardar os jogadores
import enums.TipoDivisao;
import enums.CorredorEvent;
import enums.Dificuldade;

public class labirinto {

    // O Tabuleiro (O Grafo que já tens)
    private LabyrinthGraph<Divisao> mapa;
    // Os Jogadores
    private ArrayUnorderedList<Player> jogadores;
    // Estado
    private boolean jogoTerminado;
    // --- NOVO: Lista de Enigmas ---
    private ArrayUnorderedList<Enigma> enigmasDisponiveis;

    public labirinto() {
        this.jogadores = new ArrayUnorderedList<>();
        this.jogoTerminado = false;
    }

    // --- NOVO: Método para receber os enigmas do Loader ---
    public void setEnigmas(ArrayUnorderedList<Enigma> enigmas) {
        this.enigmasDisponiveis = enigmas;
    }

    // --- NOVO: Método para obter um enigma (pela dificuldade ou o primeiro que aparecer) ---
    public Enigma obterEnigma(Dificuldade difAlvo) {
        if (enigmasDisponiveis.isEmpty()) return null;

        // Tenta encontrar um enigma da dificuldade pedida
        java.util.Iterator<Enigma> it = enigmasDisponiveis.iterator();
        while (it.hasNext()) {
            Enigma e = it.next();
            if (e.getDificuldade() == difAlvo) {
                // Remover da lista para não repetir (regra do enunciado)
                enigmasDisponiveis.remove(e); 
                return e;
            }
        }
        
        // Se não houver dessa dificuldade, devolve o primeiro que houver (fallback)
        Enigma e = enigmasDisponiveis.first();
        enigmasDisponiveis.removeFirst();
        return e;
    }

    // --- CONFIGURAÇÃO ---
    public void setMapa(LabyrinthGraph<Divisao> mapa) {
        this.mapa = mapa;
    }

    public void adicionarJogador(Player jogador) {
        jogadores.addToRear(jogador);
    }

    // --- LÓGICA DO JOGO (Onde a magia acontece) ---

    /**
     * Tenta mover o jogador para uma nova sala
     */
    public void realizarJogada(Player jogador, Divisao destino) {
        if (jogoTerminado) {
            System.out.println("O jogo já acabou!");
            return;
        }

        // 1. Verificar se o caminho existe (É vizinho?)
        // (Nota: Precisas de garantir que o Grafo tem um método 'isConnected' ou similar. 
        // Se não tiveres, assume-se para já que o Main manda movimentos válidos).
        
        System.out.println("\n--- JOGADA DE " + jogador.getNome() + " ---");
        System.out.println("Tenta ir de " + jogador.getLocalAtual().getNome() + " para " + destino.getNome());

        // 2. Verificar Eventos no Corredor [cite: 38]
        // Aqui usamos o teu LabyrinthGraph para saber o que há no caminho
        CorredorEvent evento = mapa.getCorridorEvent(jogador.getLocalAtual(), destino);

        if (evento.getType() != CorredorEvent.Type.NONE) {
            System.out.println("⚠️ EVENTO ENCONTRADO: " + evento.getType());
            System.out.println("Valor do evento: " + evento.getValue());
        }

        // 3. Aplicar Regras do Evento
        switch (evento.getType()) {
            case MOVE_BACK: // Exemplo: Recuar casas [cite: 38]
                System.out.println("🚫 ARMADILHA! O jogador é empurrado para trás!");
                // Lógica de recuar: O jogador NÃO avança para o destino.
                // Pode ficar onde está ou recuar para a sala anterior (depende da tua lógica de histórico)
                break;
                
            case BLOCK_TURN: // Exemplo: Perder turnos [cite: 38]
                System.out.println("⏳ O jogador ficou preso! Perde a vez.");
                jogador.setBloqueado(true);
                jogador.moverPara(destino); // Avança, mas fica bloqueado
                break;

            case NONE:
            default:
                // Caminho limpo, o jogador avança
                System.out.println("✅ Caminho seguro.");
                jogador.moverPara(destino);
                break;
        }

        // 4. Verificar Vitória [cite: 46]
        if (jogador.getLocalAtual().getTipo() == TipoDivisao.SALA_CENTRAL) {
            System.out.println("\n🏆 PARABÉNS! " + jogador.getNome() + " ENCONTROU O TESOURO! 🏆");
            jogoTerminado = true;
        }
    }
}