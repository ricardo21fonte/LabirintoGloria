package io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import Lists.ArrayUnorderedList;
import game.Divisao;
import game.EventoCorredor;
import game.LabyrinthGraph; // A tua lista

public class MapExporter {

    /**
     * Grava o grafo atual num ficheiro JSON.
     * @param grafo O labirinto a gravar.
     * @param nomeMapa O nome interno do mapa (ex: "Mapa Gerado 1").
     * @param nomeFicheiro O caminho do ficheiro (ex: "meu_mapa.json").
     */
    public void exportarMapa(LabyrinthGraph<Divisao> grafo, String nomeMapa, String nomeFicheiro) {
        StringBuilder json = new StringBuilder();
        
        json.append("{\n");
        json.append("  \"nome\": \"").append(escapeJson(nomeMapa)).append("\",\n");
        
        // --- 1. GRAVAR SALAS ---
        json.append("  \"salas\": [\n");
        
        Object[] vertices = grafo.getVertices();
        for (int i = 0; i < vertices.length; i++) {
            Divisao sala = (Divisao) vertices[i];
            
            // Gerar um código único baseado no ID (ex: "S1", "S2")
            // Usamos o hashCode ou um contador simples se o ID não for acessível diretamente
            // Como adicionaste um ID único na Divisao, podes usar algo como:
            String codigo = "S" + sala.hashCode(); 
            
            json.append("    { ");
            json.append("\"codigo\": \"").append(codigo).append("\", ");
            json.append("\"tipo\": \"").append(sala.getTipo().toString()).append("\", ");
            json.append("\"nome\": \"").append(escapeJson(sala.getNome())).append("\"");
            json.append(" }");
            
            if (i < vertices.length - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");

        // --- 2. GRAVAR LIGAÇÕES ---
        json.append("  \"ligacoes\": [\n");
        
        // Para evitar duplicados (A->B e B->A), usamos uma lógica de comparação
        // Como o iterador do grafo pode ser complexo, vamos iterar todos os vértices e os seus vizinhos
        
        boolean primeiraLigacao = true;
        
        for (int i = 0; i < vertices.length; i++) {
            Divisao origem = (Divisao) vertices[i];
            String codOrigem = "S" + origem.hashCode();
            
            ArrayUnorderedList<Divisao> vizinhos = grafo.getVizinhos(origem);
            Iterator<Divisao> itViz = vizinhos.iterator();
            
            while (itViz.hasNext()) {
                Divisao destino = itViz.next();
                String codDestino = "S" + destino.hashCode();
                
                // TRUQUE: Só gravamos se o código da origem for "menor" que o do destino (alfabeticamente ou hash)
                // Isto evita gravar a ligação A->B e depois B->A novamente.
                if (codOrigem.compareTo(codDestino) < 0) {
                    
                    if (!primeiraLigacao) json.append(",\n");
                    
                    EventoCorredor evento = grafo.getCorredorEvento(origem, destino);
                    
                    json.append("    { ");
                    json.append("\"origem\": \"").append(codOrigem).append("\", ");
                    json.append("\"destino\": \"").append(codDestino).append("\", ");
                    json.append("\"evento\": \"").append(evento.getTipo().toString()).append("\", ");
                    json.append("\"valor\": ").append(evento.getValor());
                    json.append(" }");
                    
                    primeiraLigacao = false;
                }
            }
        }
        
        json.append("\n  ]\n");
        json.append("}");

        // --- 3. ESCREVER NO FICHEIRO ---
        try (FileWriter writer = new FileWriter(nomeFicheiro)) {
            writer.write(json.toString());
            System.out.println("💾 Mapa gravado com sucesso em: " + nomeFicheiro);
        } catch (IOException e) {
            System.out.println("❌ Erro ao gravar o mapa: " + e.getMessage());
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}