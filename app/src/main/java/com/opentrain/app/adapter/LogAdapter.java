package com.opentrain.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.opentrain.app.R;
import com.opentrain.app.model.LogItem;
import com.opentrain.app.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by noam on 07/06/15.
 */
public class LogAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    ArrayList<LogItem> logItems = new ArrayList<>();

    public LogAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return logItems != null ? logItems.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return logItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.log_list_raw, null);
        }

        StationViewHolder stationViewHolder = (StationViewHolder) convertView.getTag();

        if (stationViewHolder == null) {
            stationViewHolder = new StationViewHolder();

            stationViewHolder.textView1 = (TextView) convertView.findViewById(R.id.textView1);

            convertView.setTag(stationViewHolder);
        }

        LogItem logItem = logItems.get(position);

        stationViewHolder.textView1.setText("Log: " + position + " (" + TimeUtils.getFormattedTime() + ") " + "\n" + logItem.msg);

        return convertView;
    }

    public void setItems(ArrayList<LogItem> items) {
        if (items == null) {
            return;
        }
        logItems = items;
        notifyDataSetChanged();
    }

    public void add(LogItem logItem) {
        logItems.add(logItem);
        notifyDataSetChanged();
    }

    static class StationViewHolder {
        TextView textView1;
    }
}
