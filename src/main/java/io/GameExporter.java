package io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports game reports to JSON files in the saved_games/ directory.
 */
public class GameExporter {
    private static final String SAVED_GAMES_DIR = "saved_games";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public GameExporter() {
        ensureSavedGamesDirectory();
    }

    /**
     * Ensures the saved_games directory exists, creates it if necessary.
     */
    private void ensureSavedGamesDirectory() {
        File dir = new File(SAVED_GAMES_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Pasta 'saved_games' criada com sucesso.");
            } else {
                System.out.println("Erro ao criar pasta 'saved_games'.");
            }
        }
    }

    /**
     * Exports a game report to a JSON file with timestamp filename.
     * Format: game_20250108_153045.json
     */
    public void exportarJogo(GameReport report) {
        if (report == null) {
            System.out.println("Erro: Relatório nulo.");
            return;
        }

        try {
            String timestamp = report.getDataHora().format(FORMATTER);
            String filename = SAVED_GAMES_DIR + File.separator + "game_" + timestamp + ".json";

            String jsonContent = generateJSON(report);

            FileWriter writer = new FileWriter(filename);
            writer.write(jsonContent);
            writer.close();

            System.out.println("Jogo guardado com sucesso: " + filename);
        } catch (IOException e) {
            System.out.println("Erro ao guardar jogo: " + e.getMessage());
        }
    }

    /**
     * Generates JSON string from GameReport.
     * Uses simple string concatenation to avoid external dependencies.
     */
    private String generateJSON(GameReport report) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"vencedor\": \"").append(escapeJson(report.getVencedor())).append("\",\n");
        json.append("  \"dataHora\": \"").append(report.getDataHora().toString()).append("\",\n");
        json.append("  \"duracao\": ").append(report.getDuracao()).append(",\n");
        json.append("  \"mapaNome\": \"").append(escapeJson(report.getMapaNome())).append("\",\n");
        json.append("  \"dificuldade\": \"").append(report.getDificuldade()).append("\",\n");
        json.append("  \"totalEnigmasResolvidos\": ").append(report.getTotalEnigmasResolvidos()).append(",\n");
        json.append("  \"totalObstaculos\": ").append(report.getTotalObstaculos()).append(",\n");
        json.append("  \"jogadores\": [\n");

        List<GameReport.PlayerReport> jogadores = report.getListaJogadores();
        for (int i = 0; i < jogadores.size(); i++) {
            GameReport.PlayerReport player = jogadores.get(i);
            json.append("    {\n");
            json.append("      \"nome\": \"").append(escapeJson(player.getNome())).append("\",\n");
            json.append("      \"tipo\": \"").append(player.getTipo()).append("\",\n");
            json.append("      \"localAtual\": \"").append(escapeJson(player.getLocalAtual())).append("\",\n");
            json.append("      \"turnosJogados\": ").append(player.getTurnosJogados()).append(",\n");
            json.append("      \"vencedor\": ").append(player.isVencedor()).append(",\n");
            
            // Percurso (path)
            json.append("      \"percurso\": [");
            List<String> percurso = player.getPercurso();
            for (int j = 0; j < percurso.size(); j++) {
                json.append("\"").append(escapeJson(percurso.get(j))).append("\"");
                if (j < percurso.size() - 1) json.append(", ");
            }
            json.append("],\n");
            
            // Obstáculos
            json.append("      \"obstaculos\": [");
            List<String> obstaculos = player.getObstaculos();
            for (int j = 0; j < obstaculos.size(); j++) {
                json.append("\"").append(escapeJson(obstaculos.get(j))).append("\"");
                if (j < obstaculos.size() - 1) json.append(", ");
            }
            json.append("],\n");
            
            // Enigmas
            json.append("      \"enigmas\": [\n");
            List<GameReport.EnigmaEvent> enigmas = player.getEnigmas();
            for (int j = 0; j < enigmas.size(); j++) {
                GameReport.EnigmaEvent enigma = enigmas.get(j);
                json.append("        {\n");
                json.append("          \"sala\": \"").append(escapeJson(enigma.sala)).append("\",\n");
                json.append("          \"pergunta\": \"").append(escapeJson(enigma.pergunta)).append("\",\n");
                json.append("          \"resposta\": \"").append(escapeJson(enigma.resposta)).append("\",\n");
                json.append("          \"resolvido\": ").append(enigma.resolvido).append(",\n");
                json.append("          \"efeito\": \"").append(escapeJson(enigma.efeito)).append("\"\n");
                json.append("        }");
                if (j < enigmas.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("      ],\n");
            
            // Efeitos aplicados
            json.append("      \"efeitosAplicados\": [");
            List<String> efeitos = player.getEfeitosAplicados();
            for (int j = 0; j < efeitos.size(); j++) {
                json.append("\"").append(escapeJson(efeitos.get(j))).append("\"");
                if (j < efeitos.size() - 1) json.append(", ");
            }
            json.append("],\n");
            
            json.append("      \"totalEnigmasResolvidos\": ").append(player.totalEnigmasResolvidos()).append("\n");
            json.append("    }");
            if (i < jogadores.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]\n");
        json.append("}\n");

        return json.toString();
    }

    /**
     * Escapes special characters in JSON strings.
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}