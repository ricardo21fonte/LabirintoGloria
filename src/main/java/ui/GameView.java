package ui;

import java.util.Iterator;
import java.util.Scanner;

import Lists.ArrayUnorderedList;
import enums.AlavancaEnum;
import game.Player;
/**
 * This class responsible for all user interaction for the labyrinth game
 */
public class GameView {

    /**
     * Scanner used to read user input from standard input.
     */
    private Scanner scanner;

    /**
     * Creates a new GameView
     */
    public GameView() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Reads an integer from the input.
     * @return the integer read
     */
    public int lerInteiro() {
        try {
            if (scanner.hasNextLine()) {
                return Integer.parseInt(scanner.nextLine());
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }
    /**
     * Reads a String line from the input
     * @return the string read
     */
    public String lerString() {
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine().trim();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
    /**
     * Waits for the user to press ENTER.
     */
    public void esperarEnter() {
        System.out.println("(Enter para continuar...)");
        if (scanner.hasNextLine()) scanner.nextLine();
    }

    /**
     * Displays a message indicating that game resources are loading.
     */
    public void mostrarMensagemCarregar() {

        System.out.println("\n--- A CARREGAR RECURSOS DO JOGO ---");
    }

    /**
     * Displays the error messages
     * @param mensagem display error message
     */
    public void mostrarErro(String mensagem) {
        System.out.println("\n ERRO: " + mensagem);
        System.out.println("Verifique os dados e tente novamente");}
    /**
     * Asks the user to choose the difficulty level for riddles.
     * @return the difficulty option chosen
     */
    public int pedirDificuldade() {
        System.out.println("\nEscolha a Dificuldade dos Enigmas:");
        System.out.println("1 - FÁCIL | 2 - MÉDIO | 3 - DIFÍCIL");
        System.out.print("Opção: ");
        return lerInteiro();
    }
    /**
     * Shows a generic invalid option error message for a menu choice.
     * @param min minimum valid option
     * @param max maximum valid option
     */
    public void mostrarErroOpcaoInvalida(int min, int max) {
        System.out.println("Opção inválida. Escolha entre " + min + " e " + max + ".");
    }

    /**
     * Displays the difficulty that was selected and the number of riddles.
     * @param dif difficulty text
     * @param qtd number of riddles
     */
    public void mostrarDificuldadeDefinida(String dif, int qtd) {
        System.out.println("Dificuldade definida: " + dif + " (" + qtd + " enigmas).");
    }

    /**
     * Displays an error indicating that the map has no entrance rooms.
     */
    public void mostrarErroSemEntradas() {
        System.out.println("Erro: O mapa não tem Entradas!");
    }

    /**
     * Displays a message indicating there are no players and the game ends.
     */
    public void mostrarSemJogadores() {
        System.out.println("Sem jogadores. Fim.");
    }

    /**
     * Displays the start-of-game banner.
     */
    public void mostrarInicioJogo() {
        System.out.println("\nQUE COMECE A CORRIDA!!!!️");
    }

    /**
     * Asks how many human players will participate.
     * @param max maximum number of allowed human players
     * @return the number of human players chosen
     */
    public int pedirQuantidadeHumanos(int max) {
        System.out.print("\nQuantos Humanos? (0-" + max + "): ");
        return lerInteiro();
    }

    /**
     * Asks for the name of a given human player.
     * @param i player index used in the prompt
     * @return the name entered by the user
     */
    public String pedirNomeJogador(int i) {
        System.out.print("Nome do Jogador " + i + ": ");
        return lerString();
    }

    /**
     * Shows the spawn room where a player or bot starts.
     * @param nomeSala the name of the starting room
     */
    public void mostrarSpawn(String nomeSala) {
        System.out.println("Spawn: " + nomeSala);
    }

    /**
     * Asks how many bots will participate.
     * @param max maximum number of allowed bots
     * @return the number of bots chosen
     */
    public int pedirQuantidadeBots(int max) {
        System.out.print("\nQuantos Bots? (0-" + max + "): ");
        return lerInteiro();
    }

    /**
     * Asks for the difficulty of a specific bot.
     * @param i bot index (1-based) used in the prompt
     * @return the chosen difficulty option
     */
    public int pedirDificuldadeBot(int i) {
        System.out.println("Dificuldade do Bot " + i + ": [1-Fácil, 2-Médio, 3-Difícil]");
        System.out.print("> ");
        return lerInteiro();
    }

    /**
     * Displays a message indicating that a bot was created in the given room.
     * @param nomeSala the name of the room where the bot spawns
     */
    public void mostrarBotCriado(String nomeSala) {
        System.out.println("Bot criado no " + nomeSala);
    }


    // GAME LOOP

    /**
     * Shows the header for the beginning of a player's turn.
     * @param nome  player name
     * @param local current room name
     */
    public void mostrarInicioTurno(String nome, String local) {
        System.out.println("\n================================================");
        System.out.println("VEZ DE: " + nome.toUpperCase());
        System.out.println("Local: " + local);
    }

    /**
     * Displays a message indicating a player is blocked and for how many turns.
     * @param nome   player name
     * @param turnos remaining blocked turns
     */
    public void mostrarBloqueado(String nome, int turnos) {
        System.out.println( nome + " está bloqueado!");
        System.out.println("Faltam " + turnos + " turno(s) de bloqueio.");
    }

    public void mostrarEscolhaAlvoTroca(ArrayUnorderedList<Player> jogadores, String nomeJogadorAtual) {
        System.out.println("\nEVENTO DE TROCA DE POSIÇÃO! Escolhe o jogador para trocar:");
        Iterator<Player> it = jogadores.iterator();
        int i = 1;

        while (it.hasNext()) {
            Player p = it.next();
            String prefixo = p.getNome().equals(nomeJogadorAtual) ? " (TU)" : "";
            System.out.println("   [" + i + "] " + p.getNome() + prefixo + " @ " + p.getLocalAtual().getNome());
            i++;
        }
        System.out.print("Escolhe (1-" + (i-1) + "): ");
    }
    /**
     * Displays that a bot is about to roll the dice.
     */
    public void avisarBotLancaDados() {
        System.out.println("O Bot vai lançar os dados...");
    }

    /**
     * Prompts a human player to press ENTER to roll the dice.
     */
    public void pedirHumanoLancaDados() {
        System.out.println("Pressiona ENTER para lançar o dado...");
        lerString(); 
    }

    /**
     * Displays the result of a dice roll.
     * @param isBot code true if the current player is a bot or false otherwise
     * @param valor dice value
     */
    public void mostrarResultadoDados(boolean isBot, int valor) {
        if(isBot) System.out.println("O Bot lancou um " + valor + "!");
        else      System.out.println("Lancaste um " + valor + "!");
    }

    /**
     * Displays bonus movement information for the current turn.
     * @param extra number of extra movements
     * @param total total movements allowed this turn
     */
    public void mostrarBonusJogadas(int extra, int total) {
        System.out.println("BÓNUS: tens " + extra + " movimento(s) extra acumulado(s)!");
        System.out.println("Total de movimentos neste turno: " + total);
    }
    /**
     * Shows movement status information, including remaining steps and current location.
     * @param isBot  true if the current player is a bot or false otherwise
     * @param passos remaining steps
     * @param local  current room name
     */
    public void mostrarStatusMovimento(boolean isBot, int passos, String local) {
        if (isBot) {
            System.out.println("\n[Bot] Passos: " + passos + " | Local: " + local);
        } else {
            System.out.println("\n--- Passos Restantes: " + passos + " ---");
            System.out.println("Estás em: " + local);
        }
    }

    /**
     * Displays the end-of-turn message for the given player.
     * @param nome player name
     */
    public void mostrarFimTurno(String nome) {
        System.out.println("Fim do turno de " + nome + ".");
    }

    /**
     * Displays the final winner of the game.
     * @param nome winner's name
     */
    public void mostrarVencedor(String nome) {
        System.out.println("\nVENCEDOR: " + nome + "! PARABÉNS! ");
    }

    /**
     * Displays the end-of-game message.
     */
    public void mostrarFimJogo() {
        System.out.println("Obrigado por jogar!");
    }

    /**
     * Displays one movement option in a menu, representing a neighbour room.
     * @param indice   menu index
     * @param nomeSala room name
     * @param tipoSala room type as a string
     */
    public void mostrarOpcaoMovimento(int indice, String nomeSala, String tipoSala) {
        System.out.println("[" + indice + "] Ir para: " + nomeSala + " (" + tipoSala + ")");
    }

    /**
     * Displays the "stop" movement option.
     */
    public void mostrarOpcaoParar() {

        System.out.println("[0] Parar");
    }

    /**
     * Asks the user to choose a movement option.
     * @return the chosen option
     */
    public int pedirEscolhaMovimento() {
        System.out.print("Escolha: ");
        return lerInteiro();
    }

    /**
     * Displays the destination chosen by a bot.
     * @param destino the name of the destination room
     */
    public void mostrarBotDecisao(String destino) {

        System.out.println("A avançar para: " + destino);
    }

    /**
     * Displays a message indicating the door is locked by a specific lock ID.
     * @param id the lock ID
     */
    public void mostrarPortaoTrancado(int id) {
        System.out.println("Portão Trancado (Tranca #" + id + ").");
        System.out.println("Ativa primeiro a Sala de Controlo #" + id + " com este jogador.");
    }

    /**
     * Displays a message indicating a trap was activated and the turn ends.
     */
    public void mostrarArmadilhaAtivada() {
        System.out.println("Armadilha ativada! Turno encerrado.");
    }

    /**
     * Displays a message indicating that a player has moved backwards
     * @param nomeJogador  the name of the player who moved back
     * @param casas        how many tiles the player moved back
     * @param nomeNovaSala the name of the room where the player ended up
     */
    public void mostrarRecuo(String nomeJogador, int casas, String nomeNovaSala) {
        System.out.println( nomeJogador + " recuou " + casas + " casa(s)!");
        System.out.println("Nova Posição: " + nomeNovaSala);
    }

    /**
     * Displays a warning indicating that a backward move was cancelled
     * @param nomeJogador the name of the player who attempted to move back
     */
    public void mostrarAvisoSemRecuo(String nomeJogador) {
        System.out.println(nomeJogador + ": Tentativa de recuo cancelada Limite de recuo atingido.");
    }

    // ENIGMAs

    /**
     * Displays the riddle question text.
     * @param p the question string
     */
    public void mostrarPergunta(String p) {

        System.out.println("Pergunta: " + p);
    }

    /**
     * Displays that a bot is analyzing a riddle.
     * @param nome bot name
     * @param dif  difficulty description
     */
    public void mostrarBotAnalisaEnigma(String nome, String dif) {
        System.out.println("O Bot " + nome + " (" + dif + ") está a analisar o enigma...");
    }

    /**
     * Displays the answer options for a riddle.
     * @param ops array of option strings
     */
    public void mostrarOpcoesEnigma(String[] ops) {
        for(int i=0; i<ops.length; i++) {
            System.out.println("   ("+(i+1)+") "+ops[i]);
        }
    }

    /**
     * Asks the user to provide an answer for a riddle.
     * @return the chosen option (1-based)
     */
    public int pedirRespostaEnigma() {
        System.out.print("Resposta: ");
        return lerInteiro();
    }

    /**
     * Displays the result of an attempt to solve a riddle.
     * @param acertou  true if the player answered correctly or false otherwise
     */
    public void mostrarResultadoEnigma(boolean acertou) {
        if (acertou) System.out.println("Correto! Podes passar.");
        else         System.out.println("Errado! A porta fecha-se na tua cara.");
    }

    /**
     * Displays a textual description of the effect associated with a riddle outcome.
     * @param efeito the effect code string
     */

    public void mostrarEfeito(String efeito) {
    if (efeito == null || efeito.equals("NONE")) {
        return; 
    }
    System.out.print("Efeito: ");
    if (efeito.equals("EXTRA_TURN")) {
        System.out.println("Ganhaste uma jogada extra!");
        return;
    }
    
    if (efeito.equals("BLOCK")) {
        System.out.println("Perdes o turno!");
        return;
    }

    if (efeito.startsWith("BONUS_MOVE:")) {
        try {
            int casas = Integer.parseInt(efeito.split(":")[1]);
            System.out.println("Avanças " + casas + " casa(s)!");
        } catch (Exception e) {
            System.out.println("Avanço bónus!");
        }
        return;
    }

    if (efeito.equals("BONUS_DICE")) {
        System.out.println("Lanças um dado e avanças o valor obtido!");
        return;
    }

    if (efeito.startsWith("BACK:")) {
        try {
            int casas = Integer.parseInt(efeito.split(":")[1]);
            System.out.println("Recuas " + casas + " casa(s).");
        } catch (Exception e) {
            System.out.println("Recuas algumas casas.");
        }
        return;
    }

    if (efeito.equals("BLOCK_EXTRA")) {
        System.out.println("Ficas bloqueado por mais 1 turno!");
        return;
    }

    System.out.println(efeito);

    }

    // ALAVANCAS

    /**
     * Displays that the player entered a control room with 3 levers.
     */
    public void mostrarSalaAlavanca() {
        System.out.println("\nSALA DE CONTROLO! Vês 3 Alavancas.");
        System.out.println("Uma abre O CAMINHO, outra penaliza, outra não faz nada.");
    }

    /**
     * Displays the 3 lever options in a menu.
     */
    public void mostrarOpcoesAlavanca() {
        System.out.println("[1] Alavanca 1");
        System.out.println("[2] Alavanca 2");
        System.out.println("[3] Alavanca 3");
    }

    /**
     * Asks the player to choose a lever.
     * @return the lever number chosen
     */
    public int pedirAlavanca() {
        System.out.print("Escolhe (1-3): ");
        return lerInteiro();
    }

    /**
     * Displays which lever was chosen by a bot.
     * @param n the lever number
     */
    public void mostrarBotEscolheAlavanca(int n) {

        System.out.println("O Bot puxa a alavanca " + n);
    }

    /**
     * Displays the result of pulling a lever, including lock info and player name.
     * @param res     result type of the lever (effect)
     * @param idTranca ID of the lock that may be opened, if any
     * @param nomeJog name of the player that pulled the lever
     */
    public void mostrarResultadoAlavanca(AlavancaEnum res, int idTranca, String nomeJog) {
        switch (res) {
            case ABRIR_PORTA:
                if (idTranca > 0) {
                    System.out.println("Tranca #" + idTranca + " abriu para " + nomeJog + "!");
                } else {
                    System.out.println("Esta sala não tinha tranca associada.");
                }
                break;
            case PENALIZAR:
                System.out.println("Armadilha! Recuas 2 casas.");
                break;
            case NADA:
            default:
                System.out.println("Nada acontece. Alavanca inútil.");
                break;
        }
    }

}