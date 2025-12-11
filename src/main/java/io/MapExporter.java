package io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import Lists.ArrayUnorderedList;
import game.Divisao;
import game.EventoCorredor;
import graph.LabyrinthGraph;

/**
 * Class responsible for exporting a LabyrinthGraph to a JSON file.
 */
public class MapExporter {

    /**
     * Exports the labyrinth graph to a JSON file.
     * @param grafo       the labyrinth graph to export
     * @param nomeMapa    the logical/visible name of the map
     * @param nomeFicheiro the output file path/name
     */
    public void exportarMapa(LabyrinthGraph<Divisao> grafo, String nomeMapa, String nomeFicheiro) {
        StringBuilder json = new StringBuilder();
        
        json.append("{\n");
        json.append("  \"nome\": \"").append(escapeJson(nomeMapa)).append("\",\n");

        json.append("  \"salas\": [\n");
        
        Object[] vertices = grafo.getVertices();
        for (int i = 0; i < vertices.length; i++) {
            Divisao sala = (Divisao) vertices[i];

            String codigo = "S" + sala.getId(); 
            
            json.append("    { ");
            json.append("\"codigo\": \"").append(codigo).append("\", ");
            json.append("\"tipo\": \"").append(sala.getTipo().toString()).append("\", ");
            json.append("\"nome\": \"").append(escapeJson(sala.getNome())).append("\"");

            if (sala.getIdDesbloqueio() != -1) {
                 json.append(", \"idDesbloqueio\": ").append(sala.getIdDesbloqueio());
            }

            json.append(" }");
            
            if (i < vertices.length - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");

        //Grava Ligacao
        json.append("  \"ligacoes\": [\n");
        
        boolean primeiraLigacao = true;
        
        for (int i = 0; i < vertices.length; i++) {
            Divisao origem = (Divisao) vertices[i];
            String codOrigem = "S" + origem.getId();
            
            ArrayUnorderedList<Divisao> vizinhos = grafo.getVizinhos(origem);
            Iterator<Divisao> itViz = vizinhos.iterator();
            
            while (itViz.hasNext()) {
                Divisao destino = itViz.next();
                String codDestino = "S" + destino.getId();

                if (origem.compareTo(destino) < 0) {
                    
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

        //Escreve no ficheiro
        try (FileWriter writer = new FileWriter(nomeFicheiro)) {
            writer.write(json.toString());
            System.out.println("Mapa gravado com sucesso em: " + nomeFicheiro);
        } catch (IOException e) {
            System.out.println("Erro ao gravar o mapa: " + e.getMessage());
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    /**
     * Exports the given labyrinth graph to a DOT format file.
     * @param grafo        the labyrinth graph to be exported as DOT
     * @param nomeFicheiro the path and name of the output DOT file to create
     */
    public void exportarDot(LabyrinthGraph<Divisao> grafo, String nomeFicheiro) {
        String dotContent = grafo.toDotString();

        // Escreve no ficheiro
        try (FileWriter writer = new FileWriter(nomeFicheiro)) {
            writer.write(dotContent);
            System.out.println("Mapa DOT gravado com sucesso em: " + nomeFicheiro);
        } catch (IOException e) {
            System.out.println("Erro ao gravar o mapa DOT: " + e.getMessage());
        }
    }
}