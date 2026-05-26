package com.example.lab5_20223806.util;

import com.example.lab5_20223806.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String formatDate(long millis, boolean hasTime) {
        SimpleDateFormat sdf;
        if (hasTime) {
            sdf = new SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.getDefault());
        } else {
            sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        }
        return sdf.format(new Date(millis));
    }

    public static String getRemainingText(Event event) {
        long now = System.currentTimeMillis();
        long eventMillis = event.getDateTimeMillis();

        if (event.isAnnual()) {
            Calendar eventCal = Calendar.getInstance();
            eventCal.setTimeInMillis(eventMillis);
            int eventMonth = eventCal.get(Calendar.MONTH);
            int eventDay = eventCal.get(Calendar.DAY_OF_MONTH);

            Calendar nextOccurrence = Calendar.getInstance();
            nextOccurrence.set(Calendar.MONTH, eventMonth);
            nextOccurrence.set(Calendar.DAY_OF_MONTH, eventDay);
            nextOccurrence.set(Calendar.HOUR_OF_DAY, eventCal.get(Calendar.HOUR_OF_DAY));
            nextOccurrence.set(Calendar.MINUTE, eventCal.get(Calendar.MINUTE));
            nextOccurrence.set(Calendar.SECOND, 0);
            nextOccurrence.set(Calendar.MILLISECOND, 0);

            if (nextOccurrence.getTimeInMillis() < now) {
                nextOccurrence.add(Calendar.YEAR, 1);
            }
            eventMillis = nextOccurrence.getTimeInMillis();
        }

        long diff = eventMillis - now;

        if (diff < 0) {
            if (event.isAnnual()) {
                return "Evento anual";
            }
            return "Evento pasado";
        }

        long diffDays = diff / (1000 * 60 * 60 * 24);
        long diffHours = diff / (1000 * 60 * 60);

        if (diffDays == 0) {
            if (diffHours == 0) {
                return "Hoy";
            }
            return diffHours + " horas restantes";
        } else if (diffDays == 1) {
            return "Mañana";
        } else {
            return diffDays + " días restantes";
        }
    }

    public static boolean isSameDay(long millis1, long millis2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(millis1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(millis2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
