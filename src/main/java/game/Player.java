package game;

import Lists.UnorderedLinkedList;
import Stacks.LinkedStack;
import Exceptions.EmptyCollectionException;
import enums.TipoDivisao;

public class Player {
    private String nome;
    private Divisao localAtual;
    private UnorderedLinkedList<String> historico;
    private int jogadasExtra;
    private boolean bloqueado;

    private LinkedStack<Divisao> caminho;

    // tamanho mínimo da stack até onde é permitido recuar
    private int limiteRecuoMinSize;

    // =========================
    //       CONSTRUTOR
    // =========================
    public Player(String nome, Divisao inicio) {
        this.nome = nome;
        this.localAtual = inicio;
        this.historico = new UnorderedLinkedList<>();
        this.jogadasExtra = 0;
        this.bloqueado = false;

        this.caminho = new LinkedStack<>();
        caminho.push(inicio);

        // No início, o limite é 1 (não pode recuar para antes da sala inicial)
        this.limiteRecuoMinSize = 1;

        historico.addToRear("Inicio: " + inicio.getNome());
    }

    // =========================
    //       LÓGICA DE JOGO
    // =========================

    public void moverPara(Divisao novaSala) {
        this.localAtual = novaSala;
        historico.addToRear("Moveu para: " + novaSala.getNome());
        caminho.push(novaSala);
    }

    // RECUAR COM LIMITE
    public void recuar(int casas) {
        if (caminho == null) {
            System.out.println(nome + " não tem caminho registado para recuar.");
            return;
        }

        if (caminho.size() <= limiteRecuoMinSize) {
            System.out.println(nome + " não pode recuar mais (já está no limite de recuo).");
            return;
        }

        int passos = casas;

        while (passos > 0 && caminho.size() > limiteRecuoMinSize) {
            try {
                caminho.pop();
            } catch (EmptyCollectionException e) {
                break;
            }
            passos--;
        }

        try {
            Divisao novaPosicao = caminho.peek();
            this.localAtual = novaPosicao;
            historico.addToRear("Recuou para: " + novaPosicao.getNome());
            System.out.println(nome + " recuou até " + novaPosicao.getNome());
        } catch (EmptyCollectionException e) {
            System.out.println("Erro ao recuar: pilha vazia inesperadamente.");
        }
    }

    public void bloquear(int turnos) {
        this.bloqueado = true;
        System.out.println(nome + " está bloqueado!");
    }

    public void setLocalAtual(Divisao novaSala) {
        this.localAtual = novaSala;
        historico.addToRear("Teleportado para: " + novaSala.getNome());
        System.out.println(nome + " foi teleportado para " + novaSala.getNome());

        if (caminho == null) {
            caminho = new LinkedStack<>();
        }
        caminho.push(novaSala);
    }

    // chamado quando há troca de posição com outro jogador
    public void marcarLimiteRecuo() {
        this.limiteRecuoMinSize = caminho.size();
    }

    // =========================
    //     GETTERS / SETTERS
    // =========================

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Divisao getLocalAtual() {
        return localAtual;
    }

    // setLocalAtual já existe acima com lógica extra (teleporte),
    // por isso não criamos um setter "simples" separado para não confundir.

    public UnorderedLinkedList<String> getHistorico() {
        return historico;
    }

    public void setHistorico(UnorderedLinkedList<String> historico) {
        this.historico = historico;
    }

    public int getJogadasExtra() {
        return jogadasExtra;
    }

    public void setJogadasExtra(int jogadasExtra) {
        this.jogadasExtra = jogadasExtra;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public LinkedStack<Divisao> getCaminho() {
        return caminho;
    }

    public void setCaminho(LinkedStack<Divisao> caminho) {
        this.caminho = caminho;
    }

    public int getLimiteRecuoMinSize() {
        return limiteRecuoMinSize;
    }

    public void setLimiteRecuoMinSize(int limiteRecuoMinSize) {
        this.limiteRecuoMinSize = limiteRecuoMinSize;
    }

    // =========================
    //     MÉTODOS EXTRA
    // =========================

    @Override
    public String toString() {
        return "Jogador " + nome + " na sala: " + localAtual.getNome();
    }

    public void adicionarJogadasExtras(int n) {
        this.jogadasExtra += n;
        System.out.println(nome + " ganhou " + n + " jogadas extra!");
    }

    public boolean temJogadasExtra() {
        return jogadasExtra > 0;
    }

    public void usarJogadaExtra() {
        if (jogadasExtra > 0) {
            jogadasExtra--;
        }
    }
}
