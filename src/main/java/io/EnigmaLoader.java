package io;

import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import game.Enigma;
import enums.Dificuldade;
import Lists.ArrayUnorderedList; // Verifica se o package é Lists ou structures

public class EnigmaLoader {

    public ArrayUnorderedList<Enigma> loadEnigmas(String filePath) {
        System.out.println("DEBUG: A carregar enigmas de " + filePath);
        ArrayUnorderedList<Enigma> listaEnigmas = new ArrayUnorderedList<>();
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray enigmasArray = (JSONArray) jsonObject.get("enigmas");

            if (enigmasArray == null) {
                System.out.println("ERRO: Não encontrei a lista 'enigmas' no JSON.");
                return listaEnigmas;
            }

            for (Object o : enigmasArray) {
                JSONObject enigmaJSON = (JSONObject) o;

                // 1. Ler dados básicos
                String pergunta = (String) enigmaJSON.get("pergunta");
                String difStr = (String) enigmaJSON.get("dificuldade");

                long corretaLong = (Long) enigmaJSON.get("correta");
                int correta = (int) corretaLong;

                JSONArray opcoesJSON = (JSONArray) enigmaJSON.get("opcoes");
                String[] opcoes = new String[opcoesJSON.size()];
                for (int i = 0; i < opcoesJSON.size(); i++) {
                    opcoes[i] = (String) opcoesJSON.get(i);
                }

                Dificuldade dif = Dificuldade.valueOf(difStr);

                // Criar o objeto Enigma
                Enigma novoEnigma = new Enigma(pergunta, opcoes, correta, dif);

                // =======================================================
                // 2. NOVA FUNCIONALIDADE: Ler Efeitos (Sucesso/Falha)
                // =======================================================
                String efSucesso = (String) enigmaJSON.get("sucesso");
                String efFalha = (String) enigmaJSON.get("falha");

                // Definir valores padrão caso o JSON não tenha estes campos
                if (efSucesso == null) efSucesso = "NONE";
                if (efFalha == null) efFalha = "BLOCK"; // Por defeito, errar bloqueia o turno

                // Injetar os efeitos no enigma
                novoEnigma.setEfeitos(efSucesso, efFalha);
                // =======================================================

                listaEnigmas.addToRear(novoEnigma);
            }

            System.out.println("✅ Sucesso! " + listaEnigmas.size() + " enigmas carregados.");

        } catch (Exception e) {
            System.out.println("❌ Erro ao ler enigmas: " + e.getMessage());
            e.printStackTrace();
        }
        return listaEnigmas;
    }
}