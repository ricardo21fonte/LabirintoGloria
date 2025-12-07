package game;

import Lists.ArrayUnorderedList;
import Queue.LinkedQueue;
import enums.TipoDivisao;
import enums.Dificuldade;
import java.util.Iterator;

public class Bot extends Player {

    private Dificuldade inteligencia;
    private LabyrinthGraph<Divisao> mapaConhecido;

    public Bot(String nome, Divisao inicio, Dificuldade inteligencia, LabyrinthGraph<Divisao> mapa) {
        super(nome, inicio);
        this.inteligencia = inteligencia;
        this.mapaConhecido = mapa;
    }

    // =================================================================
    // 1. LÓGICA DE MOVIMENTO (TODOS USAM BFS / CAMINHO MAIS CURTO)
    // =================================================================
    
    /**
     * Todos os bots, independentemente da dificuldade, usam o algoritmo
     * de caminho mais curto (BFS).
     */
    public Divisao escolherMovimento() {
        System.out.println("🤖 O Bot " + getNome() + " (" + inteligencia + ") está a calcular a rota ideal...");

        // Todos usam a inteligência máxima para andar
        Divisao melhorMovimento = movimentoInteligenteBFS();

        // Se o BFS não encontrar caminho (ex: preso), tenta mover aleatoriamente para desbloquear
        if (melhorMovimento == null) {
            System.out.println("   (Sem caminho óbvio, a tentar movimento aleatório...)");
            return movimentoAleatorio();
        }

        System.out.println("   📍 Caminho calculado. Próximo passo: " + melhorMovimento.getNome());
        return melhorMovimento;
    }

    /**
     * Algoritmo BFS para encontrar o PRÓXIMO passo do caminho mais curto até ao Tesouro.
     */
    private Divisao movimentoInteligenteBFS() {
        LinkedQueue<Divisao> fila = new LinkedQueue<>();
        ArrayUnorderedList<Divisao> visitados = new ArrayUnorderedList<>();
        // Guardar o caminho para reconstruir (Filho -> Pai)
        ArrayUnorderedList<Parente> arvoreGenealogica = new ArrayUnorderedList<>();

        fila.enqueue(getLocalAtual());
        visitados.addToRear(getLocalAtual());
        arvoreGenealogica.addToRear(new Parente(getLocalAtual(), null));

        Divisao alvoEncontrado = null;

        // --- CORE DO BFS ---
        while (!fila.isEmpty()) {
            Divisao atual;
            try { atual = fila.dequeue(); } catch (Exception e) { break; }

            // Se encontrámos o Tesouro, paramos a procura
            if (atual.getTipo() == TipoDivisao.SALA_CENTRAL) {
                alvoEncontrado = atual;
                break;
            }

            Iterator<Divisao> it = mapaConhecido.getVizinhos(atual).iterator();
            while (it.hasNext()) {
                Divisao vizinho = it.next();
                if (!contem(visitados, vizinho)) {
                    visitados.addToRear(vizinho);
                    fila.enqueue(vizinho);
                    // Guardamos quem é o "pai" deste vizinho para depois refazer o caminho
                    arvoreGenealogica.addToRear(new Parente(vizinho, atual));
                }
            }
        }

        // --- RECONSTRUÇÃO DO CAMINHO (Backtracking) ---
        if (alvoEncontrado != null) {
            Divisao passo = alvoEncontrado;
            Divisao anterior = null;
            
            // Vamos andar para trás (do Tesouro até ao Bot)
            // O objetivo é descobrir qual é o PRIMEIRO passo que o Bot deve dar.
            while (passo != getLocalAtual()) {
                anterior = passo;
                passo = obterPai(arvoreGenealogica, passo);
                
                // Segurança contra loops
                if (passo == null) return null; 
            }
            return anterior; // Este é o vizinho imediato para onde o Bot deve ir
        }

        return null; // Não há caminho possível
    }

    /**
     * Movimento Aleatório (Apenas usado se o BFS falhar/estiver preso)
     */
    private Divisao movimentoAleatorio() {
        ArrayUnorderedList<Divisao> vizinhos = mapaConhecido.getVizinhos(getLocalAtual());
        if (vizinhos.isEmpty()) return null;

        int index = (int) (Math.random() * vizinhos.size());
        Iterator<Divisao> it = vizinhos.iterator();
        
        for (int i = 0; i < index; i++) it.next();
        return it.next();
    }

    // =================================================================
    // 2. LÓGICA DE ENIGMAS (AQUI ESTÁ A DIFERENÇA DE DIFICULDADE)
    // =================================================================

    /**
     * O Bot tenta resolver um enigma com base na sua "inteligência".
     * @return true se acertar, false se errar.
     */
    public boolean tentarResolverEnigma(Enigma enigma) {
        System.out.println("   🤔 O Bot " + getNome() + " (" + inteligencia + ") está a analisar o enigma...");
        
        // Definir a probabilidade de acerto com base no nível
        double chanceAcerto = 0.0; 

        switch (inteligencia) {
            case FACIL: 
                chanceAcerto = 0.25; // 25% chance (Erra a maioria)
                break;
            case MEDIO: 
                chanceAcerto = 0.50; // 50% chance (Moeda ao ar)
                break;
            case DIFICIL: 
                chanceAcerto = 0.75; // 75% chance (Acerta a maioria)
                break;
        }

        // Gerar número aleatório entre 0.0 e 1.0
        double sorte = Math.random();

        // Debug para tu veres o que aconteceu (podes remover depois)
        // System.out.println("   [DEBUG] Sorte: " + String.format("%.2f", sorte) + " vs Chance: " + chanceAcerto);

        if (sorte <= chanceAcerto) {
            System.out.println("   ✨ SUCESSO! O Bot respondeu corretamente.");
            return true;
        } else {
            System.out.println("   ❌ ERRO! O Bot falhou a resposta.");
            return false;
        }
    }

    // --- Auxiliares do BFS ---
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
    
    // Getter necessário
    public Dificuldade getInteligencia() { return inteligencia; }
}