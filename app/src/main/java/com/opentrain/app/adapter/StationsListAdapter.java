package com.opentrain.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.opentrain.app.R;
import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.Station;
import com.opentrain.app.utils.TimeUtils;

/**
 * Created by noam on 25/05/15.
 */
public class StationsListAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;

    public StationsListAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return MainModel.getInstance().getScannedStationList().size();
    }

    @Override
    public Object getItem(int position) {
        return MainModel.getInstance().getScannedStationList().get(position);
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

            stationViewHolder.stationName = (TextView) convertView.findViewById(R.id.stationName);
            stationViewHolder.EnterTime = (TextView) convertView.findViewById(R.id.EnterTime);
            stationViewHolder.ExitTime = (TextView) convertView.findViewById(R.id.ExitTime);

            convertView.setTag(stationViewHolder);
        }

        Station station = MainModel.getInstance().getScannedStationList().get(position);

        stationViewHolder.stationName.setText(station.getName());
        stationViewHolder.EnterTime.setText(TimeUtils.getFormattedTime(station.enterUnixTimeMs));
        stationViewHolder.ExitTime.setText(station.exitUnixTimeMs != null ?
                TimeUtils.getFormattedTime(station.exitUnixTimeMs) : "");

        return convertView;
    }

    static class StationViewHolder {
        TextView stationName;
        TextView EnterTime;
        TextView ExitTime;
    }
}
