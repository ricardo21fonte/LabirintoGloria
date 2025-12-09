package app;

import java.util.Scanner;

import enums.AlavancaEnum;

public class GameView {

    private Scanner scanner;

    public GameView() {
        this.scanner = new Scanner(System.in);
    }

    public void fechar() {
        scanner.close();
    }

    // =======================================================
    // MÉTODOS DE INPUT GENÉRICOS
    // =======================================================
    
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

    public void esperarEnter() {
        System.out.println("(Enter para continuar...)");
        if (scanner.hasNextLine()) scanner.nextLine();
    }

    // =======================================================
    // SETUP E MENUS
    // =======================================================

    public void mostrarMensagemCarregar() {
        System.out.println("\n--- A CARREGAR RECURSOS DO JOGO ---");
    }

    public int pedirDificuldade() {
        System.out.println("\nEscolha a Dificuldade dos Enigmas:");
        System.out.println("1 - FÁCIL | 2 - MÉDIO | 3 - DIFÍCIL");
        System.out.print("Opção: ");
        return lerInteiro();
    }

    public void mostrarErroOpcaoInvalida(int min, int max) {
        System.out.println("⚠️ Opção inválida. Escolha entre " + min + " e " + max + ".");
    }

    public void mostrarDificuldadeDefinida(String dif, int qtd) {
        System.out.println("Dificuldade definida: " + dif + " (" + qtd + " enigmas).");
    }

    public void mostrarErroSemEntradas() {
        System.out.println("❌ Erro: O mapa não tem Entradas!");
    }

    public void mostrarAvisoJogoCheio() {
        System.out.println("\n(O jogo está cheio com 8 humanos. Não é possível adicionar bots.)");
    }

    public void mostrarSemJogadores() {
        System.out.println("Sem jogadores. Fim.");
    }

    public void mostrarInicioJogo() {
        System.out.println("\n⚔️ QUE COMECE A CORRIDA! ⚔️");
    }

    // --- SETUP JOGADORES ---

    public int pedirQuantidadeHumanos(int max) {
        System.out.print("\nQuantos Humanos? (0-" + max + "): ");
        return lerInteiro();
    }

    public String pedirNomeJogador(int i) {
        System.out.print("Nome do Jogador " + i + ": ");
        return lerString();
    }

    public void mostrarSpawn(String nomeSala) {
        System.out.println("   🎲 Spawn: " + nomeSala);
    }

    public int pedirQuantidadeBots(int max) {
        System.out.print("\nQuantos Bots? (0-" + max + "): ");
        return lerInteiro();
    }

    public int pedirDificuldadeBot(int i) {
        System.out.println("Dificuldade do Bot " + i + ": [1-Fácil, 2-Médio, 3-Difícil]");
        System.out.print("> ");
        return lerInteiro();
    }

    public void mostrarBotCriado(String nomeSala) {
        System.out.println("   🤖 Bot criado no " + nomeSala);
    }

    // =======================================================
    // GAME LOOP (TURNOS E DADOS)
    // =======================================================

    public void mostrarInicioTurno(String nome, String local) {
        System.out.println("\n================================================");
        System.out.println("👤 VEZ DE: " + nome.toUpperCase());
        System.out.println("📍 Local: " + local);
    }

    public void mostrarBloqueado(String nome, int turnos) {
        System.out.println("🚫 " + nome + " está bloqueado!");
        System.out.println("   Faltam " + turnos + " turno(s) de bloqueio.");
    }

    public void avisarBotLancaDados() {
        System.out.println("🤖 O Bot vai lançar os dados...");
    }

    public void pedirHumanoLancaDados() {
        System.out.println("🎲 Pressiona ENTER para lançar o dado...");
        lerString(); 
    }

    public void mostrarResultadoDados(boolean isBot, int valor) {
        if(isBot) System.out.println("🎲 O Bot rolou um " + valor + "!");
        else      System.out.println("🎲 ROLASTE UM " + valor + "!");
    }

    public void mostrarBonusJogadas(int extra, int total) {
        System.out.println("✨ BÓNUS: tens " + extra + " movimento(s) extra acumulado(s)!");
        System.out.println("➡️ Total de movimentos neste turno: " + total);
    }

    public void mostrarStatusMovimento(boolean isBot, int passos, String local) {
        if (isBot) {
            System.out.println("\n🤖 [Bot] Passos: " + passos + " | Local: " + local);
        } else {
            System.out.println("\n--- Passos Restantes: " + passos + " ---");
            System.out.println("Estás em: " + local);
        }
    }

    public void mostrarFimTurno(String nome) {
        System.out.println("Fim do turno de " + nome + ".");
    }

    public void mostrarVencedor(String nome) {
        System.out.println("\n🎉🎉 VENCEDOR: " + nome + "! PARABÉNS! 🎉🎉");
    }

    public void mostrarFimJogo() {
        System.out.println("Obrigado por jogar!");
    }

    // =======================================================
    // MOVIMENTO E INTERAÇÕES
    // =======================================================

    public void mostrarOpcaoMovimento(int indice, String nomeSala, String tipoSala) {
        System.out.println("   [" + indice + "] Ir para: " + nomeSala + " (" + tipoSala + ")");
    }

    public void mostrarOpcaoParar() {
        System.out.println("   [0] Parar");
    }

    public int pedirEscolhaMovimento() {
        System.out.print("Escolha: ");
        return lerInteiro();
    }

    public void mostrarBotDecisao(String destino) {
        System.out.println("   📍 A avançar para: " + destino);
    }

    // Mensagens de Eventos de Corredor
    public void mostrarPortaoTrancado(int id) {
        System.out.println("🔒 Portão Trancado (Tranca #" + id + ").");
        System.out.println("   Ativa primeiro a Sala de Controlo #" + id + " com este jogador.");
    }

    public void mostrarTrancaJaAberta(int id, String nome) {
        System.out.println("🔓 Tranca #" + id + " já foi desbloqueada para " + nome + ". Passas.");
    }

    public void mostrarArmadilhaAtivada() {
        System.out.println("⛔ Armadilha ativada! Turno encerrado.");
    }

    // =======================================================
    // ENIGMAS
    // =======================================================

    public void mostrarEnigmaNaPorta() {
        System.out.println("\n🕵️ ENIGMA NA PORTA!");
    }

    public void mostrarPergunta(String p) {
        System.out.println("P: " + p);
    }

    public void mostrarBotAnalisaEnigma(String nome, String dif) {
        System.out.println("   🤔 O Bot " + nome + " (" + dif + ") está a analisar o enigma...");
    }

    public void mostrarOpcoesEnigma(String[] ops) {
        for(int k=0; k<ops.length; k++) {
            System.out.println("   ("+(k+1)+") "+ops[k]);
        }
    }

    public int pedirRespostaEnigma() {
        System.out.print("Resp: ");
        return lerInteiro();
    }

    public void mostrarResultadoEnigma(boolean acertou) {
        if (acertou) System.out.println("✅ Correto! Podes passar.");
        else         System.out.println("❌ Errado! A porta fecha-se na tua cara.");
    }

    public void mostrarEfeito(String efeito) {
        if (efeito == null || efeito.equals("NONE")) {
            return; // nada a mostrar
        }

        System.out.print("🔮 Efeito do enigma: ");

        // SUCESSO FÁCIL / MÉDIO → BONUS_MOVE:x  (x casas a avançar)
        if (efeito.startsWith("BONUS_MOVE:")) {
            String[] partes = efeito.split(":"); // ["BONUS_MOVE", "1"] ou ["BONUS_MOVE", "2"]
            try {
                int casas = Integer.parseInt(partes[1]);
                if (casas == 1) {
                    System.out.println("avanças 1 casa!");
                } else {
                    System.out.println("avanças " + casas + " casas!");
                }
            } catch (NumberFormatException e) {
                System.out.println("bónus de movimento (valor inválido no efeito: " + efeito + ")");
            }
            return;
        }

        // SUCESSO DIFÍCIL → BONUS_DICE
        if (efeito.equals("BONUS_DICE")) {
            System.out.println("lanças um dado e avanças o valor obtido!");
            return;
        }

        // FALHA DIFÍCIL → BLOCK_EXTRA
        if (efeito.equals("BLOCK_EXTRA")) {
            System.out.println("ficas bloqueado por mais 1 turno!");
            return;
        }

        // FALHAS FÁCIL / MÉDIO → BACK:x  (x casas para trás)
        if (efeito.startsWith("BACK:")) {
            String[] partes = efeito.split(":"); // ["BACK", "1"] ou ["BACK", "2"]
            try {
                int casas = Integer.parseInt(partes[1]);
                if (casas == 1) {
                    System.out.println("recuas 1 casa.");
                } else {
                    System.out.println("recuas " + casas + " casas.");
                }
            } catch (NumberFormatException e) {
                System.out.println("recuas algumas casas (valor inválido no efeito: " + efeito + ")");
            }

        }


    }

    public void mostrarGanhouMovimentos(int n) {
        System.out.println("   (Tens agora " + n + " movimentos!)");
    }

    // =======================================================
    // ALAVANCAS
    // =======================================================

    public void mostrarSalaAlavanca() {
        System.out.println("\n⚙️ SALA DE CONTROLO! Vês 3 Alavancas.");
        System.out.println("   Uma abre O CAMINHO, outra penaliza, outra não faz nada.");
    }

    public void mostrarOpcoesAlavanca() {
        System.out.println("[1] Alavanca 1");
        System.out.println("[2] Alavanca 2");
        System.out.println("[3] Alavanca 3");
    }

    public int pedirAlavanca() {
        System.out.print("Escolhe (1-3): ");
        return lerInteiro();
    }

    public void mostrarBotEscolheAlavanca(int n) {
        System.out.println("🤖 O Bot puxa a alavanca " + n);
    }

    public void mostrarResultadoAlavanca(AlavancaEnum res, int idTranca, String nomeJog) {
        switch (res) {
            case ABRIR_PORTA:
                if (idTranca > 0) {
                    System.out.println("✅ CLACK! A tranca #" + idTranca + " abriu para " + nomeJog + "!");
                } else {
                    System.out.println("✅ CLACK! Ouves mecanismos, mas esta sala não tinha tranca associada.");
                }
                break;
            case PENALIZAR:
                System.out.println("💥 Armadilha! Recuas 2 casas.");
                break;
            case NADA:
            default:
                System.out.println("💤 Nada acontece. Alavanca inútil.");
                break;
        }
    }
    // =======================================================
    // ESTADO GLOBAL (VISUALIZAÇÃO EXTRA)
    // =======================================================

    public void mostrarCabecalhoEstado() {
        System.out.println("\n--- 🌍 ESTADO ATUAL DO JOGO ---");
    }

    public void mostrarLocalizacaoJogador(String nome, String sala, boolean isAtivo) {
        String prefixo = isAtivo ? " 👉 " : "    "; // Seta aponta para quem vai jogar
        System.out.println(prefixo + "👤 " + nome + " \t-> 📍 " + sala);
    }
}