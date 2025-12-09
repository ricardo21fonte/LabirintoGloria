package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import enums.AlavancaEnum;

class AlavancaTest {

    @Test
    void testeAlavancaLimites() {
        Alavanca a = new Alavanca();
        
        // Testar input inválido
        AlavancaEnum resultado = a.ativar(5);
        assertEquals(AlavancaEnum.NADA, resultado, "Escolha inválida deve retornar NADA");
        
        resultado = a.ativar(0);
        assertEquals(AlavancaEnum.NADA, resultado, "Escolha 0 deve retornar NADA");
        
        // Testar input válido (1 a 3)
        AlavancaEnum resValido = a.ativar(1);
        assertNotNull(resValido, "A ativação válida não pode retornar null");
    }
}