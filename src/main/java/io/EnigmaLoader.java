package io;

import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import game.Enigma;
import enums.Dificuldade;
import Lists.ArrayUnorderedList; // A tua lista

public class EnigmaLoader {

    @SuppressWarnings("unchecked")
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
                
                String pergunta = (String) enigmaJSON.get("pergunta");
                String difStr = (String) enigmaJSON.get("dificuldade");
                
                // O JSON-simple lê números como Long, convertemos para int
                long corretaLong = (Long) enigmaJSON.get("correta");
                int correta = (int) corretaLong;
                
                // Ler Opções
                JSONArray opcoesJSON = (JSONArray) enigmaJSON.get("opcoes");
                String[] opcoes = new String[opcoesJSON.size()];
                for (int i = 0; i < opcoesJSON.size(); i++) {
                    opcoes[i] = (String) opcoesJSON.get(i);
                }

                // Converter String "FACIL" para Enum Dificuldade.FACIL
                Dificuldade dif = Dificuldade.valueOf(difStr);

                // Criar objeto e adicionar à lista
                Enigma novoEnigma = new Enigma(pergunta, opcoes, correta, dif);
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