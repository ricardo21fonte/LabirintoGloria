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
import graph.LabyrinthGraph;

public class MapLoader {

    /**
     * Carrega um mapa em formato JSON e constrói o grafo LabyrinthGraph<Divisao>.
     *
     * @param filePath caminho para o ficheiro JSON
     * @return grafo carregado ou null em caso de erro grave de leitura
     */
    public LabyrinthGraph<Divisao> loadMap(String filePath) {
        LabyrinthGraph<Divisao> graph = new LabyrinthGraph<>();
        JSONParser parser = new JSONParser();
        ArrayUnorderedList<Divisao> listaSalas = new ArrayUnorderedList<>();
        ArrayUnorderedList<String> listaCodigos = new ArrayUnorderedList<>();

        int maiorIdEncontrado = 0; // Para rastrear o maior ID

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

            // ============================
            // 1) CARREGAR AS SALAS
            // ============================
            for (Object s : salas) {
                if (!(s instanceof JSONObject)) {
                    System.out.println("AVISO: entrada de 'salas' inválida (não é JSON object)");
                    continue;
                }

                JSONObject salaJSON = (JSONObject) s;

                // Ler campos obrigatórios com validação
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

                // --- CORREÇÃO DE IDS ---
                // Extrair o número do código (S5 -> 5), se fizer sentido
                try {
                    if (codigo.startsWith("S")) {
                        int idLido = Integer.parseInt(codigo.substring(1));
                        d.definirIdManual(idLido); // Força o ID do ficheiro

                        if (idLido > maiorIdEncontrado) {
                            maiorIdEncontrado = idLido;
                        }
                    } else {
                        // Se for, por ex., "T1", não mexemos, usamos o ID automático
                        if (d.getId() > maiorIdEncontrado) {
                            maiorIdEncontrado = d.getId();
                        }
                    }
                } catch (Exception e) {
                    // Se falhar o parse, mantemos o ID automático
                    if (d.getId() > maiorIdEncontrado) maiorIdEncontrado = d.getId();
                }
                // -----------------------

                // idDesbloqueio só faz sentido em SALA_ALAVANCA
                Object idDesbObj = salaJSON.get("idDesbloqueio");
                if (tipo == TipoDivisao.SALA_ALAVANCA && idDesbObj instanceof Long) {
                    d.setIdDesbloqueio(((Long) idDesbObj).intValue());
                }

                graph.addVertex(d);
                listaSalas.addToRear(d);
                listaCodigos.addToRear(codigo);
            }

            // ATUALIZAR O CONTADOR ESTÁTICO GLOBAL
            // Garante que a próxima sala criada terá um ID novo e único
            Divisao.setNextId(maiorIdEncontrado + 1);

            // ============================
            // 2) CARREGAR AS LIGAÇÕES
            // ============================
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
                    String eventoStr = safeGetString(lig, "evento"); // pode ser null, tratamos já abaixo
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

                    graph.addCorridor(d1, d2, new EventoCorredor(tipoEv, valor));
                }
            }
        } catch (Exception e) {
            System.out.println("ERRO ao carregar mapa de '" + filePath + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return graph;
    }

    /**
     * Procura uma divisao pelo código textual (ex: "S3") usando as listas paralelas.
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
     * Lê uma String de um JSONObject de forma segura, devolvendo null se não existir
     * ou não for String.
     */
    private String safeGetString(JSONObject obj, String key) {
        Object val = obj.get(key);
        if (val instanceof String) {
            String s = ((String) val).trim();
            if (!s.isEmpty()) return s;
        }
        return null;
    }
}