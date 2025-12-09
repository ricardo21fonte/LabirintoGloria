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
                // Se não houver lista, devolve vazio sem estourar
                return listaEnigmas;
            }

            for (Object o : enigmasArray) {
                JSONObject enigmaJSON = (JSONObject) o;

                // 1. Ler dados essenciais
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
                    continue; // Ignora enigmas com dificuldade inválida
                }

                // 2. Criar o Enigma
                Enigma novoEnigma = new Enigma(pergunta, opcoes, correta, dif);

                // 3. LÓGICA MERGED: Definir efeitos consoante a Dificuldade
                // (Ignora JSON e usa regras de jogo fixas, como no código do teu amigo)
                String efSucesso;
                String efFalha;

                switch (dif) {
                    case FACIL:
                        // Sucesso: avança 1 casa | Falha: recua 1 casa
                        efSucesso = "EXTRA_TURN"; // Ou "BONUS_MOVE:1" se o teu engine suportar
                        efFalha   = "BACK:1";
                        break;

                    case MEDIO:
                        // Sucesso: avança 2 casas | Falha: recua 2 casas
                        efSucesso = "EXTRA_TURN"; // Adapta para "BONUS_MOVE:2" se quiseres
                        efFalha   = "BACK:2";
                        break;

                    case DIFICIL:
                        // Sucesso: ganha turno extra | Falha: bloqueado
                        efSucesso = "EXTRA_TURN";
                        efFalha   = "BLOCK";
                        break;

                    default:
                        efSucesso = "NONE";
                        efFalha   = "NONE";
                        break;
                }

                // Aplica os efeitos calculados
                novoEnigma.setEfeitos(efSucesso, efFalha);

                listaEnigmas.addToRear(novoEnigma);
            }

        } catch (Exception e) {
            // Em caso de erro, avisa na consola (útil para debug)
            System.out.println("Erro crítico ao ler enigmas: " + e.getMessage());
        }
        
        return listaEnigmas;
    }
}