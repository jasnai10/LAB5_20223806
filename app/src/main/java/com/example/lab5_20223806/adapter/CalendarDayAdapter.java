package com.example.lab5_20223806.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_20223806.R;

import java.util.ArrayList;
import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(int day, int month, int year);
    }

    public static class CalendarDay {
        public int day;
        public boolean isToday;
        public boolean isSelected;
        public boolean hasAnnualEvent;
        public boolean hasOnceEvent;

        public CalendarDay(int day, boolean isToday, boolean isSelected, boolean hasAnnualEvent, boolean hasOnceEvent) {
            this.day = day;
            this.isToday = isToday;
            this.isSelected = isSelected;
            this.hasAnnualEvent = hasAnnualEvent;
            this.hasOnceEvent = hasOnceEvent;
        }
    }

    private List<CalendarDay> days;
    private final OnDayClickListener listener;
    private final int month;
    private final int year;

    public CalendarDayAdapter(List<CalendarDay> days, int month, int year, OnDayClickListener listener) {
        this.days = days != null ? days : new ArrayList<>();
        this.listener = listener;
        this.month = month;
        this.year = year;
    }

    public void setDays(List<CalendarDay> days) {
        this.days = days != null ? days : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay calDay = days.get(position);
        Context context = holder.itemView.getContext();

        if (calDay.day == 0) {
            holder.tvDay.setText("");
            holder.tvDay.setBackground(null);
            holder.dotsContainer.removeAllViews();
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.tvDay.setText(String.valueOf(calDay.day));

        if (calDay.isToday) {
            holder.tvDay.setBackground(ContextCompat.getDrawable(context, R.drawable.calendar_day_today_bg));
            holder.tvDay.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        } else if (calDay.isSelected) {
            holder.tvDay.setBackground(ContextCompat.getDrawable(context, R.drawable.calendar_day_selected_bg));
            holder.tvDay.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            holder.tvDay.setBackground(null);
            holder.tvDay.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary));
        }

        holder.dotsContainer.removeAllViews();
        if (calDay.hasAnnualEvent) {
            View dot = createDot(context, ContextCompat.getColor(context, R.color.colorAnnual));
            holder.dotsContainer.addView(dot);
        }
        if (calDay.hasOnceEvent) {
            View dot = createDot(context, ContextCompat.getColor(context, R.color.colorOnce));
            holder.dotsContainer.addView(dot);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(calDay.day, month, year);
        });
    }

    private View createDot(Context context, int color) {
        View dot = new View(context);
        int size = (int) (6 * context.getResources().getDisplayMetrics().density);
        int margin = (int) (1 * context.getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, 0, margin, 0);
        dot.setLayoutParams(params);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        dot.setBackground(drawable);
        return dot;
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        LinearLayout dotsContainer;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day);
            dotsContainer = itemView.findViewById(R.id.dots_container);
        }
    }
}
