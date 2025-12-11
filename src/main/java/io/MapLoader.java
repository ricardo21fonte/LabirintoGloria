package io;

import java.io.FileReader;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Lists.ArrayUnorderedList;
import Queue.LinkedQueue;
import enums.CorredorEvento;
import enums.TipoDivisao;
import game.Divisao;
import game.EventoCorredor;
import graph.LabyrinthGraph;
/**
 * Utility class responsible for loading a labyrinth map from a JSON file
 */
public class MapLoader {

    /**
     * Loads a map from a JSON file and builds a LabyrinthGraph<Divisao>.
     * @param filePath the path to the JSON map file
     * @return a fully constructed graph
     */
    public LabyrinthGraph<Divisao> loadMap(String filePath) {
        LabyrinthGraph<Divisao> graph = new LabyrinthGraph<>();
        JSONParser parser = new JSONParser();
        ArrayUnorderedList<Divisao> listaSalas = new ArrayUnorderedList<>();
        ArrayUnorderedList<String> listaCodigos = new ArrayUnorderedList<>();

        int maiorIdEncontrado = 0;

        try (FileReader reader = new FileReader(filePath)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);

            if (jsonObject == null) {
                System.out.println("ERRO: ficheiro JSON vazio ou inválido: " + filePath);
                return null;
            }

            JSONArray salas = (JSONArray) jsonObject.get("salas");
            if (salas == null) {
                System.out.println("ERRO: JSON sem array 'salas'!");
                return null;
            }

            for (Object s : salas) {
                if (!(s instanceof JSONObject)) {
                    System.out.println("AVISO: entrada de 'salas' inválida (não é JSON object)");
                    continue;
                }

                JSONObject salaJSON = (JSONObject) s;

                String codigo = safeGetString(salaJSON, "codigo");
                String nome   = safeGetString(salaJSON, "nome");
                String tipoStr= safeGetString(salaJSON, "tipo");

                if (codigo == null || nome == null || tipoStr == null) {
                    System.out.println("AVISO: sala ignorada por falta de campos obrigatórios: " + salaJSON);
                    continue;
                }

                TipoDivisao tipo;
                try {
                    tipo = TipoDivisao.valueOf(tipoStr);
                } catch (IllegalArgumentException e) {
                    System.out.println("AVISO: tipo de sala inválido '" + tipoStr + "' em " + codigo + ". Sala ignorada.");
                    continue;
                }

                Divisao d = new Divisao(nome, tipo);

                try {
                    if (codigo.startsWith("S")) {
                        int idLido = Integer.parseInt(codigo.substring(1));
                        d.definirIdManual(idLido);

                        if (idLido > maiorIdEncontrado) {
                            maiorIdEncontrado = idLido;
                        }
                    } else {
                        if (d.getId() > maiorIdEncontrado) {
                            maiorIdEncontrado = d.getId();
                        }
                    }
                } catch (Exception e) {
                    if (d.getId() > maiorIdEncontrado) maiorIdEncontrado = d.getId();
                }

                Object idDesbObj = salaJSON.get("idDesbloqueio");
                if (tipo == TipoDivisao.SALA_ALAVANCA && idDesbObj instanceof Long) {
                    d.setIdDesbloqueio(((Long) idDesbObj).intValue());
                }

                graph.addVertex(d);
                listaSalas.addToRear(d);
                listaCodigos.addToRear(codigo);
            }
            Divisao.setNextId(maiorIdEncontrado + 1);

            JSONArray ligacoes = (JSONArray) jsonObject.get("ligacoes");
            if (ligacoes == null) {
                // Sem ligações ainda é um mapa válido, só isolado
                System.out.println("AVISO: mapa sem 'ligacoes'.");
            } else {
                for (Object l : ligacoes) {
                    if (!(l instanceof JSONObject)) {
                        System.out.println("AVISO: entrada em 'ligacoes' inválida. Ignorada.");
                        continue;
                    }

                    JSONObject lig = (JSONObject) l;

                    String origem  = safeGetString(lig, "origem");
                    String destino = safeGetString(lig, "destino");
                    String eventoStr = safeGetString(lig, "evento");
                    Object valObj = lig.get("valor");

                    if (origem == null || destino == null) {
                        System.out.println("AVISO: ligação sem 'origem' ou 'destino'. Ignorada: " + lig);
                        continue;
                    }

                    int valor = 0;
                    if (valObj instanceof Long) {
                        valor = ((Long) valObj).intValue();
                    }

                    Divisao d1 = findDiv(listaSalas, listaCodigos, origem);
                    Divisao d2 = findDiv(listaSalas, listaCodigos, destino);

                    if (d1 == null || d2 == null) {
                        System.out.println("AVISO: ligação com origem/destino inexistente: "
                                + origem + " -> " + destino + ". Ignorada.");
                        continue;
                    }

                    CorredorEvento tipoEv = CorredorEvento.NONE;
                    if (eventoStr != null) {
                        try {
                            tipoEv = CorredorEvento.valueOf(eventoStr);
                        } catch (IllegalArgumentException e) {
                            System.out.println("AVISO: evento de corredor inválido '" + eventoStr
                                    + "' em ligação " + origem + " -> " + destino
                                    + ". Usado NONE.");
                        }
                    }

                    graph.addCorredor(d1, d2, new EventoCorredor(tipoEv, valor));
                }
            }
        } catch (Exception e) {
            return null;
        }

        if (!validarCaminho(graph)) {
            System.out.println(" O mapa carregado tem erros estruturais e não pode ser jogado.");
            return null;
        }

        return graph;
    }

    /**
     * Looks up a Divisao
     *
     * @param salas    list of all rooms loaded from the file
     * @param codigos  list of the corresponding textual codes
     * @param alvo     the target code to search for
     * @return the matching Divisao
     */
    private Divisao findDiv(ArrayUnorderedList<Divisao> salas, ArrayUnorderedList<String> codigos, String alvo) {
        var itSalas = salas.iterator();
        var itCodigos = codigos.iterator();
        while (itSalas.hasNext() && itCodigos.hasNext()) {
            Divisao d = itSalas.next();
            String cod = itCodigos.next();
            if (cod != null && cod.equals(alvo)) return d;
        }
        return null;
    }

    /**
     * Reads an String from a JSONObject
     */
    private String safeGetString(JSONObject obj, String key) {
        Object val = obj.get(key);
        if (val instanceof String) {
            String s = ((String) val).trim();
            if (!s.isEmpty()) return s;
        }
        return null;
    }

    /**
     * Validates whether the loaded map is playable
     */
    private boolean validarCaminho(LabyrinthGraph<Divisao> graph) {
        if (graph == null || graph.size() == 0) return false;

        LinkedQueue<Divisao> queue = new LinkedQueue<>();
        ArrayUnorderedList<Divisao> visitados = new ArrayUnorderedList<>();

        boolean temEntrada = false;
        boolean temTesouro = false;

        Object[] vertices = graph.getVertices();
        for (Object v : vertices) {
            Divisao d = (Divisao) v;

            if (d.getTipo() == TipoDivisao.ENTRADA) {
                queue.enqueue(d);
                visitados.addToRear(d);
                temEntrada = true;
            }

            if (d.getTipo() == TipoDivisao.SALA_CENTRAL) {
                temTesouro = true;
            }
        }

        if (!temEntrada) {
            System.out.println("Mapa Inválido: Não existe nenhuma sala do tipo 'ENTRADA'.");
            return false;
        }
        if (!temTesouro) {
            System.out.println("Mapa Inválido: Não existe a 'Câmara do Tesouro' (SALA_CENTRAL).");
            return false;
        }

        while (!queue.isEmpty()) {
            try {
                Divisao atual = queue.dequeue();

                if (atual.getTipo() == TipoDivisao.SALA_CENTRAL) {
                    return true;
                }

                Iterator<Divisao> it = graph.getVizinhos(atual).iterator();
                while (it.hasNext()) {
                    Divisao vizinho = it.next();
                    if (!visitados.contains(vizinho)) {
                        visitados.addToRear(vizinho);
                        queue.enqueue(vizinho);
                    }
                }
            } catch (Exception e) { break; }
        }

        System.out.println("Mapa Inválido: A Câmara do Tesouro está isolada (sem caminho a partir das Entradas)!");
        return false;
    }
}
