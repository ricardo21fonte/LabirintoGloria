package app;

import Lists.ArrayUnorderedList;
import game.Divisao;
import game.Player;
import game.EventoAleatorio;
import enums.TipoDivisao;
import enums.TipoEvento;

public class TesteEventoAleatorio {

    public static void main(String[] args) {
        testeSemEvento();
        testeJogadaExtra();
        testeRecuar();
        testeBloquearTurnos();
        testeTrocarTodos();
        testeTrocarPosicao();
    }

    // ---------- 1) SEM_EVENTO ----------
    private static void testeSemEvento() {
        System.out.println("===== TESTE: SEM_EVENTO =====");

        Divisao s1 = new Divisao("Sala 1", TipoDivisao.SALA_NORMAL);
        Divisao s2 = new Divisao("Sala 2", TipoDivisao.SALA_NORMAL);
        Divisao s3 = new Divisao("Sala 3", TipoDivisao.SALA_NORMAL);

        Player p1 = new Player("Alice",  s1);
        Player p2 = new Player("Bob",    s2);
        Player p3 = new Player("Carlos", s3);

        ArrayUnorderedList<Player> jogadores = new ArrayUnorderedList<>();
        jogadores.addToRear(p1);
        jogadores.addToRear(p2);
        jogadores.addToRear(p3);

        EventoAleatorio ev = new EventoAleatorio(TipoEvento.SEM_EVENTO, 2);
        System.out.println("Evento: " + ev.getTipo() + " | " + ev.getDescricao());

        ev.aplicar(p1, jogadores);

        for (Player p : jogadores) {
            System.out.println(" - " + p);
        }
        System.out.println();
    }

    // ---------- 2) JOGADA_EXTRA ----------
    private static void testeJogadaExtra() {
        System.out.println("===== TESTE: JOGADA_EXTRA =====");

        Divisao s1 = new Divisao("Sala 1", TipoDivisao.SALA_NORMAL);
        Divisao s2 = new Divisao("Sala 2", TipoDivisao.SALA_NORMAL);
        Divisao s3 = new Divisao("Sala 3", TipoDivisao.SALA_NORMAL);

        Player p1 = new Player("Alice",  s1);
        Player p2 = new Player("Bob",    s2);
        Player p3 = new Player("Carlos", s3);

        ArrayUnorderedList<Player> jogadores = new ArrayUnorderedList<>();
        jogadores.addToRear(p1);
        jogadores.addToRear(p2);
        jogadores.addToRear(p3);

        EventoAleatorio ev = new EventoAleatorio(TipoEvento.JOGADA_EXTRA, 2);
        System.out.println("Evento: " + ev.getTipo() + " | " + ev.getDescricao());

        ev.aplicar(p1, jogadores);

        for (Player p : jogadores) {
            System.out.println(" - " + p);
        }
        System.out.println();
    }

    // ---------- 3) RECUAR ----------
    private static void testeRecuar() {
        System.out.println("===== TESTE: RECUAR =====");

        Divisao s1 = new Divisao("Sala 1", TipoDivisao.SALA_NORMAL);
        Divisao s2 = new Divisao("Sala 2", TipoDivisao.SALA_NORMAL);
        Divisao s3 = new Divisao("Sala 3", TipoDivisao.SALA_NORMAL);

        Player p1 = new Player("Alice",  s1);
        Player p2 = new Player("Bob",    s2);
        Player p3 = new Player("Carlos", s3);

        ArrayUnorderedList<Player> jogadores = new ArrayUnorderedList<>();
        jogadores.addToRear(p1);
        jogadores.addToRear(p2);
        jogadores.addToRear(p3);

        // Simular que a Alice já andou (para o recuar ter efeito)
        p1.moverPara(s2);
        p1.moverPara(s3); // caminho: Sala 1 -> Sala 2 -> Sala 3

        EventoAleatorio ev = new EventoAleatorio(TipoEvento.RECUAR, 1);
        System.out.println("Evento: " + ev.getTipo() + " | " + ev.getDescricao());

        ev.aplicar(p1, jogadores);

        for (Player p : jogadores) {
            System.out.println(" - " + p);
        }
        System.out.println();
    }

    // ---------- 4) BLOQUEAR_TURNOS ----------
    private static void testeBloquearTurnos() {
        System.out.println("===== TESTE: BLOQUEAR_TURNOS =====");

        Divisao s1 = new Divisao("Sala 1", TipoDivisao.SALA_NORMAL);
        Divisao s2 = new Divisao("Sala 2", TipoDivisao.SALA_NORMAL);
        Divisao s3 = new Divisao("Sala 3", TipoDivisao.SALA_NORMAL);

        Player p1 = new Player("Alice",  s1);
        Player p2 = new Player("Bob",    s2);
        Player p3 = new Player("Carlos", s3);

        ArrayUnorderedList<Player> jogadores = new ArrayUnorderedList<>();
        jogadores.addToRear(p1);
        jogadores.addToRear(p2);
        jogadores.addToRear(p3);

        EventoAleatorio ev = new EventoAleatorio(TipoEvento.BLOQUEAR_TURNOS, 1);
        System.out.println("Evento: " + ev.getTipo() + " | " + ev.getDescricao());

        ev.aplicar(p1, jogadores);

        for (Player p : jogadores) {
            System.out.println(" - " + p);
        }
        System.out.println();
    }

    // ---------- 5) TROCAR_TODOS ----------
    private static void testeTrocarTodos() {
        System.out.println("===== TESTE: TROCAR_TODOS =====");

        Divisao s1 = new Divisao("Sala 1", TipoDivisao.SALA_NORMAL);
        Divisao s2 = new Divisao("Sala 2", TipoDivisao.SALA_NORMAL);
        Divisao s3 = new Divisao("Sala 3", TipoDivisao.SALA_NORMAL);

        Player p1 = new Player("Alice",  s1);
        Player p2 = new Player("Bob",    s2);
        Player p3 = new Player("Carlos", s3);

        ArrayUnorderedList<Player> jogadores = new ArrayUnorderedList<>();
        jogadores.addToRear(p1);
        jogadores.addToRear(p2);
        jogadores.addToRear(p3);

        EventoAleatorio ev = new EventoAleatorio(TipoEvento.TROCAR_TODOS, 1);
        System.out.println("Evento: " + ev.getTipo() + " | " + ev.getDescricao());

        ev.aplicar(p1, jogadores);

        for (Player p : jogadores) {
            System.out.println(" - " + p);
        }
        System.out.println();
    }

    // ---------- 6) TROCAR_POSICAO ----------
    private static void testeTrocarPosicao() {
        System.out.println("===== TESTE: TROCAR_POSICAO =====");

        Divisao s1 = new Divisao("Sala 1", TipoDivisao.SALA_NORMAL);
        Divisao s2 = new Divisao("Sala 2", TipoDivisao.SALA_NORMAL);
        Divisao s3 = new Divisao("Sala 3", TipoDivisao.SALA_NORMAL);

        Player p1 = new Player("Alice",  s1);
        Player p2 = new Player("Bob",    s2);
        Player p3 = new Player("Carlos", s3);

        ArrayUnorderedList<Player> jogadores = new ArrayUnorderedList<>();
        jogadores.addToRear(p1);
        jogadores.addToRear(p2);
        jogadores.addToRear(p3);

        EventoAleatorio ev = new EventoAleatorio(TipoEvento.TROCAR_POSICAO, 1);
        System.out.println("Evento: " + ev.getTipo() + " | " + ev.getDescricao());

        ev.aplicar(p1, jogadores);

        for (Player p : jogadores) {
            System.out.println(" - " + p);
        }
        System.out.println();
    }
}
