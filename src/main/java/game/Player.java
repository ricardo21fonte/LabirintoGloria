package game; // Confirma que está no package 'game' ou 'core.model' conforme a tua estrutura

import java.util.Iterator;

import Exceptions.EmptyCollectionException;
import Lists.ArrayUnorderedList;
import Lists.UnorderedLinkedList;
import Stacks.LinkedStack;
import ui.GameView;

public class Player {
    private String nome;
    private Divisao localAtual;
    private UnorderedLinkedList<String> historico;
    private int jogadasExtra;
    private int turnosBloqueado;
    private LinkedStack<Divisao> caminho;
    private int limiteRecuoMinSize;
    private ArrayUnorderedList<Integer> trancasDesbloqueadas;

    public Player(String nome, Divisao inicio) {
        this.nome = nome;
        this.localAtual = inicio;
        this.historico = new UnorderedLinkedList<>();
        this.jogadasExtra = 0;
        this.turnosBloqueado = 0;
        this.caminho = new LinkedStack<>();
        caminho.push(inicio);
        this.limiteRecuoMinSize = 1;
        this.trancasDesbloqueadas = new ArrayUnorderedList<>();
        historico.addToRear("Inicio: " + inicio.getNome());
    }

    // =========================================================
    // 🎮 MÉTODOS QUE O BOT VAI DAR OVERRIDE
    // (Estes métodos têm de existir aqui para o Bot não dar erro)
    // =========================================================

    /**
     * Comportamento Base (Humano): Pede para carregar Enter e lança.
     */
    public int lancarDados(GameView view) {
        view.pedirHumanoLancaDados();
        int val = (int)(Math.random() * 6) + 1;
        view.mostrarResultadoDados(false, val);
        return val;
    }

    /**
     * Comportamento Base (Humano): Mostra menu e pede escolha.
     */
    public Divisao escolherDestino(ArrayUnorderedList<Divisao> vizinhos, GameView view) {
        if (vizinhos == null || vizinhos.isEmpty()) return null;

        // Converter vizinhos para array para mostrar no menu (1, 2, 3...)
        Divisao[] opcoes = new Divisao[vizinhos.size()];
        int i = 0;
        Iterator<Divisao> it = vizinhos.iterator();
        while (it.hasNext()) {
            Divisao v = it.next();
            opcoes[i] = v;
            view.mostrarOpcaoMovimento(i + 1, v.getNome(), v.getTipo().toString());
            i++;
        }
        view.mostrarOpcaoParar();

        // Loop para garantir escolha válida
        while (true) {
            int escolha = view.pedirEscolhaMovimento();
            if (escolha == 0) return null; // Parar
            if (escolha > 0 && escolha <= vizinhos.size()) {
                return opcoes[escolha - 1];
            }
            view.mostrarErroOpcaoInvalida(0, vizinhos.size());
        }
    }

    /**
     * Comportamento Base (Humano): Mostra pergunta e pede input numérico.
     */
    public boolean resolverEnigma(Enigma enigma, GameView view) {
        view.mostrarPergunta(enigma.getPergunta());
        String[] opcoes = enigma.getOpcoes();
        view.mostrarOpcoesEnigma(opcoes);

        int resposta;
        do {
            resposta = view.pedirRespostaEnigma();
            if (resposta < 1 || resposta > opcoes.length) {
                view.mostrarErroOpcaoInvalida(1, opcoes.length);
            }
        } while (resposta < 1 || resposta > opcoes.length);

        boolean acertou = enigma.verificarResposta(resposta);
        view.mostrarResultadoEnigma(acertou);
        return acertou;
    }

    /**
     * Comportamento Base (Humano): Escolhe alavanca (1, 2 ou 3) via menu.
     */
    public int decidirAlavanca(Divisao sala, GameView view) {
        view.mostrarSalaAlavanca();
        view.mostrarOpcoesAlavanca();
        
        int escolha;
        do {
            escolha = view.pedirAlavanca();
            if (escolha < 1 || escolha > 3) {
                view.mostrarErroOpcaoInvalida(1, 3);
            }
        } while (escolha < 1 || escolha > 3);
        
        return escolha;
    }

    // =========================================================
    // ⚙️ GETTERS, SETTERS E LÓGICA INTERNA
    // =========================================================

    public void moverPara(Divisao novaSala) {
        this.localAtual = novaSala;
        historico.addToRear("Moveu para: " + novaSala.getNome());
        caminho.push(novaSala);
    }

    public void recuar(int casas) {
        if (caminho == null || caminho.size() <= limiteRecuoMinSize) {
            // Pode adicionar mensagem na View aqui se quiseres
            return;
        }
        
        int passos = casas;
        while (passos > 0 && caminho.size() > limiteRecuoMinSize) {
            try { caminho.pop(); } catch (EmptyCollectionException e) { break; }
            passos--;
        }

        try {
            Divisao novaPosicao = caminho.peek();
            this.localAtual = novaPosicao;
            historico.addToRear("Recuou para: " + novaPosicao.getNome());
        } catch (EmptyCollectionException e) {}
    }

    public void bloquear(int turnos) {
        this.turnosBloqueado += turnos;
    }

    public void setLocalAtual(Divisao novaSala) {
        this.localAtual = novaSala;
        if (caminho == null) caminho = new LinkedStack<>();
        caminho.push(novaSala);
    }

    public void marcarLimiteRecuo() {
        this.limiteRecuoMinSize = caminho.size();
    }

    public void desbloquearTranca(int id) {
        if (!podePassarTranca(id)) {
            trancasDesbloqueadas.addToRear(id);
        }
    }

    public boolean podePassarTranca(int id) {
        Iterator<Integer> it = trancasDesbloqueadas.iterator();
        while (it.hasNext()) {
            if (it.next() == id) return true;
        }
        return false;
    }

    // --- Getters Simples ---
    public String getNome() { return nome; }
    public Divisao getLocalAtual() { return localAtual; }
    public UnorderedLinkedList<String> getHistorico() { return historico; }
    public int getJogadasExtra() { return jogadasExtra; }
    public void setJogadasExtra(int n) { this.jogadasExtra = n; }
    public void adicionarJogadasExtras(int n) { this.jogadasExtra += n; }
    public boolean isBloqueado() { return turnosBloqueado > 0; }
    public int getTurnosBloqueado() { return turnosBloqueado; }
    public void consumirUmTurnoBloqueado() { if (turnosBloqueado > 0) turnosBloqueado--; }
    
    @Override
    public String toString() { return "Jogador " + nome + " @ " + localAtual.getNome(); }
}