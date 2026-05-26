package com.example.lab5_20223806.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.lab5_20223806.model.Event;
import com.example.lab5_20223806.storage.EventStorage;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            EventStorage storage = new EventStorage(context);
            List<Event> events = storage.getEvents();
            long now = System.currentTimeMillis();
            for (Event event : events) {
                long notificationTime = event.getDateTimeMillis() - ((long) event.getNotificationAdvanceDays() * 86400000L);
                if (notificationTime > now) {
                    NotificationHelper.scheduleNotification(context, event);
                }
            }
        }
    }
}
