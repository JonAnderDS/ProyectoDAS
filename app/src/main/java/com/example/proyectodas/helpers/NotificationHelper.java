package com.example.proyectodas.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.example.proyectodas.R;
import com.example.proyectodas.items.TripItem;
import java.util.Calendar;

public class NotificationHelper {

    public static void programarNotificacionesViaje(Context context, TripItem trip) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // ID base único para este viaje. Multiplicamos por 10 para tener hueco para 4 notificaciones
        int baseId = (int) trip.getId() * 10;

        // 1. Un día antes de irse (a las 18:00 de la tarde)
        programarAlarma(context, alarmManager, baseId + 1, trip.getStartDateTimestamp(), -1, 18,
                context.getString(R.string.notif_titulo_pre_ida), context.getString(R.string.notif_msg_pre_ida));

        // 2. Día de ida (a las 08:00 de la mañana)
        programarAlarma(context, alarmManager, baseId + 2, trip.getStartDateTimestamp(), 0, 8,
                context.getString(R.string.notif_titulo_ida), context.getString(R.string.notif_msg_ida));

        // 3. Un día antes de volver (a las 18:00 de la tarde)
        programarAlarma(context, alarmManager, baseId + 3, trip.getEndDateTimestamp(), -1, 18,
                context.getString(R.string.notif_titulo_pre_vuelta), context.getString(R.string.notif_msg_pre_vuelta));

        // 4. Día de vuelta (a las 08:00 de la mañana)
        programarAlarma(context, alarmManager, baseId + 4, trip.getEndDateTimestamp(), 0, 8,
                context.getString(R.string.notif_titulo_vuelta), context.getString(R.string.notif_msg_vuelta));

        Calendar testCal = Calendar.getInstance();
        testCal.add(Calendar.SECOND, 10); // Sonará en 10 segundos

        Context appContext = context.getApplicationContext();

        Intent testIntent = new Intent(appContext, NotificationReceiver.class);
        testIntent.putExtra("id", baseId + 5);
        testIntent.putExtra("titulo", "¡Test de Notificación!");
        testIntent.putExtra("mensaje", "Si estás leyendo esto, tu sistema funciona perfectamente.");

        PendingIntent testPending = PendingIntent.getBroadcast(
                appContext, baseId + 5, testIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, testCal.getTimeInMillis(), testPending);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private static void programarAlarma(Context context, AlarmManager alarmManager, int notifId,
                                        long timestamp, int diasAjuste, int hora, String titulo, String mensaje) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.DAY_OF_YEAR, diasAjuste); // Resta 1 día o deja el mismo
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Si la fecha ya pasó (ej: creó el viaje hoy mismo para hoy), no programamos alarmas pasadas
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("id", notifId);
        intent.putExtra("titulo", titulo);
        intent.putExtra("mensaje", mensaje);

        // FLAG_IMMUTABLE es obligatorio desde API 31
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, notifId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Programar alarma exacta que despierte el dispositivo
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } catch (SecurityException e) {
            // En Android 14+, si el usuario revoca el permiso de alarmas exactas manualmente, caería aquí.
            e.printStackTrace();
        }
    }
}