package game;

import enums.TipoEvento;
import Lists.ArrayUnorderedList;

public class EventoAleatorio {
    private TipoEvento tipo;
    private int intensidade;
    private String descricao;

    public EventoAleatorio(TipoEvento tipo, int intensidade) {
        this.tipo = tipo;
        this.intensidade = intensidade;
        gerarDescricao();
    }

    private void gerarDescricao() {
        switch (tipo) {
            case JOGADA_EXTRA:
                descricao = "Ganhou " + intensidade + " jogada(s) extra!";
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

    public void aplicar(Player alvo, ArrayUnorderedList<Player> todosJogadores) {
        switch (tipo) {
            case JOGADA_EXTRA:
                alvo.adicionarJogadasExtras(intensidade);
                break;

            case RECUAR:
                alvo.recuar(intensidade);
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

    // Rotação de posições entre todos os jogadores
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

        // Atribuir novas posições (rotação) + registar no histórico via setLocalAtual
        i = 0;
        for (Player j : jogadores) {
            Divisao novaPosicao = posicoesOriginais[(i + 1) % n];
            j.setLocalAtual(novaPosicao);
            i++;
        }

        // 🔴 NOVO: após a troca de todos, marcar o limite de recuo em cada jogador
        for (Player j : jogadores) {
            j.marcarLimiteRecuo();
        }
    }

    // Trocar posição com outro jogador aleatório
    private void trocarPosicaoComOutro(Player alvo, ArrayUnorderedList<Player> jogadores) {
        int n = jogadores.size();
        if (n < 2) return;

        // Encontrar índice do alvo
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

        // Troca efetiva de posição (também atualiza stack e histórico)
        alvo.setLocalAtual(posOutro);
        outro.setLocalAtual(posAlvo);

        // 🔴 NOVO: marcar limite de recuo para ambos
        alvo.marcarLimiteRecuo();
        outro.marcarLimiteRecuo();

        System.out.println(alvo.getNome() + " trocou de posição com " + outro.getNome() + "!");
    }

    public static EventoAleatorio gerarAleatorio() {
        TipoEvento[] tipos = TipoEvento.values();
        int indice = (int) (Math.random() * tipos.length);
        int intensidade = 1 + (int) (Math.random() * 3);
        return new EventoAleatorio(tipos[indice], intensidade);
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getIntensidade() {
        return intensidade;
    }
}
