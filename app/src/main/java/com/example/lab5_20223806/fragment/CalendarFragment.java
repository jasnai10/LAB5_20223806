package com.example.lab5_20223806.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_20223806.R;
import com.example.lab5_20223806.adapter.CalendarDayAdapter;
import com.example.lab5_20223806.adapter.EventAdapter;
import com.example.lab5_20223806.model.Event;
import com.example.lab5_20223806.storage.EventStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment implements CalendarDayAdapter.OnDayClickListener {

    private TextView tvMonthYear;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private RecyclerView rvCalendar;
    private RecyclerView rvCalendarEvents;
    private TextView tvSelectedDateLabel;

    private CalendarDayAdapter calendarAdapter;
    private EventAdapter eventAdapter;
    private EventStorage storage;

    private Calendar currentMonth;
    private int selectedDay = -1;
    private int selectedMonth = -1;
    private int selectedYear = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storage = new EventStorage(requireContext());
        currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);

        Calendar today = Calendar.getInstance();
        selectedDay = today.get(Calendar.DAY_OF_MONTH);
        selectedMonth = today.get(Calendar.MONTH);
        selectedYear = today.get(Calendar.YEAR);

        tvMonthYear = view.findViewById(R.id.tv_month_year);
        btnPrev = view.findViewById(R.id.btn_prev_month);
        btnNext = view.findViewById(R.id.btn_next_month);
        rvCalendar = view.findViewById(R.id.rv_calendar);
        rvCalendarEvents = view.findViewById(R.id.rv_calendar_events);
        tvSelectedDateLabel = view.findViewById(R.id.tv_selected_date_label);

        calendarAdapter = new CalendarDayAdapter(new ArrayList<>(), currentMonth.get(Calendar.MONTH), currentMonth.get(Calendar.YEAR), this);
        rvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        rvCalendar.setAdapter(calendarAdapter);
        rvCalendar.setNestedScrollingEnabled(false);

        eventAdapter = new EventAdapter(null, false);
        rvCalendarEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCalendarEvents.setAdapter(eventAdapter);
        rvCalendarEvents.setNestedScrollingEnabled(false);

        btnPrev.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            selectedDay = -1;
            rebuildCalendar();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            selectedDay = -1;
            rebuildCalendar();
        });

        rebuildCalendar();
    }

    @Override
    public void onResume() {
        super.onResume();
        rebuildCalendar();
    }

    private void rebuildCalendar() {
        List<Event> allEvents = storage.getEvents();
        int month = currentMonth.get(Calendar.MONTH);
        int year = currentMonth.get(Calendar.YEAR);

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "PE"));
        tvMonthYear.setText(capitalize(sdf.format(currentMonth.getTime())));

        Calendar today = Calendar.getInstance();

        Calendar firstDayOfMonth = Calendar.getInstance();
        firstDayOfMonth.set(year, month, 1, 0, 0, 0);
        firstDayOfMonth.set(Calendar.MILLISECOND, 0);
        int firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK);
        int leadingEmpty = firstDayOfWeek - Calendar.SUNDAY;
        if (leadingEmpty < 0) leadingEmpty = 0;

        int daysInMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<CalendarDayAdapter.CalendarDay> days = new ArrayList<>();
        for (int i = 0; i < leadingEmpty; i++) {
            days.add(new CalendarDayAdapter.CalendarDay(0, false, false, false, false));
        }

        for (int d = 1; d <= daysInMonth; d++) {
            boolean isToday = (today.get(Calendar.YEAR) == year
                    && today.get(Calendar.MONTH) == month
                    && today.get(Calendar.DAY_OF_MONTH) == d);
            boolean isSelected = (selectedDay == d && selectedMonth == month && selectedYear == year);

            boolean hasAnnual = false;
            boolean hasOnce = false;
            for (Event event : allEvents) {
                Calendar eCal = Calendar.getInstance();
                eCal.setTimeInMillis(event.getDateTimeMillis());
                if (event.isAnnual()) {
                    if (eCal.get(Calendar.MONTH) == month && eCal.get(Calendar.DAY_OF_MONTH) == d) {
                        hasAnnual = true;
                    }
                } else {
                    if (eCal.get(Calendar.YEAR) == year
                            && eCal.get(Calendar.MONTH) == month
                            && eCal.get(Calendar.DAY_OF_MONTH) == d) {
                        hasOnce = true;
                    }
                }
            }
            days.add(new CalendarDayAdapter.CalendarDay(d, isToday, isSelected, hasAnnual, hasOnce));
        }

        while (days.size() < 42) {
            days.add(new CalendarDayAdapter.CalendarDay(0, false, false, false, false));
        }

        calendarAdapter = new CalendarDayAdapter(days, month, year, this);
        rvCalendar.setAdapter(calendarAdapter);

        if (selectedDay > 0 && selectedMonth == month && selectedYear == year) {
            filterEventsForDay(selectedDay, month, year, allEvents);
        } else {
            filterEventsFromToday(allEvents);
        }
    }

    private void filterEventsFromToday(List<Event> allEvents) {
        tvSelectedDateLabel.setText(getString(R.string.events_from_today));
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayMillis = today.getTimeInMillis();

        List<Event> filtered = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.isAnnual()) {
                Calendar eCal = Calendar.getInstance();
                eCal.setTimeInMillis(event.getDateTimeMillis());
                int eMonth = eCal.get(Calendar.MONTH);
                int eDay = eCal.get(Calendar.DAY_OF_MONTH);
                Calendar nextOcc = Calendar.getInstance();
                nextOcc.set(Calendar.MONTH, eMonth);
                nextOcc.set(Calendar.DAY_OF_MONTH, eDay);
                nextOcc.set(Calendar.HOUR_OF_DAY, 0);
                nextOcc.set(Calendar.MINUTE, 0);
                nextOcc.set(Calendar.SECOND, 0);
                nextOcc.set(Calendar.MILLISECOND, 0);
                if (nextOcc.getTimeInMillis() < todayMillis) {
                    nextOcc.add(Calendar.YEAR, 1);
                }
                filtered.add(event);
            } else {
                Calendar eCal = Calendar.getInstance();
                eCal.setTimeInMillis(event.getDateTimeMillis());
                eCal.set(Calendar.HOUR_OF_DAY, 0);
                eCal.set(Calendar.MINUTE, 0);
                eCal.set(Calendar.SECOND, 0);
                eCal.set(Calendar.MILLISECOND, 0);
                if (eCal.getTimeInMillis() >= todayMillis) {
                    filtered.add(event);
                }
            }
        }
        eventAdapter.setEvents(filtered);
    }

    private void filterEventsForDay(int day, int month, int year, List<Event> allEvents) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar selected = Calendar.getInstance();
        selected.set(year, month, day, 0, 0, 0);
        selected.set(Calendar.MILLISECOND, 0);
        tvSelectedDateLabel.setText(getString(R.string.events_from_date, sdf.format(selected.getTime())));

        List<Event> filtered = new ArrayList<>();
        for (Event event : allEvents) {
            Calendar eCal = Calendar.getInstance();
            eCal.setTimeInMillis(event.getDateTimeMillis());
            if (event.isAnnual()) {
                if (eCal.get(Calendar.MONTH) == month && eCal.get(Calendar.DAY_OF_MONTH) == day) {
                    filtered.add(event);
                }
            } else {
                if (eCal.get(Calendar.YEAR) == year
                        && eCal.get(Calendar.MONTH) == month
                        && eCal.get(Calendar.DAY_OF_MONTH) == day) {
                    filtered.add(event);
                }
            }
        }
        eventAdapter.setEvents(filtered);
    }

    @Override
    public void onDayClick(int day, int month, int year) {
        selectedDay = day;
        selectedMonth = month;
        selectedYear = year;
        rebuildCalendar();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
