package com.opentrain.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.opentrain.app.R;
import com.opentrain.app.model.Station;

import java.util.ArrayList;

/**
 * Created by noam on 25/05/15.
 */
public class StationsListAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    ArrayList<Station> stationsListItems = new ArrayList<>();

    public StationsListAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return stationsListItems != null ? stationsListItems.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return stationsListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.station_list_raw, null);
        }

        StationViewHolder stationViewHolder = (StationViewHolder) convertView.getTag();

        if (stationViewHolder == null) {
            stationViewHolder = new StationViewHolder();

            stationViewHolder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
            stationViewHolder.textView2 = (TextView) convertView.findViewById(R.id.textView2);
            stationViewHolder.textView3 = (TextView) convertView.findViewById(R.id.textView3);

            convertView.setTag(stationViewHolder);
        }

        Station station = stationsListItems.get(position);

        stationViewHolder.textView1.setText("Station name: " + station.stationName);
        stationViewHolder.textView2.setText(station.arriveStr != null ? "Enter time : " + station.arriveStr : "");
        stationViewHolder.textView3.setText(station.departureStr != null ? "Exit time :" + station.departureStr : "");

        return convertView;
    }

    public void setItems(ArrayList<Station> items) {
        if (items == null) {
            return;
        }
        stationsListItems = items;
        notifyDataSetChanged();
    }

    static class StationViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;
    }
}
