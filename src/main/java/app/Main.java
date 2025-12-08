package app;

import java.util.Scanner;
import java.util.Iterator;

// Imports do Jogo
import game.labirinto;
import game.LabyrinthGraph;
import game.Divisao;
import game.Player;
import game.Bot; 
import game.Enigma;
import enums.CorredorEvento;
import game.EventoCorredor;
import game.Alavanca;
// Imports de Input/Output
import io.EnigmaLoader;
import io.GameExporter;

// Imports de Enums e Estruturas
import enums.TipoDivisao;
import enums.Dificuldade;
import enums.AlavancaEnum;
import Lists.ArrayUnorderedList;
import Queue.LinkedQueue; 

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        
        // -----------------------------------------------------------
        // 1. MENU INICIAL
        // -----------------------------------------------------------
        Menu menuDoJogo = new Menu();
        LabyrinthGraph<Divisao> labirintoGraph = menuDoJogo.apresentarMenuPrincipal();

        if (labirintoGraph == null || labirintoGraph.size() == 0) {
            System.out.println("❌ Jogo cancelado ou mapa inválido.");
            return;
        }

        System.out.println("\n--- A CARREGAR RECURSOS ---");
        
        EnigmaLoader enigmaLoader = new EnigmaLoader();
        ArrayUnorderedList<Enigma> todosEnigmas = enigmaLoader.loadEnigmas("enigmas.json");

        labirinto motorJogo = new labirinto();
        motorJogo.setMapa(labirintoGraph);

        // -----------------------------------------------------------
        // 2. CONFIGURAR DIFICULDADE

        // -----------------------------------------------------------
        System.out.println("\nEscolha a Dificuldade dos Enigmas:");
        System.out.println("1 - FÁCIL | 2 - MÉDIO | 3 - DIFÍCIL");
        System.out.print("Opção: ");
        int opDif = lerInteiro();
        
        Dificuldade difJogo = Dificuldade.FACIL;
        if (opDif == 2) difJogo = Dificuldade.MEDIO;
        if (opDif == 3) difJogo = Dificuldade.DIFICIL;

        ArrayUnorderedList<Enigma> enigmasFiltrados = new ArrayUnorderedList<>();
        Iterator<Enigma> itEnigmas = todosEnigmas.iterator();
        while (itEnigmas.hasNext()) {
            Enigma e = itEnigmas.next();
            if (e.getDificuldade() == difJogo) enigmasFiltrados.addToRear(e);
        }
        motorJogo.setEnigmas(enigmasFiltrados);
        System.out.println("Dificuldade definida: " + difJogo + " (" + enigmasFiltrados.size() + " enigmas).");

        // -----------------------------------------------------------
        // 3. CONFIGURAÇÃO JOGADORES + BOTS
        // -----------------------------------------------------------
        
        ArrayUnorderedList<Divisao> entradas = new ArrayUnorderedList<>();
        Object[] vertices = labirintoGraph.getVertices();
        for (Object obj : vertices) {
            Divisao d = (Divisao) obj;
            if (d.getTipo() == TipoDivisao.ENTRADA) entradas.addToRear(d);
        }

        if (entradas.isEmpty()) {
            System.out.println("❌ Erro: O mapa não tem Entradas!");
            return;
        }

        int totalEntradas = entradas.size();
        Divisao[] arrayEntradas = new Divisao[totalEntradas];
        Iterator<Divisao> it = entradas.iterator();
        int idx = 0;
        while(it.hasNext()) { arrayEntradas[idx++] = it.next(); }

        LinkedQueue<Player> filaDeTurnos = new LinkedQueue<>();

        // Humanos
        System.out.print("\nQuantos Humanos (1-4)? ");
        int numHumanos = lerInteiro();
        if (numHumanos < 0) numHumanos = 0;

        for (int i = 1; i <= numHumanos; i++) {
            System.out.print("Nome do Jogador " + i + ": ");
            String nome = scanner.nextLine();
            int rnd = (int)(Math.random() * totalEntradas);
            Divisao spawn = arrayEntradas[rnd];
            
            Player p = new Player(nome, spawn);
            motorJogo.adicionarJogador(p);
            filaDeTurnos.enqueue(p);
            System.out.println("   🎲 Spawn: " + spawn.getNome());
        }

        // Bots
        System.out.print("\nQuantos Bots (0-4)? ");
        int numBots = lerInteiro();
        if (numBots < 0) numBots = 0;

        for (int i = 1; i <= numBots; i++) {
            int rnd = (int)(Math.random() * totalEntradas);
            Divisao spawn = arrayEntradas[rnd];

            System.out.println("Dificuldade do Bot " + i + ": [1-Fácil, 2-Médio, 3-Difícil]");
            System.out.print("> ");
            int opBot = lerInteiro();
            Dificuldade difBot = Dificuldade.FACIL;
            if (opBot == 2) difBot = Dificuldade.MEDIO;
            if (opBot == 3) difBot = Dificuldade.DIFICIL;

            Bot bot = new Bot("Bot_" + i, spawn, difBot, labirintoGraph);
            motorJogo.adicionarJogador(bot);
            filaDeTurnos.enqueue(bot);
            System.out.println("   🤖 Bot criado no " + spawn.getNome());
        }

        if (filaDeTurnos.isEmpty()) {
            System.out.println("Sem jogadores. Fim.");
            return;
        }

        System.out.println("\n⚔️ QUE COMECE A CORRIDA! ⚔️");
        esperarEnter();

        // -----------------------------------------------------------
        // 4. CICLO DE JOGO (GAME LOOP)
        // -----------------------------------------------------------
        boolean jogoAcorrer = true;

        while (jogoAcorrer && !filaDeTurnos.isEmpty()) {
            
            Player atual;
            try { atual = filaDeTurnos.dequeue(); } catch (Exception e) { break; }

            System.out.println("\n================================================");
            System.out.println("👤 VEZ DE: " + atual.getNome().toUpperCase());
            System.out.println("📍 Local: " + atual.getLocalAtual().getNome());

            // 1. Verificar Bloqueio
            if (atual.isBloqueado()) {
                System.out.println("🚫 " + atual.getNome() + " está bloqueado!");
                System.out.println("   Faltam " + atual.getTurnosBloqueado() + " turno(s) de bloqueio.");

                atual.consumirUmTurnoBloqueado(); // paga 1 turno

                filaDeTurnos.enqueue(atual);
                if (!(atual instanceof Bot)) esperarEnter();
                else try { Thread.sleep(1000); } catch(Exception e){}
                continue;
            }


            // 2. Lançar Dados (AGORA IGUAL PARA TODOS)
            int movimentos = 0;

            if (atual instanceof Bot) {
                System.out.println("🤖 O Bot vai lançar os dados...");
                try { Thread.sleep(1000); } catch(Exception e){}
                movimentos = lancarDados();
                System.out.println("🎲 O Bot rolou um " + movimentos + "!");
            } else {
                System.out.println("Pressiona ENTER para lançar o dado...");
                scanner.nextLine();
                movimentos = lancarDados();
                System.out.println("🎲 ROLASTE UM " + movimentos + "!");
            }

            // APLICAR JOGADAS EXTRA PARA QUALQUER JOGADOR
            if (atual.getJogadasExtra() > 0) {
                System.out.println("✨ BÓNUS: tens " + atual.getJogadasExtra() + " movimento(s) extra acumulado(s)!");
                movimentos += atual.getJogadasExtra();
                atual.setJogadasExtra(0); // limpa o bónus depois de usado
                System.out.println("➡️ Total de movimentos neste turno: " + movimentos);
            }


            // 3. Realizar Movimentos
            boolean turnoQueimado = false;

            while (movimentos > 0 && !turnoQueimado) {
                // Pausa visual para ver o Bot a andar casa a casa
                if (atual instanceof Bot) {
                    try { Thread.sleep(1500); } catch(Exception e){}
                }

                if (!(atual instanceof Bot)) {
                    System.out.println("\n--- Passos Restantes: " + movimentos + " ---");
                    System.out.println("Estás em: " + atual.getLocalAtual().getNome());
                } else {
                    System.out.println("\n🤖 [Bot] Passos: " + movimentos + " | Local: " + atual.getLocalAtual().getNome());
                }

                Divisao destino = null;

                if (atual instanceof Bot) {
                    Bot oBot = (Bot) atual;
                    destino = oBot.escolherMovimento(); // Escolhe baseado no BFS
                } else {
                    ArrayUnorderedList<Divisao> vizinhos = labirintoGraph.getVizinhos(atual.getLocalAtual());
                    Divisao[] opcoes = new Divisao[10];
                    int count = 0;
                    
                    Iterator<Divisao> itVizinhos = vizinhos.iterator(); 
                    while (itVizinhos.hasNext()) {
                        Divisao v = itVizinhos.next();
                        opcoes[count] = v;
                        System.out.println("   [" + (count + 1) + "] Ir para: " + v.getNome() + " (" + v.getTipo() + ")");
                        count++;
                    }
                    System.out.println("   [0] Parar");
                    System.out.print("Escolha: ");
                    int escolha = lerInteiro();

                    if (escolha == 0) break;
                    if (escolha > 0 && escolha <= count) destino = opcoes[escolha - 1];
                }

                if (destino == null) {
                    if (!(atual instanceof Bot)) System.out.println("⚠️ Opção inválida.");
                    continue;
                }

                boolean podeEntrar = true;

                if (destino.getTipo() == TipoDivisao.SALA_ENIGMA) {
                    System.out.println("\n🕵️ ENIGMA NA PORTA!");
                    Enigma desafio = motorJogo.obterEnigma(difJogo);

                    if (desafio != null) {
                        System.out.println("P: " + desafio.getPergunta());
                        boolean acertou = false;

                        if (atual instanceof Bot) {
                            Bot oBot = (Bot) atual;
                            acertou = oBot.tentarResolverEnigma(desafio);
                        } else {
                            String[] ops = desafio.getOpcoes();
                            for(int k=0; k<ops.length; k++) System.out.println("   ("+(k+1)+") "+ops[k]);
                            System.out.print("Resp: ");
                            int r = lerInteiro();
                            acertou = desafio.verificarResposta(r);
                        }

                        if (acertou) {
                            System.out.println("✅ Correto! Podes passar.");

                            // --- ALTERAÇÃO AQUI: SOMAR MOVIMENTOS IMEDIATAMENTE ---
                            int bonus = aplicarEfeitoEnigma(desafio.getEfeitoSucesso(), atual, filaDeTurnos);
                            if (bonus > 0) {
                                movimentos += bonus;
                                System.out.println("   (Tens agora " + movimentos + " movimentos!)");
                            }

                        } else {
                            System.out.println("❌ Errado! A porta fecha-se na tua cara.");
                            podeEntrar = false;

                            // Errar enigma termina SEMPRE o turno atual
                            turnoQueimado = true;

                            // E ainda pode aplicar penalidades para o futuro (ex: Block next turn)
                            aplicarEfeitoEnigma(desafio.getEfeitoFalha(), atual, filaDeTurnos);
                        }
                    }
                }

                // --- MOVIMENTO ---
                if (podeEntrar) {
                    EventoCorredor evento = labirintoGraph.getCorredorEvento(atual.getLocalAtual(), destino);

                    // 1) Verificar se é um portão trancado
                    if (evento.getTipo() == CorredorEvento.LOCKED) {
                        int idTranca = evento.getValor();

                        // ESTE É O PONTO CRÍTICO: ver se ESTE jogador já abriu essa tranca
                        if (!atual.podePassarTranca(idTranca)) {
                            System.out.println("🔒 Portão trancado (Tranca #" + idTranca + ").");
                            System.out.println("   Ativa primeiro a Sala de Controlo #" + idTranca + " com este jogador.");
                            // não entra, não gasta movimento
                            if (atual instanceof Bot) {
                                turnoQueimado = true; // se quiseres castigar o bot, opcional
                            }
                            continue;  // volta ao while(movimentos>0) sem andar
                        } else {
                            System.out.println("🔓 Tranca #" + idTranca + " já foi desbloqueada para "
                                    + atual.getNome() + ". Passas.");
                        }
                    }

                    // 2) Se chegou aqui, ou não era LOCKED, ou está desbloqueada para este jogador
                    motorJogo.realizarJogada(atual, destino);

                    if (evento.getTipo() == CorredorEvento.BLOCK_TURN ||
                            evento.getTipo() == CorredorEvento.MOVE_BACK) {
                        System.out.println("⛔ Armadilha ativada! Turno encerrado.");
                        turnoQueimado = true;
                    }

                    movimentos--;

                    // SE A SALA DE DESTINO FOR DE ALAVANCA, CHAMAMOS O PUZZLE
                    if (!turnoQueimado && destino.getTipo() == TipoDivisao.SALA_ALAVANCA) {
                        boolean acabouTurno = resolverSalaAlavanca(atual, destino, labirintoGraph);
                        if (acabouTurno) {
                            turnoQueimado = true;
                        }
                    }

                    if (atual.getLocalAtual().getTipo() == TipoDivisao.SALA_CENTRAL) {
                        System.out.println("\n🎉🎉 VENCEDOR: " + atual.getNome() + "! 🎉🎉");
                        GameExporter exporter = new GameExporter();
                        jogoAcorrer = false;
                        turnoQueimado = true;
                    }
                }

            } 

            if (jogoAcorrer) {
                System.out.println("Fim do turno de " + atual.getNome() + ".");
                filaDeTurnos.enqueue(atual);
                // Só pede Enter se for humano a jogar
                if (!(atual instanceof Bot)) esperarEnter();
                else try { Thread.sleep(1000); } catch(Exception e){}
            }
        }
        
        System.out.println("Obrigado por jogar!");
        scanner.close();
    }

    // --- Auxiliares ---
    private static int lancarDados() { return (int) (Math.random() * 6) + 1; }
    
    private static int lerInteiro() {
        try { return Integer.parseInt(scanner.nextLine()); } catch (Exception e) { return -1; }
    }

    private static void esperarEnter() {
        System.out.println("(Enter para continuar...)");
        if (scanner.hasNextLine()) scanner.nextLine();
    }

    // --- LÓGICA DE EFEITOS ESPECIAIS (AJUDAS/PENALIDADES) ---
    private static int aplicarEfeitoEnigma(String efeito, Player atual, LinkedQueue<Player> fila) {
        if (efeito == null || efeito.equals("NONE")) return 0;

        System.out.println("🔮 EFEITO ATIVADO: " + efeito);

        if (efeito.equals("EXTRA_TURN")) {
            System.out.println("✨ BÓNUS IMEDIATO! Ganhaste +1 movimento agora!");
            // Retorna 1 para adicionar aos movimentos atuais
            return 1;
        }
        else if (efeito.equals("BLOCK")) {
            System.out.println("⛔ CASTIGO! Ficas bloqueado no próximo turno.");
            atual.bloquear(1);
        }
        else if (efeito.startsWith("BACK:")) {
            try {
                int casas = Integer.parseInt(efeito.split(":")[1]);
                System.out.println("🔙 RECUAR! Voltas " + casas + " casas.");
                atual.recuar(casas);
                // Opcional: Se recuar faz perder o resto do turno, podes retornar um valor negativo gigante
            } catch (Exception e) {}
        }
        else if (efeito.equals("SWAP")) {
            System.out.println("🔄 TROCA! Trocaste de lugar com o próximo jogador.");
            try {
                Player outro = fila.dequeue();
                if (outro != atual) {
                    Divisao posAtual = atual.getLocalAtual();
                    Divisao posOutro = outro.getLocalAtual();

                    atual.setLocalAtual(posOutro);
                    outro.setLocalAtual(posAtual);
                    System.out.println("   -> Trocaste com " + outro.getNome());
                }
                fila.enqueue(outro);
            } catch(Exception e) {}
        }

        return 0; // Se não for EXTRA_TURN, não ganha movimentos
    }
    // Trata a interação com uma sala de alavanca.
    // Devolve true se o turno deve acabar.

    private static boolean resolverSalaAlavanca(Player jogador, Divisao sala, LabyrinthGraph<Divisao> mapa) {
        // Garante que a sala já tem uma alavanca associada
        if (sala.getAlavanca() == null) {
            sala.setAlavanca(new Alavanca());
        }

        Alavanca alavanca = sala.getAlavanca();

        System.out.println("\nEsta sala tem 3 alavancas.");
        System.out.println("Uma abre um mecanismo, outra penaliza, outra não faz nada.");
        System.out.println("[1] Alavanca 1");
        System.out.println("[2] Alavanca 2");
        System.out.println("[3] Alavanca 3");
        System.out.print("Escolhe 1, 2 ou 3: ");
        int escolha;

        if (jogador instanceof Bot) {
            // Bot: escolher aleatoriamente
            escolha = 1 + (int)(Math.random() * alavanca.getNumAlavancas());
            System.out.println("O bot escolhe a alavanca " + escolha);
        } else {
            // Humano: perguntar
            do {
                escolha = lerInteiro();
            } while (escolha < 1 || escolha > 3);
        }

        AlavancaEnum resultado = alavanca.ativar(escolha);

        switch (resultado) {
            case ABRIR_PORTA:
                // ID da tranca que esta sala controla
                int idTranca = sala.getIdDesbloqueio();
                if (idTranca > 0) {
                    jogador.desbloquearTranca(idTranca);
                    System.out.println("A alavanca certa! A tranca #" + idTranca
                            + " foi desbloqueada para o jogador " + jogador.getNome() + ".");
                } else {
                    System.out.println("A alavanca certa! Ouves mecanismos ao longe...");
                }
                return false; // turno continua

            case PENALIZAR:
                System.out.println("Era uma armadilha! Recuas 2 casas.");
                jogador.recuar(2);
                return true; // penalização termina o turno

            case NADA:
            default:
                System.out.println("Nada acontece. Alavanca inútil.");
                return false; // turno continua
        }

    }


}