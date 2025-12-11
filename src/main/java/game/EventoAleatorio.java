package game;

import Lists.ArrayUnorderedList;
import enums.TipoEvento;
import ui.GameView;
/**
 * Represents a random event that can affect one or more players in the game.
 */
public class EventoAleatorio {
    /**
     * Type of this event
     */
    private TipoEvento tipo;

    /**
     * Intensity of this event.
     */
    private int intensidade;

    /**
     * Description of this event
     */
    private String descricao;

    /**
     * Creates a new random event with the given type and intensity.
     * @param tipo        the type of the event
     * @param intensidade the intensity of the event
     */
    public EventoAleatorio(TipoEvento tipo, int intensidade) {
        this.tipo = tipo;
        this.intensidade = intensidade;
        gerarDescricao();
    }
    /**
     * Generates a textual description of this event based on its type and intensity.
     * This method is called automatically in the constructor.
     */
    private void gerarDescricao() {
        switch (tipo) {
            case JOGADA_EXTRA:
                descricao = "Ganhou " + intensidade + " jogada(s) na próxima jogada extra!";
                break;
            case TROCAR_POSICAO:
                descricao = "Vai trocar de posição com outro jogador!";
                break;
            case RECUAR:
                descricao = "Recua " + intensidade + " casa(s)!";
                break;
            case BLOQUEAR_TURNOS:
                descricao = "Fica bloqueado por " + intensidade + " turno(s)!";
                break;
            case TROCAR_TODOS:
                descricao = "Todos os jogadores trocam de posições!";
                break;
            case SEM_EVENTO:
            default:
                descricao = "Nada acontece...";
                break;
        }
    }

    /**
     * Applies the event to the given target player and to all players in the game.
     * @param alvo           the main target player of the event
     * @param todosJogadores list of all players currently in the game
     * @param view  the view used to display feedback about the recoil action
     */
    public void aplicar(Player alvo, ArrayUnorderedList<Player> todosJogadores, GameView view) {
        switch (tipo) {
            case JOGADA_EXTRA:
                alvo.adicionarJogadasExtras(intensidade);
                break;

            case RECUAR:
                alvo.recuar(intensidade, view);
                break;

            case BLOQUEAR_TURNOS:
                alvo.bloquear(intensidade);
                break;

            case TROCAR_TODOS:
                trocarTodasPosicoes(todosJogadores);
                break;

            case TROCAR_POSICAO:
                trocarPosicaoComOutro(alvo, todosJogadores, view);
                break;

            case SEM_EVENTO:
            default:
                break;
        }
    }

    /**
     * Rotates the positions of all players in the list.
     * @param jogadores list of all players to rotate
     */
    private void trocarTodasPosicoes(ArrayUnorderedList<Player> jogadores) {
        int n = jogadores.size();
        if (n < 2) return;

        Divisao[] posicoesOriginais = new Divisao[n];
        int i = 0;

        // Guarda posições atuais
        for (Player j : jogadores) {
            posicoesOriginais[i] = j.getLocalAtual();
            i++;
        }

        // Atribui novas posições
        i = 0;
        for (Player j : jogadores) {
            Divisao novaPosicao = posicoesOriginais[(i + 1) % n];
            j.setLocalAtual(novaPosicao);
            i++;
        }

        // define limite de recuo em cada jogador
        for (Player j : jogadores) {
            j.marcarLimiteRecuo();
        }
    }

    /**
     * Swaps the position of the target player with a chosen other player.
     */
    private void trocarPosicaoComOutro(Player alvo, ArrayUnorderedList<Player> jogadores, GameView view) {
        if (jogadores.size() < 2) return;

        Player alvoTroca = alvo.escolherAlvoParaTroca(jogadores, view);

        if (alvoTroca == null || alvoTroca.equals(alvo)) {
            System.out.println("   (Troca cancelada ou alvo inválido)");
            return;
        }

        Divisao posAlvo = alvo.getLocalAtual();
        Divisao posOutro = alvoTroca.getLocalAtual();

        // Troca de posição
        alvo.setLocalAtual(posOutro);
        alvoTroca.setLocalAtual(posAlvo);

        // Define limite de recuo para ambos
        alvo.marcarLimiteRecuo();
        alvoTroca.marcarLimiteRecuo();

        System.out.println(alvo.getNome() + " trocou de posição com " + alvoTroca.getNome() + "!");
    }
    /**
     * Generates a new random event with a random type and intensity.
     * @return a newly created random EventoAleatorio
     */
    public static EventoAleatorio gerarAleatorio() {
        TipoEvento[] tipos = TipoEvento.values();
        int indice = (int) (Math.random() * tipos.length);
        int intensidade = 1 + (int) (Math.random() * 3);
        return new EventoAleatorio(tipos[indice], intensidade);
    }
    /**
     * Returns the type of this event.
     * @return the event type
     */
    public TipoEvento getTipo() {
        return tipo;
    }
    /**
     * Returns the description of this event.
     * @return the event description
     */
    public String getDescricao() {
        return descricao;
    }

}