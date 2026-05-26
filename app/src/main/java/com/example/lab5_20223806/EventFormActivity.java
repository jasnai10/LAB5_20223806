package com.example.lab5_20223806;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab5_20223806.model.Event;
import com.example.lab5_20223806.notification.NotificationHelper;
import com.example.lab5_20223806.storage.EventStorage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventFormActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private TextInputEditText etName;
    private MaterialButton btnDate;
    private MaterialButton btnTime;
    private CheckBox cbHasTime;
    private RadioGroup rgPeriodicity;
    private RadioButton rbOnce;
    private RadioButton rbAnnual;
    private Spinner spinnerNotification;
    private MaterialButton btnSave;

    private Calendar selectedCalendar;
    private int selectedHour = 9;
    private int selectedMinute = 0;
    private boolean timeSelected = false;

    private EventStorage storage;
    private Event currentEvent;
    private boolean isEditMode = false;

    private final int[] notificationValues = {
            Event.NOTIFICATION_SAME_DAY,
            Event.NOTIFICATION_1_DAY,
            Event.NOTIFICATION_3_DAYS,
            Event.NOTIFICATION_1_WEEK
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        storage = new EventStorage(this);

        initViews();
        setupToolbar();
        setupNotificationSpinner();

        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId != null) {
            isEditMode = true;
            currentEvent = storage.getEventById(eventId);
            if (currentEvent != null) {
                populateForm(currentEvent);
            }
        } else {
            currentEvent = new Event();
            selectedCalendar = Calendar.getInstance();
        }

        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        btnDate = findViewById(R.id.btn_date);
        btnTime = findViewById(R.id.btn_time);
        cbHasTime = findViewById(R.id.cb_has_time);
        rgPeriodicity = findViewById(R.id.rg_periodicity);
        rbOnce = findViewById(R.id.rb_once);
        rbAnnual = findViewById(R.id.rb_annual);
        spinnerNotification = findViewById(R.id.spinner_notification);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? getString(R.string.edit_event_title) : getString(R.string.add_event_title));
        }
    }

    private void setupNotificationSpinner() {
        String[] notificationOptions = {
                getString(R.string.notification_same_day),
                getString(R.string.notification_1_day),
                getString(R.string.notification_3_days),
                getString(R.string.notification_1_week)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, notificationOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNotification.setAdapter(adapter);
        spinnerNotification.setSelection(1);
    }

    private void setupListeners() {
        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());

        cbHasTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnTime.setEnabled(isChecked);
            if (!isChecked) {
                timeSelected = false;
                btnTime.setText(getString(R.string.no_time));
            } else if (timeSelected) {
                updateTimeButton();
            }
        });

        btnSave.setOnClickListener(v -> saveEvent());
    }

    private void showDatePicker() {
        long initialSelection = selectedCalendar != null ? selectedCalendar.getTimeInMillis() : MaterialDatePicker.todayInUtcMilliseconds();
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_date))
                .setSelection(initialSelection)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selectedCalendar == null) selectedCalendar = Calendar.getInstance();
            Calendar utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            utcCal.setTimeInMillis(selection);
            selectedCalendar.set(Calendar.YEAR, utcCal.get(Calendar.YEAR));
            selectedCalendar.set(Calendar.MONTH, utcCal.get(Calendar.MONTH));
            selectedCalendar.set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH));
            updateDateButton();
        });

        datePicker.show(getSupportFragmentManager(), "date_picker");
    }

    private void showTimePicker() {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(selectedHour)
                .setMinute(selectedMinute)
                .setTitleText(getString(R.string.select_time))
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            selectedHour = timePicker.getHour();
            selectedMinute = timePicker.getMinute();
            timeSelected = true;
            updateTimeButton();
        });

        timePicker.show(getSupportFragmentManager(), "time_picker");
    }

    private void updateDateButton() {
        if (selectedCalendar != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            btnDate.setText(sdf.format(selectedCalendar.getTime()));
        }
    }

    private void updateTimeButton() {
        int displayHour = selectedHour % 12;
        if (displayHour == 0) displayHour = 12;
        String amPm = selectedHour < 12 ? "AM" : "PM";
        btnTime.setText(String.format(Locale.getDefault(), "%d:%02d %s", displayHour, selectedMinute, amPm));
    }

    private void populateForm(Event event) {
        etName.setText(event.getName());

        selectedCalendar = Calendar.getInstance();
        selectedCalendar.setTimeInMillis(event.getDateTimeMillis());
        updateDateButton();

        if (event.isHasTime()) {
            selectedHour = selectedCalendar.get(Calendar.HOUR_OF_DAY);
            selectedMinute = selectedCalendar.get(Calendar.MINUTE);
            timeSelected = true;
            cbHasTime.setChecked(true);
            updateTimeButton();
        } else {
            cbHasTime.setChecked(false);
            btnTime.setEnabled(false);
            btnTime.setText(getString(R.string.no_time));
        }

        if (event.isAnnual()) {
            rbAnnual.setChecked(true);
        } else {
            rbOnce.setChecked(true);
        }

        int advanceDays = event.getNotificationAdvanceDays();
        for (int i = 0; i < notificationValues.length; i++) {
            if (notificationValues[i] == advanceDays) {
                spinnerNotification.setSelection(i);
                break;
            }
        }
    }

    private void saveEvent() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_name_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCalendar == null) {
            Toast.makeText(this, getString(R.string.error_date_required), Toast.LENGTH_SHORT).show();
            return;
        }

        currentEvent.setName(name);

        boolean hasTime = cbHasTime.isChecked() && timeSelected;
        currentEvent.setHasTime(hasTime);

        if (hasTime) {
            selectedCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            selectedCalendar.set(Calendar.MINUTE, selectedMinute);
            selectedCalendar.set(Calendar.SECOND, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);
        } else {
            selectedCalendar.set(Calendar.HOUR_OF_DAY, 9);
            selectedCalendar.set(Calendar.MINUTE, 0);
            selectedCalendar.set(Calendar.SECOND, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);
        }
        currentEvent.setDateTimeMillis(selectedCalendar.getTimeInMillis());

        int selectedRadioId = rgPeriodicity.getCheckedRadioButtonId();
        if (selectedRadioId == R.id.rb_annual) {
            currentEvent.setPeriodicity(Event.PERIODICITY_ANNUAL);
        } else {
            currentEvent.setPeriodicity(Event.PERIODICITY_ONCE);
        }

        int notificationIndex = spinnerNotification.getSelectedItemPosition();
        if (notificationIndex >= 0 && notificationIndex < notificationValues.length) {
            currentEvent.setNotificationAdvanceDays(notificationValues[notificationIndex]);
        }

        if (isEditMode) {
            NotificationHelper.cancelNotification(this, currentEvent);
            storage.updateEvent(currentEvent);
        } else {
            storage.addEvent(currentEvent);
        }

        NotificationHelper.scheduleNotification(this, currentEvent);

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
