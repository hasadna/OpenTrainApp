package il.org.hasadna.opentrain.client.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.client.adapter.StationAdapter;
import il.org.hasadna.opentrain.service.DataManager;

/**
 * Created by android on 24/10/2014.
 */
public class StationsFragment extends DialogFragment {

    private StationAdapter adapter;

    private DataManager.TaskCallBack taskCallBack = new DataManager.TaskCallBack() {

        @Override
        public void onTaskDone() {
            if (null != adapter) {
                adapter.update();
            }
        }
    };

    private DialogInterface.OnClickListener dialogClicksListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
                if(i==DialogInterface.BUTTON_POSITIVE){
                    if (null != adapter) {
                        DataManager.Stop selected = adapter.getLastSelected();
                        DataManager.getInstance().sendStopToServer(selected);
                    }
                }
        }
    };

    public static StationsFragment newInstance() {
        StationsFragment fragment = new StationsFragment();
        Bundle args = new Bundle();
        args.putInt("title", R.string.app_name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_station, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(title)
                .setPositiveButton(R.string.alert_dialog_ok,
                        dialogClicksListener)
                .setNegativeButton(R.string.alert_dialog_cancel,
                        dialogClicksListener);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        TextView emptyText = (TextView) view.findViewById(R.id.empty);
        listView.setEmptyView(emptyText);
        adapter = new StationAdapter(getActivity());
        listView.setAdapter(adapter);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        DataManager.getInstance().getStopsList(taskCallBack);
    }

    @Override
    public void onStop() {
        super.onStop();
        DataManager.getInstance().unRegisterStationResult();
    }
}
