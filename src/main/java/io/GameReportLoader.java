package io;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Lists.ArrayUnorderedList; 

public class GameReportLoader {
    
    private static final String RELATORIOS_DIR = "resources/saved_relatorios";
    
    public ArrayUnorderedList<String> listarRelatorios() {
        ArrayUnorderedList<String> relatorios = new ArrayUnorderedList<>();
        File dir = new File(RELATORIOS_DIR);

        if (!dir.exists()) return relatorios;

        File[] files = dir.listFiles((d, name) -> 
            (name.startsWith("game_") || name.startsWith("relatorio_")) && name.endsWith(".json")
        );

        if (files != null) {
            for (File file : files) {
                relatorios.addToRear(file.getName());
            }
        }
        return relatorios;
    }

    public GameReport carregarRelatorio(String filename) {
        JSONParser parser = new JSONParser();
        GameReport report = new GameReport();

        try (FileReader reader = new FileReader(RELATORIOS_DIR + File.separator + filename)) {
            
            // 1. Ler o Objeto Principal
            JSONObject json = (JSONObject) parser.parse(reader);

            report.setVencedor((String) json.get("vencedor"));
            
            // Ler Data
            String dataStr = (String) json.get("dataHora");
            if (dataStr != null && !dataStr.isEmpty()) {
                try { report.setDataHora(LocalDateTime.parse(dataStr)); } catch (Exception e) {}
            }
            
            // Ler Inteiros (Convertendo de Long do json-simple)
            report.setDuracao(getInt(json, "duracao"));
            report.setMapaNome((String) json.get("mapaNome"));
            report.setDificuldade((String) json.get("dificuldade"));
            
            report.setTotalEnigmasResolvidos(getInt(json, "totalEnigmasResolvidos"));
            report.setTotalEnigmasTentados(getInt(json, "totalEnigmasTentados"));
            report.setTotalObstaculos(getInt(json, "totalObstaculos"));

            // 2. Ler Jogadores
            JSONArray jogadoresArray = (JSONArray) json.get("jogadores");
            if (jogadoresArray != null) {
                for (Object pObj : jogadoresArray) {
                    JSONObject pJson = (JSONObject) pObj;
                    
                    String nome = (String) pJson.get("nome");
                    String tipo = (String) pJson.get("tipo");
                    
                    GameReport.PlayerReport player = new GameReport.PlayerReport(nome, tipo);
                    player.setLocalAtual((String) pJson.get("localAtual"));
                    player.setTurnosJogados(getInt(pJson, "turnosJogados"));
                    player.setVencedor((Boolean) pJson.get("vencedor"));

                    // Preencher listas internas
                    preencherLista((JSONArray) pJson.get("percurso"), player.getPercurso());
                    preencherLista((JSONArray) pJson.get("obstaculos"), player.getObstaculos());
                    preencherLista((JSONArray) pJson.get("efeitosAplicados"), player.getEfeitosAplicados());
                    
                    // Preencher Enigmas
                    JSONArray enigmasArray = (JSONArray) pJson.get("enigmas");
                    if (enigmasArray != null) {
                        for (Object eObj : enigmasArray) {
                            JSONObject eJson = (JSONObject) eObj;
                            GameReport.EnigmaEvent evt = new GameReport.EnigmaEvent(
                                (String) eJson.get("pergunta"),
                                (String) eJson.get("resposta"),
                                (Boolean) eJson.get("resolvido"),
                                (String) eJson.get("efeito"),
                                (String) eJson.get("sala")
                            );
                            player.adicionarEnigma(evt);
                        }
                    }
                    report.adicionarJogador(player);
                }
            }
            return report;

        } catch (Exception e) {
            System.out.println("Erro ao ler relatório: " + e.getMessage());
            return null;
        }
    }

    // --- Helpers Simples ---

    private void preencherLista(JSONArray source, ArrayUnorderedList<String> target) {
        if (source != null) {
            for (Object o : source) target.addToRear((String) o);
        }
    }

    private int getInt(JSONObject json, String key) {
        Object val = json.get(key);
        if (val == null) return 0;
        return ((Long) val).intValue(); // JSON-simple lê números como Long
    }
}