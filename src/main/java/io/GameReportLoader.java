package io;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Lists.ArrayUnorderedList; 

public class GameReportLoader {
    /**
     * Directory where game reports are stored as JSON files.
     */
    private static final String RELATORIOS_DIR = "resources/saved_relatorios";
    /**
     * Lists all report filenames stored in the reports directory.
     */
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
    /**
     * Loads a single GameReport from a JSON file with the given name.
     * @param filename name of the JSON report file
     * @return aa GameReport
     */
    public GameReport carregarRelatorio(String filename) {
        JSONParser parser = new JSONParser();
        GameReport report = new GameReport();

        try (FileReader reader = new FileReader(RELATORIOS_DIR + File.separator + filename)) {

            JSONObject json = (JSONObject) parser.parse(reader);

            report.setVencedor((String) json.get("vencedor"));
            
            // Le os Dados
            String dataStr = (String) json.get("dataHora");
            if (dataStr != null && !dataStr.isEmpty()) {
                try { report.setDataHora(LocalDateTime.parse(dataStr)); } catch (Exception e) {}
            }
            

            report.setDuracao(getInt(json, "duracao"));
            report.setMapaNome((String) json.get("mapaNome"));
            report.setDificuldade((String) json.get("dificuldade"));
            
            report.setTotalEnigmasResolvidos(getInt(json, "totalEnigmasResolvidos"));
            report.setTotalEnigmasTentados(getInt(json, "totalEnigmasTentados"));
            report.setTotalObstaculos(getInt(json, "totalObstaculos"));

            // Le os Jogadores
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

                    preencherLista((JSONArray) pJson.get("percurso"), player.getPercurso());
                    preencherLista((JSONArray) pJson.get("obstaculos"), player.getObstaculos());
                    preencherLista((JSONArray) pJson.get("efeitosAplicados"), player.getEfeitosAplicados());

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

    /**
     * Method that copies all elements from a JSON array into a ArrayUnorderedList
     *
     * @param source the JSON array to read from
     * @param target the list where all elements will be appended
     */
    private void preencherLista(JSONArray source, ArrayUnorderedList<String> target) {
        if (source != null) {
            for (Object o : source) target.addToRear((String) o);
        }
    }

    /**
     * Method to read an integer value from a JSONObject.
     * @param json the JSON object from which to extract the numeric field
     * @param key  the key of the numeric attribute
     * @return the integer value associated with the key
     */
    private int getInt(JSONObject json, String key) {
        Object val = json.get(key);
        if (val == null) return 0;
        return ((Long) val).intValue();
    }
}