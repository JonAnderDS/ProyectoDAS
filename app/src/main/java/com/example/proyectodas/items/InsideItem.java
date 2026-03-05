package com.example.proyectodas.items;

public class InsideItem {

    private long id;
    private long luggageId; // Clave foránea que lo enlaza a la maleta correspondiente
    private String itemName; // Nombre del artículo (Ej: "Camisetas", "Cargador")
    private boolean isPacked; // Estado para el CheckBox (true = guardado, false = no guardado)

    /**
     * Constructor para crear un nuevo artículo desde la interfaz.
     * Todavía no tiene un ID porque no ha sido insertado en la base de datos SQLite.
     */
    public InsideItem(String itemName, long luggageId, boolean isPacked) {
        this.itemName = itemName;
        this.luggageId = luggageId;
        this.isPacked = isPacked;
    }

    /**
     * Constructor para recuperar un artículo existente desde la base de datos.
     * Incluye el ID autoincremental generado por SQLite.
     */
    public InsideItem(long id, String itemName, long luggageId, boolean isPacked) {
        this.id = id;
        this.itemName = itemName;
        this.luggageId = luggageId;
        this.isPacked = isPacked;
    }

    // --- GETTERS Y SETTERS ---

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLuggageId() {
        return luggageId;
    }

    public void setLuggageId(long luggageId) {
        this.luggageId = luggageId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * Nota: Por convención en Java, el getter de un booleano suele llamarse 'isNombredelAtributo'
     * en lugar de 'getNombredelAtributo'.
     */
    public boolean isPacked() {
        return isPacked;
    }

    public void setPacked(boolean packed) {
        isPacked = packed;
    }
}