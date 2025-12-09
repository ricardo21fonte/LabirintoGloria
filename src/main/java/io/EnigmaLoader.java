package io;

import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Lists.ArrayUnorderedList;
import enums.Dificuldade;
import game.Enigma; 

public class EnigmaLoader {

    public ArrayUnorderedList<Enigma> loadEnigmas(String filePath) {
        ArrayUnorderedList<Enigma> listaEnigmas = new ArrayUnorderedList<>();
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray enigmasArray = (JSONArray) jsonObject.get("enigmas");

            if (enigmasArray == null) {
        
                return listaEnigmas;
            }

            for (Object o : enigmasArray) {
                JSONObject enigmaJSON = (JSONObject) o;

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

                Enigma novoEnigma = new Enigma(pergunta, opcoes, correta, dif);

                String efSucesso = (String) enigmaJSON.get("sucesso");
                String efFalha = (String) enigmaJSON.get("falha");

                if (efSucesso == null) efSucesso = "NONE";
                if (efFalha == null) efFalha = "BLOCK"; 

                novoEnigma.setEfeitos(efSucesso, efFalha);

                listaEnigmas.addToRear(novoEnigma);
            } 
        } catch (Exception e) {
            System.out.println("Erro crítico ao ler enigmas: " + e.getMessage());
    
        }
        return listaEnigmas;
    }
}