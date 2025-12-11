package game;

import enums.CorredorEvento;

/**
 * Represents an event associated with a corridor between two divisions.
 */
public class EventoCorredor {

    /**
     * Type of event
     */
    private CorredorEvento tipo;

    /**
     * Value of intensity on a specified event
     */
    private int valor;

    /**
     * Creates a new corredor event with the specified type and value.
     * @param tipo  the type of corridor event
     * @param valor the numeric value/intensity for this event
     */
    public EventoCorredor(CorredorEvento tipo, int valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    /**
     * Returns the type of this corredor event.
     * @return the event type
     */
    public CorredorEvento getTipo() {
        return tipo;
    }
    /**
     * Returns the value associated with the corredor event.
     * @return the event value
     */
    public int getValor() {
        return valor;
    }

    /**
     * Sets the type of the corredor event.
     * @param tipo the new event type
     */
    public void setTipo(CorredorEvento tipo) {
        this.tipo = tipo;
    }
    /**
     * Sets the value associated with thes corredor event.
     * @param valor the new event value
     */
    public void setValor(int valor) {
        this.valor = valor;
    }
    /**
     * Returns a representation of the corredor event.
     * @return a string representation of the corredor event
     */
    @Override
    public String toString() {
        if (tipo == CorredorEvento.NONE) return "Corredor Seguro";
        return "Evento: " + tipo + " (Valor: " + valor + ")";
    }
}

