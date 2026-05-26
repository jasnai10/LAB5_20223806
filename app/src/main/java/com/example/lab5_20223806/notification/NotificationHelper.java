package com.example.lab5_20223806.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.lab5_20223806.model.Event;

public class NotificationHelper {

    public static final String CHANNEL_ANNUAL_ID = "channel_annual";
    public static final String CHANNEL_ONCE_ID = "channel_once";

    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_NAME = "event_name";
    public static final String EXTRA_EVENT_DATE = "event_date";
    public static final String EXTRA_CHANNEL_ID = "channel_id";

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel annualChannel = new NotificationChannel(
                    CHANNEL_ANNUAL_ID,
                    "Eventos Anuales",
                    NotificationManager.IMPORTANCE_HIGH
            );
            annualChannel.setDescription("Notificaciones para eventos anuales");
            annualChannel.enableVibration(true);
            manager.createNotificationChannel(annualChannel);

            NotificationChannel onceChannel = new NotificationChannel(
                    CHANNEL_ONCE_ID,
                    "Eventos Únicos",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            onceChannel.setDescription("Notificaciones para eventos de una sola vez");
            manager.createNotificationChannel(onceChannel);
        }
    }

    public static void scheduleNotification(Context context, Event event) {
        long notificationTime = event.getDateTimeMillis() - ((long) event.getNotificationAdvanceDays() * 86400000L);
        long currentTime = System.currentTimeMillis();

        if (notificationTime <= currentTime) {
            return;
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_EVENT_ID, event.getId());
        intent.putExtra(EXTRA_EVENT_NAME, event.getName());
        intent.putExtra(EXTRA_EVENT_DATE, event.getDateTimeMillis());
        intent.putExtra(EXTRA_CHANNEL_ID, event.isAnnual() ? CHANNEL_ANNUAL_ID : CHANNEL_ONCE_ID);

        int requestCode = event.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        }
    }

    public static void cancelNotification(Context context, Event event) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        int requestCode = event.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
