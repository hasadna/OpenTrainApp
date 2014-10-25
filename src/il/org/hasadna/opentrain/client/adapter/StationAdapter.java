package il.org.hasadna.opentrain.client.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;

import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.service.DataManager;

/**
 * Created by android on 19/10/2014.
 */
public class StationAdapter extends BaseAdapter {

    private ArrayList<DataManager.Stop> items;
    private LayoutInflater inflater;

    public DataManager.Stop getLastSelected() {
        return lastSelected;
    }

    private DataManager.Stop lastSelected;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            int position = (Integer) compoundButton.getTag();
            DataManager.Stop stop = items.get(position);
            stop.isChecked = b;
            if (b) {
                if (lastSelected != null && lastSelected.gtfsStopId != null) {
                    if (!lastSelected.gtfsStopId.equals(stop.gtfsStopId)) {
                        lastSelected.isChecked = false;
                    }
                }
                lastSelected = stop;
            }
            notifyDataSetChanged();
        }
    };

    public StationAdapter(Activity context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void update() {
        this.items = DataManager.getInstance().getStopsList();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.row_stops, viewGroup, false);
            holder = new ViewHolder();
            holder.checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            holder.checkBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        DataManager.Stop stop = items.get(position);
        holder.checkBox.setText(stop.stopShortName);
        holder.checkBox.setTag(position);
        holder.checkBox.setChecked(stop.isChecked);
        return view;
    }

    public class ViewHolder {
        public CheckBox checkBox;
    }
}
