package io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Iterator; // Necessário para percorrer as listas

import Lists.ArrayUnorderedList; // A tua lista customizada

/**
 * Exports game reports to JSON files in the saved_games/ directory.
 */
public class GameExporter {
    private static final String SAVED_GAMES_DIR = "saved_games";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public GameExporter() {
        ensureSavedGamesDirectory();
    }

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

    public void exportarJogo(GameReport report) {
        if (report == null) {
            System.out.println("Erro: Relatório nulo.");
            return;
        }

        try {
            String timestamp = report.getDataHora().format(FORMATTER);
            // =================================================================
            // AQUI ESTÁ A MUDANÇA PARA O NOME DO FICHEIRO
            // =================================================================
            // 1. Vai buscar o nome do mapa (ex: "ze2")
            // 2. Limpa caracteres estranhos para não dar erro no Windows
            String nomeMapaLimpo = report.getMapaNome().replaceAll("[^a-zA-Z0-9.-]", "_");
            
            // 3. Monta o nome: relatorio_ze2_20251209... .json
            String filename = SAVED_GAMES_DIR + File.separator + "relatorio_" + nomeMapaLimpo + "_" + timestamp + ".json";
            // =================================================================

            String jsonContent = generateJSON(report);

            FileWriter writer = new FileWriter(filename);
            writer.write(jsonContent);
            writer.close();

            System.out.println("💾 Relatório guardado com sucesso: " + filename);
        } catch (IOException e) {
            System.out.println("Erro ao guardar jogo: " + e.getMessage());
        }
    }

    /**
     * Generates JSON string from GameReport.
     * Adaptado para usar ArrayUnorderedList e Iterators.
     */
    private String generateJSON(GameReport report) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"vencedor\": \"").append(escapeJson(report.getVencedor())).append("\",\n");
        json.append("  \"dataHora\": \"").append(report.getDataHora().toString()).append("\",\n");
        json.append("  \"duracao\": ").append(report.getDuracao()).append(",\n");
        
        // Aqui usamos o nome do mapa que vem do report
        json.append("  \"mapaNome\": \"").append(escapeJson(report.getMapaNome())).append("\",\n");
        
        json.append("  \"dificuldade\": \"").append(report.getDificuldade()).append("\",\n");
        json.append("  \"totalEnigmasResolvidos\": ").append(report.getTotalEnigmasResolvidos()).append(",\n");
        
        // --- ESCREVER TENTADOS ---
        json.append("  \"totalEnigmasTentados\": ").append(report.getTotalEnigmasTentados()).append(",\n");
        
        json.append("  \"totalObstaculos\": ").append(report.getTotalObstaculos()).append(",\n");
        json.append("  \"jogadores\": [\n");

        // --- MUDANÇA: Usar Iterator para os jogadores ---
        ArrayUnorderedList<GameReport.PlayerReport> jogadores = report.getListaJogadores();
        Iterator<GameReport.PlayerReport> itJogadores = jogadores.iterator();
        
        while (itJogadores.hasNext()) {
            GameReport.PlayerReport player = itJogadores.next();
            
            json.append("    {\n");
            json.append("      \"nome\": \"").append(escapeJson(player.getNome())).append("\",\n");
            json.append("      \"tipo\": \"").append(player.getTipo()).append("\",\n");
            json.append("      \"localAtual\": \"").append(escapeJson(player.getLocalAtual())).append("\",\n");
            json.append("      \"turnosJogados\": ").append(player.getTurnosJogados()).append(",\n");
            json.append("      \"vencedor\": ").append(player.isVencedor()).append(",\n");
            
            // Percurso (path)
            json.append("      \"percurso\": [");
            ArrayUnorderedList<String> percurso = player.getPercurso();
            Iterator<String> itPercurso = percurso.iterator();
            while (itPercurso.hasNext()) {
                json.append("\"").append(escapeJson(itPercurso.next())).append("\"");
                if (itPercurso.hasNext()) json.append(", ");
            }
            json.append("],\n");
            
            // Obstáculos
            json.append("      \"obstaculos\": [");
            ArrayUnorderedList<String> obstaculos = player.getObstaculos();
            Iterator<String> itObstaculos = obstaculos.iterator();
            while (itObstaculos.hasNext()) {
                json.append("\"").append(escapeJson(itObstaculos.next())).append("\"");
                if (itObstaculos.hasNext()) json.append(", ");
            }
            json.append("],\n");
            
            // Enigmas
            json.append("      \"enigmas\": [\n");
            ArrayUnorderedList<GameReport.EnigmaEvent> enigmas = player.getEnigmas();
            Iterator<GameReport.EnigmaEvent> itEnigmas = enigmas.iterator();
            while (itEnigmas.hasNext()) {
                GameReport.EnigmaEvent enigma = itEnigmas.next();
                json.append("        {\n");
                json.append("          \"sala\": \"").append(escapeJson(enigma.sala)).append("\",\n");
                json.append("          \"pergunta\": \"").append(escapeJson(enigma.pergunta)).append("\",\n");
                json.append("          \"resposta\": \"").append(escapeJson(enigma.resposta)).append("\",\n");
                json.append("          \"resolvido\": ").append(enigma.resolvido).append(",\n");
                json.append("          \"efeito\": \"").append(escapeJson(enigma.efeito)).append("\"\n");
                json.append("        }");
                if (itEnigmas.hasNext()) json.append(",");
                json.append("\n");
            }
            json.append("      ],\n");
            
            // Efeitos aplicados
            json.append("      \"efeitosAplicados\": [");
            ArrayUnorderedList<String> efeitos = player.getEfeitosAplicados();
            Iterator<String> itEfeitos = efeitos.iterator();
            while (itEfeitos.hasNext()) {
                json.append("\"").append(escapeJson(itEfeitos.next())).append("\"");
                if (itEfeitos.hasNext()) json.append(", ");
            }
            json.append("],\n");
            
            json.append("      \"totalEnigmasResolvidos\": ").append(player.totalEnigmasResolvidos()).append("\n");
            json.append("    }");
            
            if (itJogadores.hasNext()) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]\n");
        json.append("}\n");

        return json.toString();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}