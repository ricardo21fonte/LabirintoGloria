package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Loads and retrieves saved game reports from the saved_games/ directory.
 */
public class GameReportLoader {
    private static final String SAVED_GAMES_DIR = "saved_games";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Lists all saved game JSON files in chronological order (newest first).
     */
    public List<String> listarRelatorios() {
        List<String> relatorios = new ArrayList<>();
        File dir = new File(SAVED_GAMES_DIR);

        if (!dir.exists()) {
            System.out.println("Pasta 'saved_games' não existe.");
            return relatorios;
        }

        File[] files = dir.listFiles((d, name) -> name.startsWith("game_") && name.endsWith(".json"));

        if (files != null && files.length > 0) {
            // Sort by filename in reverse order (newest first)
            Arrays.sort(files, (a, b) -> b.getName().compareTo(a.getName()));

            for (File file : files) {
                relatorios.add(file.getName());
            }
        }

        return relatorios;
    }

    /**
     * Loads and parses a saved game JSON file.
     * Returns a GameReport object.
     */
    public GameReport carregarRelatorio(String filename) {
        try {
            String filepath = SAVED_GAMES_DIR + File.separator + filename;
            String content = new String(Files.readAllBytes(Paths.get(filepath)));

            return parseJSON(content);
        } catch (IOException e) {
            System.out.println("Erro ao carregar relatório: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses JSON string into GameReport object.
     * Simple JSON parser using string operations (no external dependencies).
     */
    private GameReport parseJSON(String json) {
        GameReport report = new GameReport();

        try {
            // Parse vencedor
            String vencedor = extractJsonString(json, "vencedor");
            report.setVencedor(vencedor);

            // Parse dataHora
            String dataHoraStr = extractJsonString(json, "dataHora");
            LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
            report.setDataHora(dataHora);

            // Parse duracao
            int duracao = extractJsonInt(json, "duracao");
            report.setDuracao(duracao);

            // Parse mapaNome
            String mapaNome = extractJsonString(json, "mapaNome");
            report.setMapaNome(mapaNome);

            // Parse dificuldade
            String dificuldade = extractJsonString(json, "dificuldade");
            report.setDificuldade(dificuldade);

            // Parse totals
            int totalEnigmas = extractJsonInt(json, "totalEnigmasResolvidos");
            report.setTotalEnigmasResolvidos(totalEnigmas);

            int totalObstaculos = extractJsonInt(json, "totalObstaculos");
            report.setTotalObstaculos(totalObstaculos);

            // Parse jogadores array
            List<GameReport.PlayerReport> jogadores = parseJogadoresArray(json);
            report.setListaJogadores(jogadores);

            return report;
        } catch (Exception e) {
            System.out.println("Erro ao parsear JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts a string value from JSON.
     */
    private String extractJsonString(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIdx = json.indexOf(searchKey);
        if (startIdx == -1) return "";

        int valueStart = json.indexOf("\"", startIdx + searchKey.length());
        if (valueStart == -1) return "";

        int valueEnd = json.indexOf("\"", valueStart + 1);
        if (valueEnd == -1) return "";

        String value = json.substring(valueStart + 1, valueEnd);
        // Unescape JSON special characters
        return unescapeJson(value);
    }

    /**
     * Extracts an integer value from JSON.
     */
    private int extractJsonInt(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIdx = json.indexOf(searchKey);
        if (startIdx == -1) return 0;

        int valueStart = startIdx + searchKey.length();
        int valueEnd = json.indexOf(",", valueStart);
        if (valueEnd == -1) {
            valueEnd = json.indexOf("}", valueStart);
        }
        if (valueEnd == -1) return 0;

        String valueStr = json.substring(valueStart, valueEnd).trim();
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parses the jogadores array from JSON.
     */
    private List<GameReport.PlayerReport> parseJogadoresArray(String json) {
        List<GameReport.PlayerReport> jogadores = new ArrayList<>();

        int arrayStart = json.indexOf("\"jogadores\":");
        if (arrayStart == -1) return jogadores;

        int arrayBegin = json.indexOf("[", arrayStart);
        int arrayEnd = json.indexOf("]", arrayBegin);
        if (arrayBegin == -1 || arrayEnd == -1) return jogadores;

        String arrayContent = json.substring(arrayBegin + 1, arrayEnd);

        // Simple split by object boundaries
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
                    if (player != null) {
                        jogadores.add(player);
                    }
                    objectStart = -1;
                }
            }
        }

        return jogadores;
    }

    /**
     * Parses a single player object from JSON.
     */
    private GameReport.PlayerReport parsePlayerObject(String objectJson) {
        try {
            String nome = extractJsonString(objectJson, "nome");
            String tipo = extractJsonString(objectJson, "tipo");
            String localAtual = extractJsonString(objectJson, "localAtual");
            int turnosJogados = extractJsonInt(objectJson, "turnosJogados");
            boolean vencedor = extractJsonBoolean(objectJson, "vencedor");

            GameReport.PlayerReport player = new GameReport.PlayerReport(nome, tipo);
            player.setLocalAtual(localAtual);
            player.setTurnosJogados(turnosJogados);
            player.setVencedor(vencedor);

            // Parse percurso array
            List<String> percurso = parseStringArray(objectJson, "percurso");
            for (String sala : percurso) {
                player.adicionarPercurso(sala);
            }

            // Parse obstáculos array
            List<String> obstaculos = parseStringArray(objectJson, "obstaculos");
            for (String obstaculo : obstaculos) {
                player.adicionarObstaculo(obstaculo);
            }

            // Parse enigmas array
            List<GameReport.EnigmaEvent> enigmas = parseEnigmasArray(objectJson);
            for (GameReport.EnigmaEvent enigma : enigmas) {
                player.adicionarEnigma(enigma);
            }

            // Parse efeitos array
            List<String> efeitos = parseStringArray(objectJson, "efeitosAplicados");
            for (String efeito : efeitos) {
                player.adicionarEfeito(efeito);
            }

            return player;
        } catch (Exception e) {
            System.out.println("Erro ao parsear jogador: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses enigmas array from player object.
     */
    private List<GameReport.EnigmaEvent> parseEnigmasArray(String playerJson) {
        List<GameReport.EnigmaEvent> enigmas = new ArrayList<>();

        int arrayStart = playerJson.indexOf("\"enigmas\":");
        if (arrayStart == -1) return enigmas;

        int arrayBegin = playerJson.indexOf("[", arrayStart);
        int arrayEnd = playerJson.indexOf("]", arrayBegin);
        if (arrayBegin == -1 || arrayEnd == -1) return enigmas;

        String arrayContent = playerJson.substring(arrayBegin + 1, arrayEnd);

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
                    GameReport.EnigmaEvent enigma = parseEnigmaObject(objectJson);
                    if (enigma != null) {
                        enigmas.add(enigma);
                    }
                    objectStart = -1;
                }
            }
        }

        return enigmas;
    }

    /**
     * Parses a single enigma object from JSON.
     */
    private GameReport.EnigmaEvent parseEnigmaObject(String objectJson) {
        try {
            String pergunta = extractJsonString(objectJson, "pergunta");
            String resposta = extractJsonString(objectJson, "resposta");
            boolean resolvido = extractJsonBoolean(objectJson, "resolvido");
            String efeito = extractJsonString(objectJson, "efeito");
            String sala = extractJsonString(objectJson, "sala");

            return new GameReport.EnigmaEvent(pergunta, resposta, resolvido, efeito, sala);
        } catch (Exception e) {
            System.out.println("Erro ao parsear enigma: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses a string array from JSON.
     */
    private List<String> parseStringArray(String json, String key) {
        List<String> list = new ArrayList<>();

        int arrayStart = json.indexOf("\"" + key + "\":");
        if (arrayStart == -1) return list;

        int arrayBegin = json.indexOf("[", arrayStart);
        int arrayEnd = json.indexOf("]", arrayBegin);
        if (arrayBegin == -1 || arrayEnd == -1) return list;

        String arrayContent = json.substring(arrayBegin + 1, arrayEnd);

        // Simple string extraction
        int stringCount = 0;
        for (String item : arrayContent.split("\"")) {
            stringCount++;
            if (stringCount % 2 == 0 && !item.isEmpty() && !item.startsWith("[") && !item.startsWith("]")) {
                list.add(unescapeJson(item));
            }
        }

        return list;
    }

    /**
     * Extracts a boolean value from JSON.
     */
    private boolean extractJsonBoolean(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIdx = json.indexOf(searchKey);
        if (startIdx == -1) return false;

        int valueStart = startIdx + searchKey.length();
        int valueEnd = json.indexOf(",", valueStart);
        if (valueEnd == -1) {
            valueEnd = json.indexOf("}", valueStart);
        }
        if (valueEnd == -1) return false;

        String valueStr = json.substring(valueStart, valueEnd).trim();
        return valueStr.equalsIgnoreCase("true");
    }

    /**
     * Unescapes JSON special characters.
     */
    private String unescapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\\\", "\\")
                   .replace("\\\"", "\"")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t");
    }

    /**
     * Returns list of all GameReport objects.
     */
    public List<GameReport> carregarTodosRelatorios() {
        List<GameReport> reports = new ArrayList<>();
        List<String> filenames = listarRelatorios();

        for (String filename : filenames) {
            GameReport report = carregarRelatorio(filename);
            if (report != null) {
                reports.add(report);
            }
        }

        return reports;
    }
}
