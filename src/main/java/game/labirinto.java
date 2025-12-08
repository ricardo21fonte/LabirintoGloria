package game;

import Lists.ArrayUnorderedList; // A tua lista para guardar os jogadores
import enums.TipoDivisao;
import enums.CorredorEvento;
import enums.Dificuldade;
import game.EventoAleatorio;     // já usas em baixo
// TipoEvento já não é necessário aqui se só usares no teste de debug
import enums.TipoEvento;

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

        Divisao origem = jogador.getLocalAtual();

        System.out.println("\n--- JOGADA DE " + jogador.getNome() + " ---");
        System.out.println("Tenta ir de " + origem.getNome() +
                " para " + destino.getNome());

        // 1) EVENTO ALEATÓRIO NO CORREDOR (ANTES DE ENTRAR NA SALA)
        double probEventoAleatorio = 0.05; // 5%
        if (!jogoTerminado && Math.random() < probEventoAleatorio) {
            EventoAleatorio ev = EventoAleatorio.gerarAleatorio();

            System.out.println("\n✨ EVENTO ALEATÓRIO NO CORREDOR! ✨");
            System.out.println("Tipo: " + ev.getTipo() + " | " + ev.getDescricao());

            // aplica efeitos (recuar, bloquear, trocar posições, etc.)
            ev.aplicar(jogador, jogadores);

            // SE o evento mexeu com a posição ou bloqueou, cancela o movimento
            if (!jogador.getLocalAtual().equals(origem)) {
                System.out.println("⛔ O evento aleatório desviou-te do corredor.");
                return;
            }
            if (jogador.isBloqueado()) {
                System.out.println("⛔ O evento aleatório bloqueou-te antes de entrares na sala.");
                return;
            }
        }

        // 2) EVENTO DO CORREDOR (trancas, armadilhas, etc.)
        EventoCorredor eventoCorredor = mapa.getCorredorEvento(origem, destino);
        CorredorEvento tipoEvento = eventoCorredor.getTipo();

        if (tipoEvento != CorredorEvento.NONE) {
            System.out.println("⚠️ EVENTO NO CORREDOR: " + tipoEvento +
                    " (valor = " + eventoCorredor.getValor() + ")");
        }

        switch (tipoEvento) {
            case MOVE_BACK:
                System.out.println("🚫 ARMADILHA! O jogador é empurrado para trás!");
                jogador.recuar(eventoCorredor.getValor());
                // não entra na sala
                break;

            case BLOCK_TURN:
                int t = eventoCorredor.getValor();
                if (t <= 0) t = 1;
                System.out.println("⏳ O jogador ficou preso! Perde " + t + " turno(s).");
                jogador.bloquear(t);
                // aqui podes decidir se entra na sala ou não; vou manter como antes:
                jogador.moverPara(destino);
                break;

            case NONE:
            default:
                System.out.println("✅ Caminho seguro.");
                jogador.moverPara(destino);
                break;
        }

        // 3) Verificar vitória (depois de tudo)
        if (jogador.getLocalAtual().getTipo() == TipoDivisao.SALA_CENTRAL) {
            System.out.println("\n🏆 PARABÉNS! " + jogador.getNome() + " ENCONTROU O TESOURO! 🏆");
            jogoTerminado = true;
        }
    }

}