package io;

import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Lists.ArrayUnorderedList;
import enums.CorredorEvento;
import enums.TipoDivisao;
import game.Divisao;
import game.EventoCorredor;
import game.LabyrinthGraph;

public class MapLoader {

    public LabyrinthGraph<Divisao> loadMap(String filePath) {
        LabyrinthGraph<Divisao> graph = new LabyrinthGraph<>();
        JSONParser parser = new JSONParser();
        ArrayUnorderedList<Divisao> listaSalas = new ArrayUnorderedList<>();
        ArrayUnorderedList<String> listaCodigos = new ArrayUnorderedList<>();

        int maiorIdEncontrado = 0; // Para rastrear o maior ID

        try (FileReader reader = new FileReader(filePath)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            
            JSONArray salas = (JSONArray) jsonObject.get("salas");
            if (salas == null) throw new Exception("JSON sem 'salas'!");

            for (Object s : salas) {
                JSONObject salaJSON = (JSONObject) s;
                String codigo = (String) salaJSON.get("codigo"); // Ex: "S5"
                String nome = (String) salaJSON.get("nome");
                TipoDivisao tipo = TipoDivisao.valueOf((String) salaJSON.get("tipo"));

                Divisao d = new Divisao(nome, tipo);
                
                // --- CORREÇÃO DE IDS ---
                // Extrair o número do código (S5 -> 5)
                try {
                    if (codigo.startsWith("S")) {
                        int idLido = Integer.parseInt(codigo.substring(1));
                        d.definirIdManual(idLido); // Força o ID do ficheiro
                        
                        if (idLido > maiorIdEncontrado) {
                            maiorIdEncontrado = idLido;
                        }
                    }
                } catch (Exception e) {
                    // Se falhar o parse (ex: código "T1"), mantém o ID automático gerado pelo construtor
                    // e tentamos ver se o ID automático é maior
                    if (d.getId() > maiorIdEncontrado) maiorIdEncontrado = d.getId();
                }
                // -----------------------

                if (tipo == TipoDivisao.SALA_ALAVANCA && salaJSON.get("idDesbloqueio") != null) {
                    d.setIdDesbloqueio(((Long) salaJSON.get("idDesbloqueio")).intValue());
                }
                
                graph.addVertex(d);
                listaSalas.addToRear(d);
                listaCodigos.addToRear(codigo);
            }

            // ATUALIZAR O CONTADOR ESTÁTICO GLOBAL
            // Garante que a próxima sala criada terá um ID novo e único
            Divisao.setNextId(maiorIdEncontrado + 1);

            JSONArray ligacoes = (JSONArray) jsonObject.get("ligacoes");
            if (ligacoes != null) {
                for (Object l : ligacoes) {
                    JSONObject lig = (JSONObject) l;
                    String origem = (String) lig.get("origem");
                    String destino = (String) lig.get("destino");
                    String eventoStr = (String) lig.get("evento");
                    int valor = lig.get("valor") != null ? ((Long) lig.get("valor")).intValue() : 0;

                    Divisao d1 = findDiv(listaSalas, listaCodigos, origem);
                    Divisao d2 = findDiv(listaSalas, listaCodigos, destino);

                    if (d1 != null && d2 != null) {
                        CorredorEvento tipoEv = CorredorEvento.NONE;
                        try { tipoEv = CorredorEvento.valueOf(eventoStr); } catch (Exception e) {}
                        graph.addCorridor(d1, d2, new EventoCorredor(tipoEv, valor));
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return graph;
    }

    private Divisao findDiv(ArrayUnorderedList<Divisao> salas, ArrayUnorderedList<String> codigos, String alvo) {
        var itSalas = salas.iterator();
        var itCodigos = codigos.iterator();
        while (itSalas.hasNext() && itCodigos.hasNext()) {
            Divisao d = itSalas.next();
            if (itCodigos.next().equals(alvo)) return d;
        }
        return null;
    }
}