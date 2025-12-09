package game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import enums.Dificuldade;

class EnigmaTest {

    @Test
    void testeVerificarResposta() {
        String[] opcoes = {"Opcao A", "Opcao B", "Opcao C", "Opcao D"};
        // Correta é o índice 1 (Opcao B), que corresponde à escolha "2" do utilizador
        Enigma e = new Enigma("Pergunta?", opcoes, 1, Dificuldade.FACIL);

        // O teu método espera o número que o utilizador escolhe (1, 2, 3...)
        // e subtrai 1 internamente.
        
        assertTrue(e.verificarResposta(2), "A resposta 2 devia estar correta");
        assertFalse(e.verificarResposta(1), "A resposta 1 devia estar errada");
        assertFalse(e.verificarResposta(5), "A resposta 5 devia estar errada");
    }
}