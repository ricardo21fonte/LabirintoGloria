package io;

import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import game.Divisao;
import game.LabyrinthGraph;
import game.EventoCorredor;
import enums.TipoDivisao;
import enums.CorredorEvento;
import Lists.ArrayUnorderedList;

public class MapLoader {

    public LabyrinthGraph<Divisao> loadMap(String filePath) {
        System.out.println("DEBUG: A iniciar MapLoader para: " + filePath);
        LabyrinthGraph<Divisao> graph = new LabyrinthGraph<>();
        JSONParser parser = new JSONParser();

        // Listas auxiliares simples
        ArrayUnorderedList<Divisao> listaSalas = new ArrayUnorderedList<>();
        ArrayUnorderedList<String> listaCodigos = new ArrayUnorderedList<>();

        try {
            System.out.println("DEBUG: A tentar abrir o ficheiro...");
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            System.out.println("DEBUG: JSON lido! Nome do mapa: " + jsonObject.get("nome"));

            // --- 1. LER SALAS ---
            System.out.println("DEBUG: A ler salas...");
            JSONArray salas = (JSONArray) jsonObject.get("salas");

            if (salas == null) throw new Exception("O array 'salas' não existe no JSON!");

            for (Object s : salas) {
                JSONObject salaJSON = (JSONObject) s;
                String codigo = (String) salaJSON.get("codigo");
                String nome   = (String) salaJSON.get("nome");
                String tipoStr = (String) salaJSON.get("tipo");

                System.out.println("   -> A ler sala: " + codigo + " (" + tipoStr + ")");

                // Converter Enum TipoDivisao
                TipoDivisao tipo;
                try {
                    tipo = TipoDivisao.valueOf(tipoStr);
                } catch (IllegalArgumentException e) {
                    throw new Exception("Tipo de sala inválido no JSON: " + tipoStr);
                }

                Divisao d = new Divisao(nome, tipo);
                graph.addVertex(d);

                listaSalas.addToRear(d);
                listaCodigos.addToRear(codigo);
            }

            // --- 2. LER LIGAÇÕES ---
            System.out.println("DEBUG: A ler ligações...");
            JSONArray ligacoes = (JSONArray) jsonObject.get("ligacoes");

            if (ligacoes == null) throw new Exception("O array 'ligacoes' não existe no JSON!");

            for (Object l : ligacoes) {
                JSONObject lig = (JSONObject) l;
                String origem   = (String) lig.get("origem");
                String destino  = (String) lig.get("destino");
                String eventoStr = (String) lig.get("evento");

                System.out.println("   -> A ligar " + origem + " a " + destino +
                        " | evento=" + eventoStr);

                int valor = 0;
                if (lig.get("valor") != null) {
                    valor = ((Long) lig.get("valor")).intValue();
                }

                Divisao divOrigem = findDiv(listaSalas, listaCodigos, origem);
                Divisao divDestino = findDiv(listaSalas, listaCodigos, destino);

                if (divOrigem != null && divDestino != null) {

                    // Converter string do JSON para o enum CorredorEvento
                    CorredorEvento tipoEvento;
                    try {
                        tipoEvento = CorredorEvento.valueOf(eventoStr);
                    } catch (IllegalArgumentException e) {
                        System.out.println("      AVISO: Evento '" + eventoStr +
                                "' desconhecido. A assumir NONE.");
                        tipoEvento = CorredorEvento.NONE;
                    }

                    EventoCorredor evento = new EventoCorredor(tipoEvento, valor);
                    graph.addCorridor(divOrigem, divDestino, evento);

                } else {
                    System.out.println("      ERRO: Sala de origem ou destino não encontrada!");
                }
            }

            System.out.println("DEBUG: Leitura concluída com sucesso!");

        } catch (Exception e) {
            System.out.println("\n🚨 ERRO CRÍTICO NO MAPLOADER 🚨");
            System.out.println("Mensagem: " + e.getMessage());
            System.out.println("Causa provável: Nomes errados no JSON ou Enum diferente.");
            e.printStackTrace();
            return null;
        }
        return graph;
    }

    private Divisao findDiv(ArrayUnorderedList<Divisao> salas,
                            ArrayUnorderedList<String> codigos,
                            String codigoAlvo) {
        java.util.Iterator<Divisao> itSalas = salas.iterator();
        java.util.Iterator<String> itCodigos = codigos.iterator();
        while (itSalas.hasNext() && itCodigos.hasNext()) {
            Divisao d = itSalas.next();
            String c  = itCodigos.next();
            if (c.equals(codigoAlvo)) return d;
        }
        return null;
    }
}
