package game;

import java.util.Iterator;

import Lists.ArrayUnorderedList;
import Queue.LinkedQueue;
import enums.CorredorEvento;
import enums.Dificuldade;
import enums.TipoDivisao;

public class Bot extends Player {

    private Dificuldade inteligencia;
    private LabyrinthGraph<Divisao> mapaConhecido;

    public Bot(String nome, Divisao inicio, Dificuldade inteligencia, LabyrinthGraph<Divisao> mapa) {
        super(nome, inicio);
        this.inteligencia = inteligencia;
        this.mapaConhecido = mapa;
    }

    // =================================================================
    // 1. CÉREBRO DO BOT (DECISÃO DE MOVIMENTO)
    // =================================================================
    
    // REMOVI O @OVERRIDE AQUI PORQUE O PLAYER NÃO TEM ESTE MÉTODO
    public Divisao escolherMovimento() {
        System.out.println("🤖 O Bot " + getNome() + " (" + inteligencia + ") está a pensar...");

        // PRIORIDADE 1: Tentar ir para o Tesouro
        Divisao passoParaTesouro = bfsParaAlvo(TipoDivisao.SALA_CENTRAL, null);

        if (passoParaTesouro != null) {
            System.out.println("   📍 Caminho livre para o Tesouro! A avançar para: " + passoParaTesouro.getNome());
            return passoParaTesouro;
        }

        // PRIORIDADE 2: Procurar Alavanca Útil
        System.out.println("   🔒 Caminho para o tesouro bloqueado. A procurar alavancas...");
        Divisao passoParaAlavanca = buscarAlavancaMaisProxima();

        if (passoParaAlavanca != null) {
            System.out.println("   🔑 Vou buscar uma chave! A ir para: " + passoParaAlavanca.getNome());
            return passoParaAlavanca;
        }

        // PRIORIDADE 3: Movimento Aleatório (Desespero)
        System.out.println("   (Bot confuso ou preso, a tentar movimento aleatório...)");
        return movimentoAleatorio();
    }

    // =================================================================
    // 2. ALGORITMOS DE BUSCA (PATHFINDING)
    // =================================================================

    private Divisao bfsParaAlvo(TipoDivisao tipoAlvo, Divisao divisaoAlvo) {
        LinkedQueue<Divisao> fila = new LinkedQueue<>();
        ArrayUnorderedList<Divisao> visitados = new ArrayUnorderedList<>();
        ArrayUnorderedList<Parente> arvoreGenealogica = new ArrayUnorderedList<>();

        Divisao inicio = getLocalAtual();
        fila.enqueue(inicio);
        visitados.addToRear(inicio);
        arvoreGenealogica.addToRear(new Parente(inicio, null));

        Divisao alvoEncontrado = null;

        while (!fila.isEmpty()) {
            Divisao atual;
            try { atual = fila.dequeue(); } catch (Exception e) { break; }

            boolean chegou = false;
            if (tipoAlvo != null && atual.getTipo() == tipoAlvo) chegou = true;
            if (divisaoAlvo != null && atual.equals(divisaoAlvo)) chegou = true;

            if (chegou) {
                alvoEncontrado = atual;
                break;
            }

            Iterator<Divisao> it = mapaConhecido.getVizinhos(atual).iterator();
            while (it.hasNext()) {
                Divisao vizinho = it.next();

                if (!contem(visitados, vizinho)) {
                    if (podePassar(atual, vizinho)) {
                        visitados.addToRear(vizinho);
                        fila.enqueue(vizinho);
                        arvoreGenealogica.addToRear(new Parente(vizinho, atual));
                    }
                }
            }
        }

        if (alvoEncontrado != null) {
            return reconstruirPrimeiroPasso(arvoreGenealogica, alvoEncontrado);
        }
        return null;
    }

    private Divisao buscarAlavancaMaisProxima() {
        LinkedQueue<Divisao> fila = new LinkedQueue<>();
        ArrayUnorderedList<Divisao> visitados = new ArrayUnorderedList<>();
        ArrayUnorderedList<Parente> arvoreGenealogica = new ArrayUnorderedList<>();

        Divisao inicio = getLocalAtual();
        fila.enqueue(inicio);
        visitados.addToRear(inicio);
        arvoreGenealogica.addToRear(new Parente(inicio, null));

        Divisao alvoEncontrado = null;

        while (!fila.isEmpty()) {
            Divisao atual;
            try { atual = fila.dequeue(); } catch (Exception e) { break; }

            if (atual.getTipo() == TipoDivisao.SALA_ALAVANCA) {
                int idTranca = atual.getIdDesbloqueio();
                if (!this.podePassarTranca(idTranca)) {
                    alvoEncontrado = atual;
                    break; 
                }
            }

            Iterator<Divisao> it = mapaConhecido.getVizinhos(atual).iterator();
            while (it.hasNext()) {
                Divisao vizinho = it.next();
                if (!contem(visitados, vizinho)) {
                    if (podePassar(atual, vizinho)) {
                        visitados.addToRear(vizinho);
                        fila.enqueue(vizinho);
                        arvoreGenealogica.addToRear(new Parente(vizinho, atual));
                    }
                }
            }
        }

        if (alvoEncontrado != null) {
            return reconstruirPrimeiroPasso(arvoreGenealogica, alvoEncontrado);
        }
        return null;
    }

    private boolean podePassar(Divisao origem, Divisao destino) {
        EventoCorredor evento = mapaConhecido.getCorredorEvento(origem, destino);
        
        if (evento.getTipo() == CorredorEvento.LOCKED) {
            int idChave = evento.getValor();
            return this.podePassarTranca(idChave);
        }
        return true;
    }

    private Divisao reconstruirPrimeiroPasso(ArrayUnorderedList<Parente> arvore, Divisao destino) {
        Divisao passo = destino;
        Divisao anterior = null;
        
        while (passo != getLocalAtual()) {
            anterior = passo;
            passo = obterPai(arvore, passo);
            if (passo == null) return null; 
        }
        return anterior;
    }

    private Divisao movimentoAleatorio() {
        ArrayUnorderedList<Divisao> vizinhos = mapaConhecido.getVizinhos(getLocalAtual());
        if (vizinhos.isEmpty()) return null;
        int index = (int) (Math.random() * vizinhos.size());
        Iterator<Divisao> it = vizinhos.iterator();
        for (int i = 0; i < index; i++) it.next();
        return it.next();
    }

    // =================================================================
    // 3. LÓGICA DE ENIGMAS
    // =================================================================

    public boolean tentarResolverEnigma(Enigma enigma) {
        System.out.println("   🤔 O Bot " + getNome() + " (" + inteligencia + ") está a analisar o enigma...");
        double chanceAcerto = 0.0; 
        switch (inteligencia) {
            case FACIL: chanceAcerto = 0.25; break;
            case MEDIO: chanceAcerto = 0.50; break;
            case DIFICIL: chanceAcerto = 0.75; break;
        }
        return Math.random() <= chanceAcerto;
    }

    // --- Auxiliares ---
    private boolean contem(ArrayUnorderedList<Divisao> lista, Divisao alvo) {
        Iterator<Divisao> it = lista.iterator();
        while (it.hasNext()) if (it.next().equals(alvo)) return true;
        return false;
    }

    private Divisao obterPai(ArrayUnorderedList<Parente> lista, Divisao filho) {
        Iterator<Parente> it = lista.iterator();
        while (it.hasNext()) {
            Parente p = it.next();
            if (p.filho.equals(filho)) return p.pai;
        }
        return null;
    }

    private class Parente {
        Divisao filho, pai;
        public Parente(Divisao f, Divisao p) { this.filho = f; this.pai = p; }
    }
    
    public Dificuldade getInteligencia() { return inteligencia; }
}