package io;

import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Lists.ArrayUnorderedList;
import enums.Dificuldade;
import game.Enigma;
/**
 * Class responsible for loading Enigma instances from an JSON file.
 */
public class EnigmaLoader {
    /**
     * Loads all enigmas from the given JSON file path and returns them as ArrayUnorderedList.
     * @param filePath the path to the JSON file containing the enigmas
     * @return a list with all successfully loaded {@link Enigma} objects
     */
    public ArrayUnorderedList<Enigma> loadEnigmas(String filePath) {
        ArrayUnorderedList<Enigma> listaEnigmas = new ArrayUnorderedList<>();
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray enigmasArray = (JSONArray) jsonObject.get("enigmas");

            if (enigmasArray == null) {
                // Se não houver lista, devolve vazio
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

                Dificuldade dif;
                try {
                    dif = Dificuldade.valueOf(difStr);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                //Cria o Enigma
                Enigma novoEnigma = new Enigma(pergunta, opcoes, correta, dif);

                String efSucesso;
                String efFalha;

                switch (dif) {
                    case FACIL:
                        efSucesso = "EXTRA_TURN";
                        efFalha   = "BACK:1";
                        break;

                    case MEDIO:
                        efSucesso = "EXTRA_TURN";
                        efFalha   = "BACK:2";
                        break;

                    case DIFICIL:
                        efSucesso = "EXTRA_TURN";
                        efFalha   = "BLOCK";
                        break;

                    default:
                        efSucesso = "NONE";
                        efFalha   = "NONE";
                        break;
                }

                novoEnigma.setEfeitos(efSucesso, efFalha);

                listaEnigmas.addToRear(novoEnigma);
            }

        } catch (Exception e) {
            System.out.println("Erro crítico ao ler enigmas: " + e.getMessage());
        }
        
        return listaEnigmas;
    }
}