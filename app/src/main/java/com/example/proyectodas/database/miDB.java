package com.example.proyectodas.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.proyectodas.items.InsideItem;
import com.example.proyectodas.items.LuggageItem;
import com.example.proyectodas.items.TripItem;

import java.util.ArrayList;
import java.util.List;

public class miDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ViajesDB.db";
    private static final int DATABASE_VERSION = 1;

    public miDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * IMPORTANTE: En Android, las Foreign Keys están desactivadas por defecto en SQLite.
     * Este método las activa para que los borrados en cascada (ON DELETE CASCADE) funcionen.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Crear tabla de Viajes
        String createTripsTable = "CREATE TABLE TRIPS (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "place TEXT, " +
                "start_date INTEGER, " + // Ida
                "end_date INTEGER, " +   // Vuelta
                "type TEXT)";
        db.execSQL(createTripsTable);

        // 2. Crear tabla de Equipajes (Nivel 1)
        String createLuggageTable = "CREATE TABLE LUGGAGE (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "trip_id INTEGER, " +
                "FOREIGN KEY(trip_id) REFERENCES TRIPS(id) ON DELETE CASCADE)";
        db.execSQL(createLuggageTable);

        // 3. Crear tabla de Artículos (Nivel 2)
        String createItemsTable = "CREATE TABLE ITEMS (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "item_name TEXT, " +
                "is_packed INTEGER, " + // SQLite usa 0 (false) y 1 (true)
                "luggage_id INTEGER, " +
                "FOREIGN KEY(luggage_id) REFERENCES LUGGAGE(id) ON DELETE CASCADE)";
        db.execSQL(createItemsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // En un entorno de producción real, aquí haríamos migraciones (ALTER TABLE).
        // Por ahora, simplemente reiniciamos las tablas si cambia la versión.
        db.execSQL("DROP TABLE IF EXISTS ITEMS");
        db.execSQL("DROP TABLE IF EXISTS LUGGAGE");
        db.execSQL("DROP TABLE IF EXISTS TRIPS");
        onCreate(db);
    }

    // ==========================================
    // MÉTODOS PARA LA TABLA TRIPS (VIAJES)
    // ==========================================

    public void addTrip(TripItem trip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", trip.getTitle());
        cv.put("place", trip.getPlace());
        cv.put("start_date", trip.getStartDateTimestamp()); // Guardamos ida
        cv.put("end_date", trip.getEndDateTimestamp());     // Guardamos vuelta
        cv.put("type", trip.getTripType());

        long result = db.insert("TRIPS", null, cv);
        if (result != -1) {
            trip.setId(result);
        }
        db.close();
    }

    public List<TripItem> getAllTrips() {
        List<TripItem> returnList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TRIPS", null);

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String place = cursor.getString(2);
                long startDate = cursor.getLong(3); // Ida
                long endDate = cursor.getLong(4);   // Vuelta
                String type = cursor.getString(5);

                TripItem newTrip = new TripItem(id, title, place, startDate, endDate, type);
                returnList.add(newTrip);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return returnList;
    }

    // ==========================================
    // MÉTODOS PARA LA TABLA LUGGAGE (EQUIPAJE)
    // ==========================================

    public long addLuggage(LuggageItem luggage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", luggage.getName());
        cv.put("trip_id", luggage.getTripId());

        long insertId = db.insert("LUGGAGE", null, cv);
        db.close();
        return insertId; // Devolvemos el ID generado
    }

    public List<LuggageItem> getLuggageForTrip(long tripId) {
        List<LuggageItem> returnList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM LUGGAGE WHERE trip_id = ?", new String[]{String.valueOf(tripId)});

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                // El índice 2 es trip_id, que ya lo conocemos

                LuggageItem item = new LuggageItem(id, name, tripId);
                returnList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return returnList;
    }

    // ==========================================
    // MÉTODOS PARA LA TABLA ITEMS (ARTÍCULOS)
    // ==========================================

    public long addItem(InsideItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("item_name", item.getItemName());
        cv.put("luggage_id", item.getLuggageId());
        cv.put("is_packed", item.isPacked() ? 1 : 0);

        long insertId = db.insert("ITEMS", null, cv);
        db.close();
        return insertId;
    }

    public void updateItemPackedStatus(long itemId, boolean isPacked) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_packed", isPacked ? 1 : 0);

        db.update("ITEMS", cv, "id=?", new String[]{String.valueOf(itemId)});
        db.close();
    }

    public List<InsideItem> getItemsForLuggage(long luggageId) {
        List<InsideItem> returnList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ITEMS WHERE luggage_id = ?", new String[]{String.valueOf(luggageId)});

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                boolean isPacked = cursor.getInt(2) == 1; // 1 es true, 0 es false

                InsideItem item = new InsideItem(id, name, luggageId, isPacked);
                returnList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return returnList;
    }

    // ==========================================
    // MÉTODOS PARA ELIMINAR
    // ==========================================

    public void deleteTrip(long tripId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Al borrar el viaje, SQLite borrará sus maletas y objetos automáticamente gracias al CASCADE
        db.delete("TRIPS", "id=?", new String[]{String.valueOf(tripId)});
        db.close();
    }

    public void deleteLuggage(long luggageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("LUGGAGE", "id=?", new String[]{String.valueOf(luggageId)});
        db.close();
    }

    public void deleteItem(long itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ITEMS", "id=?", new String[]{String.valueOf(itemId)});
        db.close();
    }

    // ==========================================
    // MÉTODOS PARA ACTUALIZAR (EDITAR)
    // ==========================================

    public void updateTrip(TripItem trip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // Preparamos los nuevos datos
        cv.put("title", trip.getTitle());
        cv.put("place", trip.getPlace());
        cv.put("start_date", trip.getStartDateTimestamp());
        cv.put("end_date", trip.getEndDateTimestamp());
        cv.put("type", trip.getTripType());

        // Actualizamos la fila donde el ID coincida
        db.update("TRIPS", cv, "id=?", new String[]{String.valueOf(trip.getId())});
        db.close();
    }
}