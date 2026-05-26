package com.example.lab5_20223806.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_20223806.EventFormActivity;
import com.example.lab5_20223806.R;
import com.example.lab5_20223806.adapter.EventAdapter;
import com.example.lab5_20223806.model.Event;
import com.example.lab5_20223806.notification.NotificationHelper;
import com.example.lab5_20223806.storage.EventStorage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class EventsFragment extends Fragment implements EventAdapter.OnEventListener {

    private RecyclerView rvEvents;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;
    private EventAdapter adapter;
    private EventStorage storage;

    private final ActivityResultLauncher<Intent> formLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    loadEvents();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storage = new EventStorage(requireContext());

        rvEvents = view.findViewById(R.id.rv_events);
        tvEmpty = view.findViewById(R.id.tv_empty);
        fabAdd = view.findViewById(R.id.fab_add);

        adapter = new EventAdapter(this, true);
        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEvents.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EventFormActivity.class);
            formLauncher.launch(intent);
        });

        loadEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    private long getNextOccurrenceMillis(Event event) {
        if (!event.isAnnual()) return event.getDateTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(event.getDateTimeMillis());
        Calendar next = Calendar.getInstance();
        next.set(Calendar.MONTH, c.get(Calendar.MONTH));
        next.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
        next.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
        next.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() < System.currentTimeMillis()) {
            next.add(Calendar.YEAR, 1);
        }
        return next.getTimeInMillis();
    }

    private void loadEvents() {
        List<Event> events = storage.getEvents();
        Collections.sort(events, (a, b) -> Long.compare(getNextOccurrenceMillis(a), getNextOccurrenceMillis(b)));
        adapter.setEvents(events);
        if (events.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvEvents.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditEvent(Event event) {
        Intent intent = new Intent(requireContext(), EventFormActivity.class);
        intent.putExtra(EventFormActivity.EXTRA_EVENT_ID, event.getId());
        formLauncher.launch(intent);
    }

    @Override
    public void onDeleteEvent(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_confirm_title))
                .setMessage(getString(R.string.delete_confirm_message))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    NotificationHelper.cancelNotification(requireContext(), event);
                    storage.deleteEvent(event.getId());
                    loadEvents();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}
