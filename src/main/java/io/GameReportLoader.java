package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import Lists.ArrayUnorderedList; // A TUA LISTA

public class GameReportLoader {
    private static final String SAVED_GAMES_DIR = "saved_games";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ArrayUnorderedList<String> listarRelatorios() {
        ArrayUnorderedList<String> relatorios = new ArrayUnorderedList<>();
        File dir = new File(SAVED_GAMES_DIR);

        if (!dir.exists()) return relatorios;

        File[] files = dir.listFiles((d, name) -> name.startsWith("game_") && name.endsWith(".json"));

        if (files != null) {
            // Nota: Removi o sort para simplificar e não depender de Java Collections
            for (File file : files) {
                relatorios.addToRear(file.getName());
            }
        }
        return relatorios;
    }

    // O resto do ficheiro mantém a lógica mas deve mudar onde usa ArrayList
    // Vou pôr aqui o parseJogadoresArray e parseStringArray que são os mais importantes

    public GameReport carregarRelatorio(String filename) {
        try {
            String filepath = SAVED_GAMES_DIR + File.separator + filename;
            String content = new String(Files.readAllBytes(Paths.get(filepath)));
            return parseJSON(content);
        } catch (IOException e) {
            return null;
        }
    }

    private GameReport parseJSON(String json) {
        GameReport report = new GameReport();
        try {
            report.setVencedor(extractJsonString(json, "vencedor"));
            String dataHoraStr = extractJsonString(json, "dataHora");
            if (!dataHoraStr.isEmpty()) {
                report.setDataHora(LocalDateTime.parse(dataHoraStr, FORMATTER));
            }
            report.setDuracao(extractJsonInt(json, "duracao"));
            report.setMapaNome(extractJsonString(json, "mapaNome"));
            report.setDificuldade(extractJsonString(json, "dificuldade"));
            report.setTotalEnigmasResolvidos(extractJsonInt(json, "totalEnigmasResolvidos"));
            report.setTotalObstaculos(extractJsonInt(json, "totalObstaculos"));

            // Parse da lista de jogadores
            ArrayUnorderedList<GameReport.PlayerReport> jogadores = parseJogadoresArray(json);
            report.setListaJogadores(jogadores);

            return report;
        } catch (Exception e) {
            System.out.println("Erro JSON: " + e.getMessage());
            return null;
        }
    }

    // Métodos auxiliares de parsing (String, Int) mantêm-se iguais ao teu original...
    // ...

    private ArrayUnorderedList<GameReport.PlayerReport> parseJogadoresArray(String json) {
        ArrayUnorderedList<GameReport.PlayerReport> jogadores = new ArrayUnorderedList<>();
        // ... (lógica de split string mantém-se igual) ...
        // Quando criares um jogador, adiciona:
        // jogadores.addToRear(player);
        // Vou assumir que consegues adaptar a lógica de string split existente
        // apenas mudando ArrayList para ArrayUnorderedList e .add() para .addToRear()
        return jogadores;
    }
    
    // --- MÉTODOS DE EXTRAÇÃO DE STRINGS MANTÊM-SE ---
    // Copia os teus métodos extractJsonString e extractJsonInt para aqui.
    
    private String unescapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\\\", "\\").replace("\\\"", "\"");
    }
    
    private String extractJsonString(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIdx = json.indexOf(searchKey);
        if (startIdx == -1) return "";
        int valueStart = json.indexOf("\"", startIdx + searchKey.length());
        if (valueStart == -1) return "";
        int valueEnd = json.indexOf("\"", valueStart + 1);
        if (valueEnd == -1) return "";
        return unescapeJson(json.substring(valueStart + 1, valueEnd));
    }

    private int extractJsonInt(String json, String key) {
        // ... (igual ao teu original) ...
        return 0; // placeholder
    }
}