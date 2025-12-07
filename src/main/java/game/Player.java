package game;

import Lists.UnorderedLinkedList; // Confirma se o pacote é Lists ou structures

public class Player {
    private String nome;
    private Divisao localAtual;
    private UnorderedLinkedList<String> historico; // Histórico para o relatório
    
    // Variáveis para os eventos
    private int jogadasExtra; 
    private boolean bloqueado; 

    public Player(String nome, Divisao inicio) {
        this.nome = nome;
        this.localAtual = inicio;
        this.historico = new UnorderedLinkedList<>();
        this.jogadasExtra = 0;
        this.bloqueado = false;
        
        // Regista o ponto de partida
        historico.addToRear("Inicio: " + inicio.getNome());
    }

    /**
     * Move o jogador para uma nova sala e regista no histórico.
     */
    public void moverPara(Divisao novaSala) {
        this.localAtual = novaSala;
        historico.addToRear("Moveu para: " + novaSala.getNome());
    }

    // --- MÉTODOS NOVOS (Necessários para os Eventos) ---

    /**
     * Adiciona jogadas extra ao jogador.
     * @param quantidade Número de jogadas a adicionar.
     */
    public void adicionarJogadasExtras(int quantidade) {
        this.jogadasExtra += quantidade;
        System.out.println("✨ " + nome + " ganhou " + quantidade + " jogadas extra!");
    }

    /**
     * Verifica se o jogador tem jogadas extra disponíveis e consome uma.
     * @return true se usou uma jogada extra, false se não tinha nenhuma.
     */
    public boolean usarJogadaExtra() {
        if (this.jogadasExtra > 0) {
            this.jogadasExtra--;
            return true;
        }
        return false;
    }

    /**
     * Simula o recuo do jogador.
     * Nota: Como não temos histórico duplo, apenas avisamos.
     * @param casas Número de casas a recuar.
     */
    public void recuar(int casas) {
        System.out.println("🔙 " + nome + " foi empurrado para trás " + casas + " casas!");
        // Se quiseres implementar lógica real, precisas de uma Stack de histórico.
        // Para já, o jogador fica onde está mas perde o progresso do turno.
    }

    /**
     * Bloqueia o jogador.
     * @param turnos (Pode ser usado no futuro para contagem, agora é boolean)
     */
    public void bloquear(int turnos) {
        this.bloqueado = true;
        System.out.println("⛔ " + nome + " está bloqueado por " + turnos + " turnos.");
    }

    /**
     * Define a posição diretamente (Teleporte).
     * Necessário para o evento de Trocar Posições.
     */
    public void setLocalAtual(Divisao novaPosicao) {
        this.localAtual = novaPosicao;
        historico.addToRear("Teleportado para: " + novaPosicao.getNome());
        System.out.println("🔄 " + nome + " foi teleportado para " + novaPosicao.getNome());
    }

    // --- GETTERS E SETTERS ---

    public String getNome() { return nome; }
    
    public Divisao getLocalAtual() { return localAtual; }
    
    public UnorderedLinkedList<String> getHistorico() { return historico; }
    
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }
    
    public boolean isBloqueado() { return bloqueado; }

    public int getJogadasExtra() { return jogadasExtra; }

    @Override
    public String toString() {
        return "Jogador " + nome + " na sala: " + localAtual.getNome();
    }
}