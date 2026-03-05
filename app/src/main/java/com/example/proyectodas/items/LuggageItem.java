package com.example.proyectodas.items;

public class LuggageItem {
    private long id;
    private long tripId; // Clave foránea que lo enlaza al viaje
    private String name; // Ej: "Maleta Grande", "Mochila de mano"

    public LuggageItem(String name, long tripId) {
        this.name = name;
        this.tripId = tripId;
    }

    public LuggageItem(long id, String name, long tripId) {
        this.id = id;
        this.name = name;
        this.tripId = tripId;
    }

    // --- GETTERS Y SETTERS ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getTripId() { return tripId; }
    public void setTripId(long tripId) { this.tripId = tripId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}