package game;

import enums.TipoDivisao;

/**
 * Represents a single room (or division) in the labyrinth.
 */
public class Divisao implements Comparable<Divisao> { // <--- 1. ADICIONADO implements Comparable

    /**
     * Global counter used to assign unique IDs to each new Divisao.
     */
    private static int nextId = 1;

    /**
     * Unique ID of this specific Divisao.
     */
    private int id;

    /**
     * Name of the room.
     */
    private String nome;

    /**
     * Type of this room, which describes its function in the game.
     */
    private TipoDivisao tipo;
    /**
     * Lever associated with this room's puzzle, if any.
     */
    private Alavanca alavancaDoPuzzle;

    /**
     * ID of the door that this room can unlock via its puzzle/lever.
     */
    private int idDaPortaQueAbre = -1;

    /**
     * Creates a new Divisao
     * @param nome the room name
     * @param tipo the type of the room
     */
    public Divisao(String nome, TipoDivisao tipo) {
        this.id = nextId++;
        this.nome = nome;
        this.tipo = tipo;
    }

    /**
     * Updates the global ID counter used to generate IDs for new instances.
     * @param valor the new value to be used as the next ID to generate
     */
    public static void setNextId(int valor) {
        nextId = valor;
    }

    /**
     * Forces this Divisao to use a specific ID, when loading from a file or external representation.
     * @param id the ID read from the external source
     */
    public void definirIdManual(int id) {
        this.id = id;
    }

    /**
     * Returns the unique ID of this room.
     * @return the room ID
     */
    public int getId() { return id; }

    /**
     * Associates a lever puzzle with this room.
     * @param a the lever to set
     */
    public void setAlavanca(Alavanca a) {
        this.alavancaDoPuzzle = a;
    }

    /**
     * Returns the lever associated with this room
     * @return the lever puzzle
     */
    public Alavanca getAlavanca() { return alavancaDoPuzzle; }

    /**
     * Sets the ID of the door that this room can unlock
     * @param id the ID of the door that can be unlocked from this room
     */
    public void setIdDesbloqueio(int id) { this.idDaPortaQueAbre = id; }

    /**
     * Returns the ID of the door that this room can unlock.
     * @return the door ID to unlock
     */
    public int getIdDesbloqueio() { return idDaPortaQueAbre; }

    /**
     * Returns the display name of the room.
     * @return the room name
     */
    public String getNome() { return nome; }

    /**
     * Returns the type of this room.
     * @return the TipoDivisao of the room
     */
    public TipoDivisao getTipo() { return tipo; }

    /**
     * Returns a string representation of this room
     * @return a string representation of this room
     */
    @Override
    public String toString() {
        return nome + " [" + tipo + "] (ID:" + id + ")";
    }

    /**
     * Compares this room to another object for equality.
     * @param obj the object to compare with
     * @return true if both represent the same room ID, or false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Divisao outra = (Divisao) obj;
        return this.id == outra.id;
    }

    /**
     * Compares this Divisao with another based on their IDs.
     * @param outra the other Divisao to compare with.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Divisao outra) {
        return Integer.compare(this.id, outra.id);
    }
}