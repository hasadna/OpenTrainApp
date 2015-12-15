package com.opentrain.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.opentrain.app.R;
import com.opentrain.app.model.MatchedStation;
import com.opentrain.app.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elina on 20/10/2015.
 */
public class StationsCardViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String UNKNOWN_STATION = "לא בלו\"ז";

    // The stations displayed
    private List<MatchedStation> stationsList;
    // Define listener member variable
    private final OnItemClickListener listener;
    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public StationsCardViewAdapter(OnItemClickListener listener) {
        this.stationsList = new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return stationsList.size() > 0 ? stationsList.size() : 1;
    }

    // When no station is displayed - an empty viewHolder is displayed.
    private static final int VIEW_TYPE_EMPTY_LIST_PLACEHOLDER = 10;

    @Override
    public int getItemViewType(int position) {
        if (stationsList.size() == 0) {
            return VIEW_TYPE_EMPTY_LIST_PLACEHOLDER;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof StationViewHolder) {
            StationViewHolder stationViewHolder = (StationViewHolder) viewHolder;
            MatchedStation station = stationsList.get(i);
            stationViewHolder.stationName.setText(station.scannedStation.getName());
            stationViewHolder.scanEnterTime.setText(TimeUtils.getFormattedTime(station.scannedStation.enterUnixTimeMs));
            stationViewHolder.scanExitTime.setText(station.scannedStation.exitUnixTimeMs != null ?
                    TimeUtils.getFormattedTime(station.scannedStation.exitUnixTimeMs) : "");
            stationViewHolder.gtfsEnterTime.setText(station.stop != null ? TimeUtils.getFormattedTime(station.stop.arrival) : UNKNOWN_STATION);
            stationViewHolder.gtfsExitTime.setText(station.stop != null ? TimeUtils.getFormattedTime(station.stop.departure) : UNKNOWN_STATION);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView;

        if (viewType == VIEW_TYPE_EMPTY_LIST_PLACEHOLDER) {
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.empty_layout, viewGroup, false);
            return new EmptyViewHolder(itemView);
        }

        itemView =
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);
        return new StationViewHolder(itemView);
    }

    public void add(int position, MatchedStation item) {
        stationsList.add(position, item);
        notifyItemInserted(position);
    }

    public void setItems(List<MatchedStation> items) {
        if (items == null) {
            return;
        }
        stationsList = items;
        notifyDataSetChanged();
    }

    public void remove(String item) {
        int position = stationsList.indexOf(item);
        stationsList.remove(position);
        notifyItemRemoved(position);
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View convertView) {
            super (convertView);
        }
    }

    public class StationViewHolder extends RecyclerView.ViewHolder {

        protected TextView stationName;
        protected TextView scanEnterTime;
        protected TextView scanExitTime;
        protected TextView gtfsEnterTime;
        protected TextView gtfsExitTime;

        public StationViewHolder(View convertView) {
            super(convertView);
            stationName =  (TextView) convertView.findViewById(R.id.txtName);
            scanEnterTime = (TextView) convertView.findViewById(R.id.scannedEnterTime);
            scanExitTime = (TextView) convertView.findViewById(R.id.scannedExitTime);
            gtfsEnterTime = (TextView) convertView.findViewById(R.id.gtfsEnterTime);
            gtfsExitTime = (TextView) convertView.findViewById(R.id.gtfsExitTime);
            // Setup the click listener
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null)
                        listener.onItemClick(itemView, getLayoutPosition());
                }
            });
        }
    }

}
