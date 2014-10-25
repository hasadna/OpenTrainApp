package il.org.hasadna.opentrain.client.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.client.adapter.TripAdapter;
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
        View view = getView();
        if (null != view) {
            ListView listView = (ListView) view.findViewById(R.id.listView);
            TextView emptyText = (TextView) view.findViewById(R.id.empty);
            listView.setEmptyView(emptyText);
            tripAdapter = new TripAdapter(getActivity());
            listView.setAdapter(tripAdapter);
        }
        updateUI();
    }

    public void updateUI() {
        if (null != tripAdapter) {
            tripAdapter.update();
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

}
