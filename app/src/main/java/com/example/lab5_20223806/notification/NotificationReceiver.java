package com.example.lab5_20223806.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.lab5_20223806.MainActivity;
import com.example.lab5_20223806.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventId = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_ID);
        String eventName = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_NAME);
        long eventDate = intent.getLongExtra(NotificationHelper.EXTRA_EVENT_DATE, 0);
        String channelId = intent.getStringExtra(NotificationHelper.EXTRA_CHANNEL_ID);

        if (eventName == null || channelId == null) return;

        String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(eventDate));
        String contentText = "Fecha: " + dateStr;

        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                eventId != null ? eventId.hashCode() : 0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(eventName)
                .setContentText(contentText)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationHelper.CHANNEL_ANNUAL_ID.equals(channelId)
                        ? NotificationCompat.PRIORITY_HIGH
                        : NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            int notificationId = eventId != null ? eventId.hashCode() : (int) System.currentTimeMillis();
            manager.notify(notificationId, builder.build());
        }
    }
}
