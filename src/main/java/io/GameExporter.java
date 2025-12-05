package io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

// Imports da biblioteca JSON
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

// Imports do teu jogo
import game.Player;
import Lists.ArrayUnorderedList;

public class GameExporter {

    /**
     * Gera um ficheiro JSON com o relatório final do jogador.
     * @param jogador O jogador que venceu (ou terminou)
     */
    @SuppressWarnings("unchecked")
    public void exportarRelatorio(Player jogador) {
        System.out.println("📄 A gerar relatório de jogo...");

        // 1. Criar o Objeto JSON principal
        JSONObject relatorio = new JSONObject();
        relatorio.put("jogador", jogador.getNome());
        relatorio.put("resultado", "VENCEDOR"); // Se chamamos isto, é porque ganhou
        
        // 2. Criar o Array do Histórico de Movimentos
        JSONArray listaMovimentos = new JSONArray();
        
        // Vamos buscar a lista de passos guardada no Player
        ArrayUnorderedList<String> historico = jogador.getHistorico();
        
        if (historico != null) {
            Iterator<String> it = historico.iterator();
            while (it.hasNext()) {
                // Adiciona cada passo (String) ao array do JSON
                listaMovimentos.add(it.next());
            }
        }
        
        // Juntar a lista ao objeto principal
        relatorio.put("percurso", listaMovimentos);

        // 3. Escrever no Ficheiro
        // O nome do ficheiro será algo como: relatorio_ze.json
        String nomeFicheiro = "relatorio_" + jogador.getNome() + ".json";
        
        try (FileWriter file = new FileWriter(nomeFicheiro)) {
            file.write(relatorio.toJSONString());
            System.out.println("✅ Relatório guardado com sucesso: " + nomeFicheiro);
        } catch (IOException e) {
            System.out.println("❌ Erro ao gravar relatório: " + e.getMessage());
            e.printStackTrace();
        }
    }
}