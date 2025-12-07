package game;

import Lists.ArrayUnorderedList; // A tua lista para guardar os jogadores
import enums.TipoDivisao;
import enums.CorredorEvent;
import enums.Dificuldade;
import enums.TipoEvento;
import game.EventoAleatorio;

public class labirinto {

    private LabyrinthGraph<Divisao> mapa;
    private ArrayUnorderedList<Player> jogadores;
    private boolean jogoTerminado;
    private ArrayUnorderedList<Enigma> enigmasDisponiveis;

    public labirinto() {
        this.jogadores = new ArrayUnorderedList<>();
        this.enigmasDisponiveis = new ArrayUnorderedList<>();
        this.jogoTerminado = false;
    }

    public void setEnigmas(ArrayUnorderedList<Enigma> enigmas) {
        this.enigmasDisponiveis = enigmas;
    }

    public void setMapa(LabyrinthGraph<Divisao> mapa) {
        this.mapa = mapa;
    }

    public void adicionarJogador(Player jogador) {
        jogadores.addToRear(jogador);
    }

    public Enigma obterEnigma(Dificuldade difAlvo) {
        if (enigmasDisponiveis.isEmpty()) return null;

        Lists.ArrayUnorderedList<Enigma> candidatos = new Lists.ArrayUnorderedList<>();

        for (Enigma e : enigmasDisponiveis) {
            if (e.getDificuldade() == difAlvo) {
                candidatos.addToRear(e);
            }
        }

        if (candidatos.isEmpty()) {
            System.out.println("(Não há mais enigmas desta dificuldade!)");
            return null;
        }

        int totalCandidatos = candidatos.size();
        int indiceSorteado = (int) (Math.random() * totalCandidatos);

        Enigma enigmaEscolhido = null;
        int idx = 0;
        for (Enigma e : candidatos) {
            if (idx == indiceSorteado) {
                enigmaEscolhido = e;
                break;
            }
            idx++;
        }

        if (enigmaEscolhido != null) {
            enigmasDisponiveis.remove(enigmaEscolhido);
        }

        return enigmaEscolhido;
    }

    // --- LÓGICA DO JOGO ---
    public void realizarJogada(Player jogador, Divisao destino) {
        if (jogoTerminado) {
            System.out.println("O jogo já acabou!");
            return;
        }

        System.out.println("\n--- JOGADA DE " + jogador.getNome() + " ---");
        System.out.println("Tenta ir de " + jogador.getLocalAtual().getNome() +
                " para " + destino.getNome());

        // 1) Evento fixo do corredor (CorredorEvent vindo do grafo)
        CorredorEvent evento = mapa.getCorridorEvent(jogador.getLocalAtual(), destino);

        if (evento.getType() != CorredorEvent.Type.NONE) {
            System.out.println("⚠️ EVENTO ENCONTRADO NO CORREDOR: " + evento.getType());
            System.out.println("Valor do evento: " + evento.getValue());
        }

        switch (evento.getType()) {
            case MOVE_BACK:
                System.out.println("🚫 ARMADILHA! O jogador é empurrado para trás!");
                // Usa a stack do Player para recuar
                jogador.recuar(evento.getValue());
                // NÃO avança para o destino
                break;

            case BLOCK_TURN:
                System.out.println("⏳ O jogador ficou preso! Perde a vez.");
                jogador.setBloqueado(true);
                jogador.moverPara(destino); // Avança, mas fica bloqueado
                break;

            case NONE:
            default:
                System.out.println("✅ Caminho seguro.");
                jogador.moverPara(destino);
                break;
        }

        // 2) Evento ALEATÓRIO adicional no corredor (a tua classe EventoAleatorio)
        double probEventoAleatorio = 0.4; // 40% de probabilidade, ajusta se quiseres

        if (!jogoTerminado && Math.random() < probEventoAleatorio) {
            // MODO NORMAL: evento aleatório
            EventoAleatorio ev = EventoAleatorio.gerarAleatorio();

            // Se quiseres testar um tipo específico, podes trocar por:
            // EventoAleatorio ev = new EventoAleatorio(TipoEvento.RECUAR, 1);

            System.out.println("\n✨ EVENTO ALEATÓRIO NO CORREDOR! ✨");
            System.out.println("Tipo: " + ev.getTipo() + " | " + ev.getDescricao());

            ev.aplicar(jogador, jogadores);
        }

        // 3) Verificar vitória
        if (jogador.getLocalAtual().getTipo() == TipoDivisao.SALA_CENTRAL) {
            System.out.println("\n🏆 PARABÉNS! " + jogador.getNome() + " ENCONTROU O TESOURO! 🏆");
            jogoTerminado = true;
        }
    }
}
