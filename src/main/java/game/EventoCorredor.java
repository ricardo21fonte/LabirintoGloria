package game;

import enums.CorredorEvento;

public class EventoCorredor {

    private CorredorEvento tipo;  // tipo do evento (NONE, MOVE_BACK, ...)
    private int valor;            // intensidade (ex: recuar 3 casas, bloquear 2 turnos)

    public EventoCorredor(CorredorEvento tipo, int valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    // GETTERS
    public CorredorEvento getTipo() {
        return tipo;
    }

    public int getValor() {
        return valor;
    }

    @Override
    public String toString() {
        if (tipo == CorredorEvento.NONE) return "Corredor Seguro";
        return "Evento: " + tipo + " (Valor: " + valor + ")";
    }
}

