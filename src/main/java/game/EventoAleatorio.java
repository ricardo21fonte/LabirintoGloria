package game;

import enums.TipoEvento;
import Lists.ArrayUnorderedList;
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
                trocarPosicaoComOutro(alvo, todosJogadores);
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

        // Guardar as posições atuais
        for (Player j : jogadores) {
            posicoesOriginais[i] = j.getLocalAtual();
            i++;
        }

        // Atribuir novas posições
        i = 0;
        for (Player j : jogadores) {
            Divisao novaPosicao = posicoesOriginais[(i + 1) % n];
            j.setLocalAtual(novaPosicao);
            i++;
        }

        // Após a troca marca limite de recuo em cada jogador
        for (Player j : jogadores) {
            j.marcarLimiteRecuo();
        }
    }

    /**
     * Swaps the position of the target player with a randomly chosen other player.
     * @param alvo      the player who triggered the event
     * @param jogadores list of all players in the game
     */
    private void trocarPosicaoComOutro(Player alvo, ArrayUnorderedList<Player> jogadores) {
        int n = jogadores.size();
        if (n < 2) return;

        // Encontrar índice
        int indiceAlvo = -1;
        int idx = 0;
        for (Player p : jogadores) {
            if (p == alvo) {
                indiceAlvo = idx;
                break;
            }
            idx++;
        }
        if (indiceAlvo == -1) return;

        // Escolher outro índice aleatório
        int indiceOutro;
        do {
            indiceOutro = (int) (Math.random() * n);
        } while (indiceOutro == indiceAlvo);

        // Encontrar o jogador com esse índice
        Player outro = null;
        idx = 0;
        for (Player p : jogadores) {
            if (idx == indiceOutro) {
                outro = p;
                break;
            }
            idx++;
        }
        if (outro == null) return;

        Divisao posAlvo = alvo.getLocalAtual();
        Divisao posOutro = outro.getLocalAtual();

        // Troca de posição
        alvo.setLocalAtual(posOutro);
        outro.setLocalAtual(posAlvo);

        // Marca limite de recuo para ambos
        alvo.marcarLimiteRecuo();
        outro.marcarLimiteRecuo();

        System.out.println(alvo.getNome() + " trocou de posição com " + outro.getNome() + "!");
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

    /**
     * Returns the intensity of this event.
     * @return the event intensity
     */
    public int getIntensidade() {
        return intensidade;
    }
}