package com.example.lab5_20223806.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lab5_20223806.model.Event;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EventStorage {
    private static final String PREFS_NAME = "events_prefs";
    private static final String KEY_EVENTS = "events_list";

    private final SharedPreferences prefs;
    private final Gson gson;

    public EventStorage(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<Event> getEvents() {
        String json = prefs.getString(KEY_EVENTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Event>>() {}.getType();
        List<Event> events = gson.fromJson(json, type);
        if (events == null) {
            return new ArrayList<>();
        }
        return events;
    }

    public void saveEvents(List<Event> events) {
        String json = gson.toJson(events);
        prefs.edit().putString(KEY_EVENTS, json).apply();
    }

    public void addEvent(Event event) {
        List<Event> events = getEvents();
        events.add(event);
        saveEvents(events);
    }

    public void updateEvent(Event event) {
        List<Event> events = getEvents();
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(event.getId())) {
                events.set(i, event);
                break;
            }
        }
        saveEvents(events);
    }

    public void deleteEvent(String id) {
        List<Event> events = getEvents();
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(id)) {
                events.remove(i);
                break;
            }
        }
        saveEvents(events);
    }

    public Event getEventById(String id) {
        List<Event> events = getEvents();
        for (Event event : events) {
            if (event.getId().equals(id)) {
                return event;
            }
        }
        return null;
    }
}
