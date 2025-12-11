package engine;

import java.util.Iterator;

import Lists.ArrayUnorderedList;
import enums.AlavancaEnum;
import enums.CorredorEvento;
import enums.Dificuldade;
import enums.TipoDivisao;
import game.Alavanca;
import game.Bot;
import game.Divisao;
import game.Enigma;
import game.EventoCorredor;
import game.Player; // Precisas disto para o checkCorridorEvent
import graph.LabyrinthGraph; // <--- CONFIRMA SE ESTÁ EM 'game' OU 'graph'
import io.GameExporter;
import io.GameReport; // Para o método obterEnigma (se usares o loader)
import ui.GameView;

public class GameEngine {

    private GameView view;
    private LabyrinthGraph<Divisao> graph;
    
    // --- ESTA FOI A VARIÁVEL QUE FALTOU ---
    private String nomeDoMapaEscolhido = "Mapa do Jogo"; 

    // Dados do jogo
    private TurnManager turnManager;
    private ArrayUnorderedList<Enigma> enigmasDisponiveis;
    private ArrayUnorderedList<Player> todosJogadores;
    private Dificuldade dificuldade;
    
    // Stats
    private int turnoCount = 0;
    private int totalResolvidos = 0;
    private int totalTentados = 0;
    private int totalObstaculos = 0;
    private ArrayUnorderedList<GameReport.PlayerReport> playerReports;

    public GameEngine(LabyrinthGraph<Divisao> graph, GameView view) {
        this.graph = graph;
        this.view = view;
        this.playerReports = new ArrayUnorderedList<>();
    }

    public void setNomeDoMapa(String nome) {
        this.nomeDoMapaEscolhido = nome;
    }

    public void start() {
        // 1. SETUP (Delegado para o Initializer)
        GameInitializer init = new GameInitializer(view, graph);
        GameData dados = init.setupCompleto();
        
        if (dados == null) return; // Cancelado ou sem jogadores

        // 2. Carregar dados recebidos
        this.turnManager = new TurnManager(dados.turnQueue, view);
        this.todosJogadores = dados.todosJogadores;
        this.enigmasDisponiveis = dados.enigmasDisponiveis;
        this.dificuldade = dados.dificuldade;
        
        // Inicializar relatórios
        inicializarRelatorios();

        // 3. GAME LOOP
        boolean jogoACorrer = true;
        while (jogoACorrer && turnManager.temJogadores()) {
            
            Player atual = turnManager.proximoJogador();
            if (atual == null) break; // Segurança

            turnoCount++;
            mostrarEstadoGlobal(atual);
            view.mostrarInicioTurno(atual.getNome(), atual.getLocalAtual().getNome());

            // --- LÓGICA DE MOVIMENTO ---
            int movimentos = atual.lancarDados(view); 
            
           if (atual.getJogadasExtra() > 0) {
    int extra = atual.getJogadasExtra(); // Guardar numa variável temporária
    movimentos += extra;
    atual.setJogadasExtra(0);
    view.mostrarBonusJogadas(extra, movimentos); // <--- Passar a variável 'extra'
}

            boolean turnoTerminou = processarMovimento(atual, movimentos);

            if (verificarVitoria(atual)) {
                jogoACorrer = false;
            } else {
                view.mostrarFimTurno(atual.getNome());
                turnManager.fimDoTurno(atual);
            }
        }
        
        view.mostrarFimJogo();
    }
    

    // --- LÓGICA CORE ---

    private boolean processarMovimento(Player player, int movimentos) {
        while (movimentos > 0) {
            view.mostrarStatusMovimento(player instanceof Bot, movimentos, player.getLocalAtual().getNome());
            
            // Decisão de movimento
            Divisao destino = player.escolherDestino(graph.getVizinhos(player.getLocalAtual()), view);
            if (destino == null) return true; // Parou

            // Lógica de Enigmas
            if (destino.getTipo() == TipoDivisao.SALA_ENIGMA) {
                if (!processarEnigma(player)) {
                    return true; // Errou, turno acaba
                }
            }

            // Verificar se pode entrar (trancas e eventos)
            if (!podeEntrar(player, destino)) continue;

            // MOVER
            Divisao origem = player.getLocalAtual();
            player.moverPara(destino);
            registarRelatorioMovimento(player, destino);

            // Armadilhas
           if (verificarArmadilha(player, origem, destino)) return true;

            movimentos--;

            // Alavancas
            if (destino.getTipo() == TipoDivisao.SALA_ALAVANCA) {
                if (processarAlavanca(player, destino)) return true;
            }
            
            // Vitória
            if (destino.getTipo() == TipoDivisao.SALA_CENTRAL) return true; // Venceu
        }
        return false;
    }

    private boolean processarEnigma(Player player) {
        Enigma enigma = obterEnigma(); 
        if (enigma == null) {
            view.mostrarMensagemCarregar(); // "Sem enigmas, passa"
            return true; 
        }
        
        totalTentados++;
        boolean acertou = player.resolverEnigma(enigma, view); 
        
        if (acertou) {
            totalResolvidos++;
            applyEffect(enigma.getEfeitoSucesso(), player);
        } else {
            applyEffect(enigma.getEfeitoFalha(), player);
        }
        return acertou;
    }

    private boolean processarAlavanca(Player player, Divisao sala) {
        if (sala.getAlavanca() == null) sala.setAlavanca(new Alavanca());
        
        int escolha = player.decidirAlavanca(sala, view);
        
        AlavancaEnum resultado = sala.getAlavanca().ativar(escolha);
        view.mostrarResultadoAlavanca(resultado, sala.getIdDesbloqueio(), player.getNome());
        
        if (resultado == AlavancaEnum.ABRIR_PORTA) {
            player.desbloquearTranca(sala.getIdDesbloqueio());
            return false;
        } else if (resultado == AlavancaEnum.PENALIZAR) {
            player.recuar(2, view);
            return true;
        }
        return false;
    }

    // --- MÉTODOS DE SUPORTE (Preenchidos com lógica básica para não dar erro) ---
    
    private Enigma obterEnigma() {
        if (enigmasDisponiveis == null || enigmasDisponiveis.isEmpty()) return null;
        // Retira o primeiro enigma da lista (Lógica simples)
        return enigmasDisponiveis.removeFirst(); 
    }

    private void applyEffect(String effect, Player p) {
        if (effect == null || effect.equals("NONE")) return;
        view.mostrarEfeito(effect);

        if (effect.equals("EXTRA_TURN")) {
            // Se o TurnManager suportar jogadas imediatas, ou dar pontos extra
            // Aqui simplificado:
            p.adicionarJogadasExtras(1);
        } else if (effect.startsWith("BACK:")) {
            try {
                int steps = Integer.parseInt(effect.split(":")[1]);
                p.recuar(steps, view);
            } catch (Exception e) {}
        } else if (effect.equals("BLOCK")) {
            p.bloquear(1);
        }
        // Adiciona mais lógica aqui conforme necessário (SWAP, etc.)
    }

    private boolean verificarVitoria(Player p) {
        if (p.getLocalAtual().getTipo() == TipoDivisao.SALA_CENTRAL) {
            view.mostrarVencedor(p.getNome());
            
            // 1. Atualizar dados finais nos relatórios individuais
            Iterator<GameReport.PlayerReport> it = playerReports.iterator();
            while(it.hasNext()) {
                GameReport.PlayerReport pr = it.next();
                
                // Define o total de turnos do jogo para todos
                pr.setTurnosJogados(turnoCount);
                
                // Marca o vencedor específico
                if (pr.getNome().equals(p.getNome())) {
                    pr.setVencedor(true);
                    pr.setLocalAtual(p.getLocalAtual().getNome());
                }
            }
            
            // 2. Preencher o Relatório Global
            GameReport report = new GameReport();
            report.setVencedor(p.getNome());
            report.setDuracao(turnoCount);
            report.setMapaNome(nomeDoMapaEscolhido);
            
            // CORREÇÃO: Passar a dificuldade (com verificação de nulo)
            if (dificuldade != null) {
                report.setDificuldade(dificuldade.toString());
            } else {
                report.setDificuldade("DESCONHECIDA");
            }

            report.setListaJogadores(playerReports);
            
            // CORREÇÃO: Passar os contadores de estatística
            report.setTotalEnigmasResolvidos(totalResolvidos);
            report.setTotalEnigmasTentados(totalTentados);
            report.setTotalObstaculos(totalObstaculos);
            
            // 3. Exportar
            GameExporter exporter = new GameExporter();
            exporter.exportarJogo(report);
            
            return true;
        }
        return false;
    }
    
    private void inicializarRelatorios() {
        Iterator<Player> it = todosJogadores.iterator();
        while(it.hasNext()) {
            Player p = it.next();
            playerReports.addToRear(new GameReport.PlayerReport(p.getNome(), p instanceof Bot ? "Bot" : "Humano"));
        }
    }
    
    private void registarRelatorioMovimento(Player p, Divisao d) {
        // Encontrar o report do player e adicionar
        Iterator<GameReport.PlayerReport> it = playerReports.iterator();
        while(it.hasNext()) {
            GameReport.PlayerReport pr = it.next();
            if(pr.getNome().equals(p.getNome())) {
                pr.adicionarPercurso(d.getNome());
                // Atualizar local atual no report
                pr.setLocalAtual(d.getNome());
                break;
            }
        }
    }
    
    private void mostrarEstadoGlobal(Player ativo) {
       // Opcional: mostrar lista de jogadores
    }
    
    private boolean podeEntrar(Player p, Divisao d) {
        // Lógica simplificada de verificação de trancas
        EventoCorredor ev = graph.getCorredorEvento(p.getLocalAtual(), d);
        if (ev.getTipo() == CorredorEvento.LOCKED) {
            int key = ev.getValor();
            if (!p.podePassarTranca(key)) {
                view.mostrarPortaoTrancado(key);
                return false;
            }
        }
        return true; 
    }
    
    private boolean verificarArmadilha(Player p, Divisao o, Divisao d) { // <--- Adicionado 'Player p'
    EventoCorredor ev = graph.getCorredorEvento(o, d);
    
    if (ev.getTipo() == CorredorEvento.BLOCK_TURN || ev.getTipo() == CorredorEvento.MOVE_BACK) {
        view.mostrarArmadilhaAtivada();
        
        // Agora 'p' já é conhecido aqui!
        if (ev.getTipo() == CorredorEvento.BLOCK_TURN) {
            p.bloquear(ev.getValor());
        }
        if (ev.getTipo() == CorredorEvento.MOVE_BACK) {
            p.recuar(ev.getValor(), view);
        }
        
        // Relocalizar
        graph.relocalizarArmadilha(o, d);
        return true;
    }
    return false;
}
}