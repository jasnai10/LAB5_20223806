package com.example.lab5_20223806.model;

import java.io.Serializable;
import java.util.UUID;

public class Event implements Serializable {
    public static final String PERIODICITY_ONCE = "ONCE";
    public static final String PERIODICITY_ANNUAL = "ANNUAL";
    public static final int NOTIFICATION_SAME_DAY = 0;
    public static final int NOTIFICATION_1_DAY = 1;
    public static final int NOTIFICATION_3_DAYS = 3;
    public static final int NOTIFICATION_1_WEEK = 7;

    private String id;
    private String name;
    private long dateTimeMillis;
    private boolean hasTime;
    private String periodicity;
    private int notificationAdvanceDays;

    public Event() {
        this.id = UUID.randomUUID().toString();
        this.periodicity = PERIODICITY_ONCE;
        this.notificationAdvanceDays = NOTIFICATION_1_DAY;
        this.hasTime = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateTimeMillis() {
        return dateTimeMillis;
    }

    public void setDateTimeMillis(long dateTimeMillis) {
        this.dateTimeMillis = dateTimeMillis;
    }

    public boolean isHasTime() {
        return hasTime;
    }

    public void setHasTime(boolean hasTime) {
        this.hasTime = hasTime;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public int getNotificationAdvanceDays() {
        return notificationAdvanceDays;
    }

    public void setNotificationAdvanceDays(int notificationAdvanceDays) {
        this.notificationAdvanceDays = notificationAdvanceDays;
    }

    public boolean isAnnual() {
        return PERIODICITY_ANNUAL.equals(periodicity);
    }
}
