package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import Lists.ArrayUnorderedList; 

public class GameReportLoader {
    
    // --- MUDANÇA: Apontar para a nova pasta de relatórios ---
    private static final String RELATORIOS_DIR = "resources/saved_relatorios";
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ArrayUnorderedList<String> listarRelatorios() {
        ArrayUnorderedList<String> relatorios = new ArrayUnorderedList<>();
        
        // Usar a nova constante
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
        try {
            // Usar a nova constante
            String filepath = RELATORIOS_DIR + File.separator + filename;
            
            String content = new String(Files.readAllBytes(Paths.get(filepath)));
            return parseJSON(content);
        } catch (IOException e) {
            System.out.println("Erro ao ler ficheiro: " + e.getMessage());
            return null;
        }
    }

    // ... (O RESTO DO FICHEIRO MANTÉM-SE EXATAMENTE IGUAL) ...
    // Copia os métodos parseJSON, parseJogadoresArray, extractJsonInt, etc. do código que colaste.
    // Eles já estão corretos na versão que me mostraste.
    
    private GameReport parseJSON(String json) {
        // ... igual ...
        GameReport report = new GameReport();
        try {
            report.setVencedor(extractJsonString(json, "vencedor"));
            String dataHoraStr = extractJsonString(json, "dataHora");
            if (!dataHoraStr.isEmpty()) {
                try { report.setDataHora(LocalDateTime.parse(dataHoraStr)); } catch (Exception e) {}
            }
            report.setDuracao(extractJsonInt(json, "duracao"));
            report.setMapaNome(extractJsonString(json, "mapaNome"));
            report.setDificuldade(extractJsonString(json, "dificuldade"));
            report.setTotalEnigmasResolvidos(extractJsonInt(json, "totalEnigmasResolvidos"));
            report.setTotalEnigmasTentados(extractJsonInt(json, "totalEnigmasTentados"));
            report.setTotalObstaculos(extractJsonInt(json, "totalObstaculos"));

            ArrayUnorderedList<GameReport.PlayerReport> jogadores = parseJogadoresArray(json);
            report.setListaJogadores(jogadores);
            return report;
        } catch (Exception e) { return null; }
    }

    private ArrayUnorderedList<GameReport.PlayerReport> parseJogadoresArray(String json) {
        ArrayUnorderedList<GameReport.PlayerReport> jogadores = new ArrayUnorderedList<>();
        int arrayStart = json.indexOf("\"jogadores\":");
        if (arrayStart == -1) return jogadores;
        int arrayBegin = json.indexOf("[", arrayStart);
        if (arrayBegin == -1) return jogadores;
        int arrayEnd = findMatchingBracket(json, arrayBegin);
        if (arrayEnd == -1) return jogadores;

        String arrayContent = json.substring(arrayBegin + 1, arrayEnd);
        int objectCount = 0;
        int objectStart = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                if (objectCount == 0) objectStart = i;
                objectCount++;
            } else if (c == '}') {
                objectCount--;
                if (objectCount == 0 && objectStart != -1) {
                    String objectJson = arrayContent.substring(objectStart, i + 1);
                    GameReport.PlayerReport player = parsePlayerObject(objectJson);
                    if (player != null) jogadores.addToRear(player);
                    objectStart = -1;
                }
            }
        }
        return jogadores;
    }
    
    private GameReport.PlayerReport parsePlayerObject(String objectJson) {
        try {
            String nome = extractJsonString(objectJson, "nome");
            String tipo = extractJsonString(objectJson, "tipo");
            GameReport.PlayerReport player = new GameReport.PlayerReport(nome, tipo);
            player.setLocalAtual(extractJsonString(objectJson, "localAtual"));
            player.setTurnosJogados(extractJsonInt(objectJson, "turnosJogados"));
            player.setVencedor(extractJsonBoolean(objectJson, "vencedor"));

            parseStringArray(objectJson, "percurso", player.getPercurso());
            parseStringArray(objectJson, "obstaculos", player.getObstaculos());
            parseStringArray(objectJson, "efeitosAplicados", player.getEfeitosAplicados());
            parseEnigmasArray(objectJson, player.getEnigmas());
            return player;
        } catch (Exception e) { return null; }
    }

    private void parseStringArray(String json, String key, ArrayUnorderedList<String> listDestino) {
        int keyPos = json.indexOf("\"" + key + "\":");
        if (keyPos == -1) return;
        int arrayBegin = json.indexOf("[", keyPos);
        if (arrayBegin == -1) return;
        int arrayEnd = findMatchingBracket(json, arrayBegin);
        if (arrayEnd == -1) return;

        String arrayContent = json.substring(arrayBegin + 1, arrayEnd);
        String[] parts = arrayContent.split("\"");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.equals(",") && !part.isEmpty()) listDestino.addToRear(unescapeJson(part));
        }
    }

    private void parseEnigmasArray(String json, ArrayUnorderedList<GameReport.EnigmaEvent> listDestino) {
        int keyPos = json.indexOf("\"enigmas\":");
        if (keyPos == -1) return;
        int arrayBegin = json.indexOf("[", keyPos);
        if (arrayBegin == -1) return;
        int arrayEnd = findMatchingBracket(json, arrayBegin);
        if (arrayEnd == -1) return;

        String arrayContent = json.substring(arrayBegin + 1, arrayEnd);
        int objectCount = 0;
        int objectStart = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                if (objectCount == 0) objectStart = i;
                objectCount++;
            } else if (c == '}') {
                objectCount--;
                if (objectCount == 0 && objectStart != -1) {
                    String objJson = arrayContent.substring(objectStart, i + 1);
                    GameReport.EnigmaEvent evt = new GameReport.EnigmaEvent(
                        extractJsonString(objJson, "pergunta"),
                        extractJsonString(objJson, "resposta"),
                        extractJsonBoolean(objJson, "resolvido"),
                        extractJsonString(objJson, "efeito"),
                        extractJsonString(objJson, "sala")
                    );
                    listDestino.addToRear(evt);
                    objectStart = -1;
                }
            }
        }
    }

    private int findMatchingBracket(String text, int startPos) {
        int count = 0;
        for (int i = startPos; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '[') count++;
            else if (c == ']') {
                count--;
                if (count == 0) return i;
            }
        }
        return -1;
    }

    private String extractJsonString(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIdx = json.indexOf(searchKey);
        if (startIdx == -1) return "";
        int valueStart = json.indexOf("\"", startIdx + searchKey.length());
        if (valueStart == -1) return "";
        int valueEnd = json.indexOf("\"", valueStart + 1);
        while (valueEnd < json.length()) {
            if (json.charAt(valueEnd) == '"' && json.charAt(valueEnd - 1) != '\\') break;
            valueEnd++;
        }
        if (valueEnd >= json.length()) return "";
        return unescapeJson(json.substring(valueStart + 1, valueEnd));
    }

    private int extractJsonInt(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIdx = json.indexOf(searchKey);
        if (startIdx == -1) return 0;
        
        int i = startIdx + searchKey.length();
        while (i < json.length()) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '-') break;
            i++;
        }
        
        int startNum = i;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (!Character.isDigit(c) && c != '-') break;
            i++;
        }
        
        try { 
            String numStr = json.substring(startNum, i);
            return Integer.parseInt(numStr); 
        } catch (Exception e) { return 0; }
    }

    private boolean extractJsonBoolean(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIdx = json.indexOf(searchKey);
        if (startIdx == -1) return false;
        int valStart = startIdx + searchKey.length();
        String sub = json.substring(valStart, Math.min(valStart + 10, json.length()));
        return sub.contains("true");
    }

    private String unescapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\\\", "\\").replace("\\\"", "\"").replace("\\n", "\n");
    }
}