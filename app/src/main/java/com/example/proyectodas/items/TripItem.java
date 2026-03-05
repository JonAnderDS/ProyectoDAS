package com.example.proyectodas.items;

public class TripItem {

    // 1. Añadimos el ID para la persistencia en base de datos
    private long id;

    // 2. Cambiamos la visibilidad a private (Encapsulamiento)
    private String title;
    private String place;
    private long startDateTimestamp; // Fecha de ida
    private long endDateTimestamp;   // Fecha de vuelta
    private String tripType;

    // Constructor con ID (para leer de la base de datos)
    public TripItem(long id, String title, String place, long startDateTimestamp, long endDateTimestamp, String tripType) {
        this.id = id;
        this.title = title;
        this.place = place;
        this.startDateTimestamp = startDateTimestamp;
        this.endDateTimestamp = endDateTimestamp;
        this.tripType = tripType;
    }

    // Constructor sin ID (para crear uno nuevo)
    public TripItem(String title, String place, long startDateTimestamp, long endDateTimestamp, String tripType) {
        this.title = title;
        this.place = place;
        this.startDateTimestamp = startDateTimestamp;
        this.endDateTimestamp = endDateTimestamp;
        this.tripType = tripType;
    }

    // Getters y Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public String getPlace() { return place; }
    public long getStartDateTimestamp() { return startDateTimestamp; }
    public long getEndDateTimestamp() { return endDateTimestamp; }
    public String getTripType() { return tripType; }

}