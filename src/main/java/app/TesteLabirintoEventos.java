package app;

import Lists.ArrayUnorderedList;
import game.Divisao;
import game.LabyrinthGraph;
import game.Player;
import game.labirinto;
import io.MapGenerator;
import enums.TipoDivisao;

public class TesteLabirintoEventos {

    public static void main(String[] args) {
        // 1) Gerar um mapa simples
        MapGenerator gerador = new MapGenerator();
        LabyrinthGraph<Divisao> grafo = gerador.gerarMapaAleatorio(1); // 1 = pequeno

        // 2) Criar motor do jogo
        labirinto motor = new labirinto();
        motor.setMapa(grafo);

        // 3) Encontrar uma entrada para usar como spawn
        ArrayUnorderedList<Divisao> entradas = new ArrayUnorderedList<>();
        Object[] vertices = grafo.getVertices();
        for (Object obj : vertices) {
            Divisao d = (Divisao) obj;
            if (d.getTipo() == TipoDivisao.ENTRADA) {
                entradas.addToRear(d);
            }
        }

        if (entradas.isEmpty()) {
            System.out.println("Não há entradas no mapa. Teste cancelado.");
            return;
        }

        // Usar a primeira entrada da lista
        Divisao spawn = null;
        for (Divisao d : entradas) {
            spawn = d;
            break;
        }

        // 4) Criar alguns jogadores
        Player p1 = new Player("Alice", spawn);
        Player p2 = new Player("Bob",   spawn);
        Player p3 = new Player("Carlos", spawn);

        // Lista local só para mostrar estados no fim de cada jogada
        ArrayUnorderedList<Player> jogadores = new ArrayUnorderedList<>();
        jogadores.addToRear(p1);
        jogadores.addToRear(p2);
        jogadores.addToRear(p3);

        // Registar jogadores no motor
        motor.adicionarJogador(p1);
        motor.adicionarJogador(p2);
        motor.adicionarJogador(p3);

        // 5) Fazer alguns movimentos automáticos com o p1
        System.out.println("===== INÍCIO DO TESTE NO LABIRINTO =====");
        System.out.println("Spawn na divisão: " + spawn.getNome());

        Player atual = p1;

        for (int i = 0; i < 10; i++) {
            System.out.println("\n================ MOVIMENTO " + (i + 1) + " ================");

            // Obter vizinhos da posição atual do p1
            ArrayUnorderedList<Divisao> vizinhos = grafo.getVizinhos(atual.getLocalAtual());
            if (vizinhos.isEmpty()) {
                System.out.println("Sem vizinhos a partir de " + atual.getLocalAtual().getNome() + ". Teste termina.");
                break;
            }

            // Escolher simplesmente o primeiro vizinho
            Divisao destino = null;
            for (Divisao v : vizinhos) {
                destino = v;
                break;
            }

            System.out.println("Vou tentar mover " + atual.getNome() + " para: " + destino.getNome());

            // Chamar o motor do jogo (aqui entram CorredorEvent + EventoAleatorio)
            motor.realizarJogada(atual, destino);

            // Mostrar a posição de todos os jogadores após a jogada
            System.out.println("\nEstado dos jogadores após a jogada:");
            for (Player p : jogadores) {
                System.out.println(" - " + p);
            }
        }

        System.out.println("\n===== FIM DO TESTE NO LABIRINTO =====");
    }
}
