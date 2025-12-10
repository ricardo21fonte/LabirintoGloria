package game;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import graph.LabyrinthGraph;
import io.MapLoader;

class MapLoaderTest {

    // Cria uma pasta temporária que é apagada automaticamente no fim do teste
    @TempDir
    Path tempDir;

    @Test
    void testeCarregarMapaValido() throws IOException {
        // 1. Criar um ficheiro JSON temporário para teste
        File ficheiroTeste = tempDir.resolve("mapa_teste.json").toFile();
        
        // JSON simples mas válido conforme a estrutura do teu projeto
        String jsonConteudo = "{\n" +
                "  \"nome\": \"Mapa Teste Unitario\",\n" +
                "  \"salas\": [\n" +
                "    { \"codigo\": \"S1\", \"tipo\": \"ENTRADA\", \"nome\": \"Inicio\" },\n" +
                "    { \"codigo\": \"S2\", \"tipo\": \"SALA_CENTRAL\", \"nome\": \"Fim\" }\n" +
                "  ],\n" +
                "  \"ligacoes\": [\n" +
                "    { \"origem\": \"S1\", \"destino\": \"S2\", \"evento\": \"NONE\", \"valor\": 0 }\n" +
                "  ]\n" +
                "}";

        try (FileWriter writer = new FileWriter(ficheiroTeste)) {
            writer.write(jsonConteudo);
        }

        // 2. Usar o MapLoader para ler este ficheiro
        MapLoader loader = new MapLoader();
        LabyrinthGraph<Divisao> grafo = loader.loadMap(ficheiroTeste.getAbsolutePath());

        // 3. Asserções (Verificar se funcionou)
        assertNotNull(grafo, "O grafo não devia ser nulo");
        assertTrue(grafo.size() > 0, "O grafo devia ter vértices carregados");
    }

    @Test
    void testeFicheiroInexistente() {
        MapLoader loader = new MapLoader();
        // Tenta carregar um ficheiro que não existe
        LabyrinthGraph<Divisao> grafo = loader.loadMap("caminho/que/nao/existe.json");
        
        // CORREÇÃO: O teu MapLoader devolve null quando dá erro (ver o catch no MapLoader.java)
        // Por isso, devemos testar se é null.
        assertNull(grafo, "O loader deve retornar null se o ficheiro não existir");
    }
}