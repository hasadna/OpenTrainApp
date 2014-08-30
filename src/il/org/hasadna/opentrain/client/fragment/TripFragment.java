package il.org.hasadna.opentrain.client.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.service.DataManager;

/**
 * Created by android on 30/08/2014.
 */
public class TripFragment extends Fragment {

    private TripAdapter tripAdapter;
    private DataManager.TaskCallBack taskCallBack = new DataManager.TaskCallBack() {

        @Override
        public void onTaskDone() {
            updateUI();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = (ListView) getView().findViewById(R.id.listView);

        TextView emptyText = (TextView) getView().findViewById(R.id.empty);
        listView.setEmptyView(emptyText);

        tripAdapter = new TripAdapter(getActivity());
        listView.setAdapter(tripAdapter);
        updateUI();
    }

    public void updateUI() {
        if (null != tripAdapter) {
            tripAdapter.update();
            tripAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        DataManager.getInstance().registerTripResult(taskCallBack);
    }

    @Override
    public void onStop() {
        super.onStop();
        DataManager.getInstance().unRegisterTripResult();
    }

    public class TripAdapter extends BaseAdapter {

        private ArrayList<DataManager.Stop> items;
        private LayoutInflater inflater;

        public TripAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void update() {
            this.items = DataManager.getInstance().getTripStopList();
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

            if (view == null) {
                view = inflater.inflate(R.layout.trip_stop_row, null);
            }

            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder == null) {
                holder = new ViewHolder();
                holder.textView1 = (TextView) view.findViewById(R.id.textView1);
                holder.textView2 = (TextView) view.findViewById(R.id.textView2);
                holder.textView3 = (TextView) view.findViewById(R.id.textView3);
                view.setTag(holder);
            }

            DataManager.Stop stop = items.get(position);
            holder.textView1.setText(stop.stopName);
            holder.textView2.setText(stop.expDeparture);
            holder.textView3.setText(stop.expArrival);
            return view;
        }
    }

    public class ViewHolder {
        public TextView textView1;
        public TextView textView2;
        public TextView textView3;
    }

}
