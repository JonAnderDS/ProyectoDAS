package com.example.proyectodas.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.proyectodas.activities.MainActivity;
import com.example.proyectodas.R;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "viajes_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.d("MI_ALARMA", "¡El Receiver ha despertado! Intentando mostrar notificación...");
        String titulo = intent.getStringExtra("titulo");
        String mensaje = intent.getStringExtra("mensaje");
        int notificationId = intent.getIntExtra("id", 0);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 1. Crear el canal (Obligatorio en Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios de Viaje",
                    NotificationManager.IMPORTANCE_HIGH // HIGH para que salga popup emergente
            );
            notificationManager.createNotificationChannel(channel);
        }

        // 2. Intent para abrir la app al tocar la notificación
        Intent tapIntent = new Intent(context, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3. Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // <-- CAMBIA ESTO POR UN ICONO TUYO DE VIAJE
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje)) // Por si el texto es largo
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // 4. Mostrar
        notificationManager.notify(notificationId, builder.build());
    }
}