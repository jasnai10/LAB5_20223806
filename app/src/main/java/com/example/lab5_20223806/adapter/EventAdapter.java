package com.example.lab5_20223806.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_20223806.R;
import com.example.lab5_20223806.model.Event;
import com.example.lab5_20223806.util.DateUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventListener {
        void onEditEvent(Event event);
        void onDeleteEvent(Event event);
    }

    private List<Event> events;
    private final OnEventListener listener;
    private final boolean showDeleteButton;

    public EventAdapter(OnEventListener listener, boolean showDeleteButton) {
        this.events = new ArrayList<>();
        this.listener = listener;
        this.showDeleteButton = showDeleteButton;
    }

    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(event.getName());
        holder.tvDate.setText(DateUtils.formatDate(event.getDateTimeMillis(), event.isHasTime()));
        holder.chipRemaining.setText(DateUtils.getRemainingText(event));

        MaterialCardView card = (MaterialCardView) holder.itemView;
        if (event.isAnnual()) {
            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAnnualLight));
            holder.colorBar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAnnual));
            holder.chipPeriodicity.setText(context.getString(R.string.periodicity_annual));
            holder.chipPeriodicity.setChipStrokeColorResource(R.color.colorAnnual);
            holder.chipPeriodicity.setTextColor(ContextCompat.getColor(context, R.color.colorAnnual));
        } else {
            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorOnceLight));
            holder.colorBar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorOnce));
            holder.chipPeriodicity.setText(context.getString(R.string.periodicity_once));
            holder.chipPeriodicity.setChipStrokeColorResource(R.color.colorOnce);
            holder.chipPeriodicity.setTextColor(ContextCompat.getColor(context, R.color.colorOnce));
        }

        if (showDeleteButton) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteEvent(event);
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEditEvent(event);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        View colorBar;
        TextView tvName;
        TextView tvDate;
        Chip chipRemaining;
        Chip chipPeriodicity;
        ImageButton btnDelete;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            colorBar = itemView.findViewById(R.id.color_bar);
            tvName = itemView.findViewById(R.id.tv_event_name);
            tvDate = itemView.findViewById(R.id.tv_event_date);
            chipRemaining = itemView.findViewById(R.id.chip_remaining);
            chipPeriodicity = itemView.findViewById(R.id.chip_periodicity);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
