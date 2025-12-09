package io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Iterator; 

import Lists.ArrayUnorderedList; 

public class GameExporter {
    
    // --- MUDANÇA 1: Nova pasta para relatórios ---
    private static final String RELATORIOS_DIR = "resources/saved_relatorios";
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public GameExporter() {
        ensureDirectory();
    }

    private void ensureDirectory() {
        File dir = new File(RELATORIOS_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Pasta 'saved_relatorios' criada.");
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
            
            // --- MUDANÇA 2: Usar o nome do mapa no ficheiro ---
            String nomeMapaLimpo = report.getMapaNome().replaceAll("[^a-zA-Z0-9.-]", "_");
            
            String filename = RELATORIOS_DIR + File.separator + "relatorio_" + nomeMapaLimpo + "_" + timestamp + ".json";

            String jsonContent = generateJSON(report);

            FileWriter writer = new FileWriter(filename);
            writer.write(jsonContent);
            writer.close();

            System.out.println("💾 Relatório guardado: " + filename);
        } catch (IOException e) {
            System.out.println("Erro ao guardar jogo: " + e.getMessage());
        }
    }

    // ... (MANTÉM O RESTO DO MÉTODO generateJSON e escapeJson IGUAIS AO QUE ME MOSTRASTE) ...
    // O teu método generateJSON já está correto (tem o totalEnigmasTentados).
    // Só precisas de copiar o resto do ficheiro aqui para baixo.
    
    private String generateJSON(GameReport report) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"vencedor\": \"").append(escapeJson(report.getVencedor())).append("\",\n");
        json.append("  \"dataHora\": \"").append(report.getDataHora().toString()).append("\",\n");
        json.append("  \"duracao\": ").append(report.getDuracao()).append(",\n");
        json.append("  \"mapaNome\": \"").append(escapeJson(report.getMapaNome())).append("\",\n");
        json.append("  \"dificuldade\": \"").append(report.getDificuldade()).append("\",\n");
        json.append("  \"totalEnigmasResolvidos\": ").append(report.getTotalEnigmasResolvidos()).append(",\n");
        json.append("  \"totalEnigmasTentados\": ").append(report.getTotalEnigmasTentados()).append(",\n");
        json.append("  \"totalObstaculos\": ").append(report.getTotalObstaculos()).append(",\n");
        json.append("  \"jogadores\": [\n");

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
            
            // Percurso
            json.append("      \"percurso\": [");
            Iterator<String> itPercurso = player.getPercurso().iterator();
            while (itPercurso.hasNext()) {
                json.append("\"").append(escapeJson(itPercurso.next())).append("\"");
                if (itPercurso.hasNext()) json.append(", ");
            }
            json.append("],\n");
            
            // Obstáculos
            json.append("      \"obstaculos\": [");
            Iterator<String> itObstaculos = player.getObstaculos().iterator();
            while (itObstaculos.hasNext()) {
                json.append("\"").append(escapeJson(itObstaculos.next())).append("\"");
                if (itObstaculos.hasNext()) json.append(", ");
            }
            json.append("],\n");
            
            // Enigmas
            json.append("      \"enigmas\": [\n");
            Iterator<GameReport.EnigmaEvent> itEnigmas = player.getEnigmas().iterator();
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
            
            // Efeitos
            json.append("      \"efeitosAplicados\": [");
            Iterator<String> itEfeitos = player.getEfeitosAplicados().iterator();
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