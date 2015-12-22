package com.opentrain.app.view;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opentrain.app.R;
import com.opentrain.app.adapter.StationsCardViewAdapter;
import com.opentrain.app.controller.Action;
import com.opentrain.app.controller.MainController;
import com.opentrain.app.controller.NewWifiScanResultAction;
import com.opentrain.app.controller.TripMatcher;
import com.opentrain.app.controller.UpdateBssidMapAction;
import com.opentrain.app.model.BssidMap;
import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.MatchedStation;
import com.opentrain.app.model.Settings;
import com.opentrain.app.model.Station;
import com.opentrain.app.model.StationBasicInfo;
import com.opentrain.app.model.Trip;
import com.opentrain.app.model.WifiScanResult;
import com.opentrain.app.network.NetowrkManager;
import com.opentrain.app.service.ScannerService;
import com.opentrain.app.service.ServiceBroadcastReceiver;
import com.opentrain.app.service.WifiScanner;
import com.opentrain.app.testing.MockWifiScanner;
import com.opentrain.app.utils.Logger;
import com.opentrain.app.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements StationsCardViewAdapter.OnItemClickListener {

    private ScannerService mBoundService;
    private ServiceBroadcastReceiver mReceiver;
    private StationsCardViewAdapter mAdapter;
    private RecyclerView mRecycleView;

    private Menu menu;
    ProgressBar progressBarScannig, progressBarSyncSever;

    private boolean mIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBarScannig = (ProgressBar) findViewById(R.id.progressBarScannig);
        progressBarScannig.setVisibility(View.INVISIBLE);
        progressBarSyncSever = (ProgressBar) findViewById(R.id.progressBarSyncServer);
        progressBarSyncSever.setVisibility(View.INVISIBLE);

        mRecycleView = (RecyclerView)findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(layoutManager);
        mAdapter = new StationsCardViewAdapter(this);
        mRecycleView.setAdapter(mAdapter);

        mReceiver = new ServiceBroadcastReceiver(this);

        startService(getServiceIntent());
        doBindService();
    }

    public void onItemClick(View itemView, int position) {
        onStationItemClick(position);
    }

    protected Intent getServiceIntent() {
        return new Intent(this, ScannerService.class);
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((ScannerService.LocalBinder) service).getService();
            // Start scaning for wifi stations when the service is up:
            onTrackingClick();
            updateConnectionState();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            updateConnectionState();
        }
    };

    void doBindService() {
        bindService(getServiceIntent(), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mReceiver.register();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mReceiver.unregister();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBoundService != null) {
            mBoundService.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBoundService != null) {
            mBoundService.onResume();
        }
        updateConnectionState();
        updateAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_start_scanning) {
            onTrackingClick();
            return true;
        } else if (id == R.id.action_clear) {
            clearList();
            return true;
        } else if (id == R.id.action_load_from_server) {
            getMapFromServer();
            return true;
        } else if (id == R.id.action_edit_server) {
            editServer();
            return true;
        } else if (id == R.id.action_set_ssid_search_name) {
            onSetSSIDNameClick();
            return true;
        } else if (id == R.id.action_view_logs) {
            onViewLogsClick();
            return true;
        } else if (id == R.id.action_test_trip) {
            onTestClick();
            return true;
        } else if (id == R.id.action_send_logs_by_email) {
            sendLogByEmail();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateScanMenuTitle(String menuText) {
        if (menu != null) {
            MenuItem scanning = menu.findItem(R.id.action_start_scanning);
            scanning.setTitle(menuText);
        }
    }

    private void onViewLogsClick() {
        startActivity(new Intent(this, LogActivity.class));
    }

    private void editServer() {
        onStationItemClick(null);
    }

    private void onStationItemClick(final Integer stationNum) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Edit Station:");

        View view = this.getLayoutInflater().inflate(R.layout.edit_station_name, null);


        final Spinner spinner = (Spinner) view.findViewById(R.id.stations_spinner);
        List<StationBasicInfo> list = MainModel.getInstance().getStationList();
        final Station station = (stationNum != null) ? MainModel.getInstance().getScannedStationList().get(stationNum) : null;

        ArrayAdapter<StationBasicInfo> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        final EditText stationRouters = (EditText) view.findViewById(R.id.editText_station_routers);
        stationRouters.setEnabled(station == null);
        stationRouters.setText(station != null ? station.getBSSIDs() : null);

        alert.setView(view);

        alert.setPositiveButton("Edit server", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                StationBasicInfo stationInfo = (StationBasicInfo) spinner.getAdapter().getItem(spinner.getSelectedItemPosition());
                if (stationInfo != null) {
                    if (station == null) {
                        String bssids = stationRouters.getText().toString();
                        Set<String> bssidsSet = new HashSet<>();
                        bssidsSet.add(bssids);
                        Station newStation = new Station(bssidsSet, System.currentTimeMillis());
                        addStationBssidToServer(newStation, stationInfo);
                    } else {
                        addStationBssidToServer(station, stationInfo);
                    }
                }
            }
        });

        alert.setNegativeButton("Cancel", null);

        alert.show();
    }

    private void onSetSSIDNameClick() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Change SSID search name");

        View view = this.getLayoutInflater().inflate(R.layout.dialog_layout, null);

        final EditText stationRouters = (EditText) view.findViewById(R.id.editText_station_routers);
        stationRouters.setVisibility(View.GONE);
        TextView routers = (TextView) view.findViewById(R.id.textView2);
        routers.setVisibility(View.INVISIBLE);

        TextView current = (TextView) view.findViewById(R.id.stationName);
        current.setText("Current Search String: " + Settings.stationSSID);

        // Set an EditText view to get user input
        final EditText input = (EditText) view.findViewById(R.id.editText_station_name);

        alert.setView(view);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                if (!value.isEmpty()) {
                    Settings.stationSSID = value;
                }
            }
        });

        alert.show();
    }

    public void onTrackingClick() {
        if (mBoundService == null) {
            return;
        }
        if (mBoundService.isScanning()) {
            stopScanning();
        } else {
            startScanning();
        }
    }

    public void sendLogByEmail() {
        toast("Share logs with email");

        Intent email = new Intent(Intent.ACTION_SEND);
        // prompts email clients only
        email.setType("message/rfc822");
        email.putExtra(Intent.EXTRA_EMAIL, new String[] {"open.train.application@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "OpenTrainApp - Log");
        email.putExtra(Intent.EXTRA_TEXT, "OpenTrainApp Log Files attached");
        // Get the actions history in JSON format from main model:
        JSONObject historyJson = MainModel.getInstance().historyToJson();
        email.putExtra(Intent.EXTRA_TEXT, historyJson.toString());

        try {
            // the user can choose the email client
            startActivity(email);
        } catch (android.content.ActivityNotFoundException ex) {
            toast("No email client installed.");
        }
    }

    public void clearList() {
        MainModel.getInstance().clearScannedItems();
        onScanResult();
    }

    public void getMapFromServer() {
        onRequestStart();
        NetowrkManager.getInstance().getMapFromServer(new NetowrkManager.RequestListener() {
            @Override
            public void onResponse(Object response) {
                toast("Succes!");
                onRequestDone();
            }

            @Override
            public void onError() {
                toast("Fail to get map from server");
                onRequestDone();

            }
        });
    }

    public void addStationBssidToServer(Station station, StationBasicInfo stationInfo) {
        onRequestStart();
        // Update all the bssids of this station:
        List<JSONObject> jsonList = new ArrayList<>();
        for (String bssid : station.bssids) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", stationInfo.name);
                jsonObject.put("bssid", bssid);
                jsonObject.put("stop_id", stationInfo.id);
                addMapToServer(jsonObject);
            } catch (JSONException e) {
                Logger.log("addStationBssidToServer" + e.toString());
            }
            jsonList.add(jsonObject);
        }
    }

    public void addMapToServer(JSONObject jsonObject/*Station station, String stationId*/) {
        NetowrkManager.getInstance().addMappingToServer(jsonObject, new NetowrkManager.RequestListener() {
            @Override
            public void onResponse(Object response) {
                toast("Succes!");
                // TODO: get updated map from server!
                NetowrkManager.getInstance().getMapFromServer(null);
                onRequestDone();
            }

            @Override
            public void onError() {
                toast("Fail to edit server");
                onRequestDone();
            }
        });
    }

    public void onRequestDone() {
        progressBarSyncSever.setVisibility(View.INVISIBLE);
    }

    public void onRequestStart() {
        progressBarSyncSever.setVisibility(View.VISIBLE);
    }

    public void updateConnectionState() {
        if (mBoundService != null && mBoundService.isScanning()) {
            onStartScanning();
        } else {
            onStopScanning();
        }
    }

    private void startScanning() {
        if (mBoundService != null && !mBoundService.isScanning()) {
            mBoundService.startScannig();
        }
    }

    private void stopScanning() {
        if (mBoundService != null && mBoundService.isScanning()) {
            mBoundService.stopScanning();
        }
    }

    public void onStartScanning() {
        updateScanMenuTitle(getString(R.string.action_stop_scanning));
    }

    public void onStopScanning() {
        updateScanMenuTitle(getString(R.string.action_start_scanning));
        onStopScan();
    }

    public void onStopScan() {
        progressBarScannig.setVisibility(View.INVISIBLE);
    }

    public void onStartScan() {
        progressBarScannig.setVisibility(View.VISIBLE);
    }

    public void onScanResult() {
        Logger.log("Run matchTrip()");
        Trip matchedTrip = TripMatcher.matchTrip();
        MainModel.getInstance().setMatchedTrip(matchedTrip);
        updateAdapter();
    }

    private void updateAdapter() {
        Trip trip = MainModel.getInstance().getMatchedTrip();
        List<MatchedStation> matched = MainModel.getInstance().alignScannedTripToGtfsTrip(
                MainModel.getInstance().getScannedStationList(), trip);
        mAdapter.setItems(matched);
    }

    private void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    private List<Action> getHardCodedTestActions() {
        List<Action> actions = new ArrayList<>();

        BssidMap mockBssidMap = new BssidMap();
        mockBssidMap.put("1", "37332"); // Rehovot
        mockBssidMap.put("2", "37338"); // Lod
        mockBssidMap.put("3", "37292"); // Tel Aviv Hahagana
        mockBssidMap.put("4", "37350");
        mockBssidMap.put("5", "37358");
        mockBssidMap.put("6", "37360");
        mockBssidMap.put("7", "37318");
        mockBssidMap.put("8", "37330"); // Beer Yaakov
        actions.add(new UpdateBssidMapAction(mockBssidMap));

        long baseTimeUnixMs = 1449643135000L;
        try { // get the date of today
            baseTimeUnixMs = TimeUtils.getFormattedTime("08:38:55");
        } catch (Exception e) {
            Logger.log(e.toString());
        }
        final long second = 1000;
        actions.add(new NewWifiScanResultAction(new WifiScanResult(baseTimeUnixMs, "1", "S-ISRAEL-RAILWAYS")));
        actions.add(new NewWifiScanResultAction(new WifiScanResult(baseTimeUnixMs + second * 10, "1", "S-ISRAEL-RAILWAYS")));
        actions.add(new NewWifiScanResultAction(new WifiScanResult(baseTimeUnixMs + second * 20)));
        actions.add(new NewWifiScanResultAction(new WifiScanResult(baseTimeUnixMs + second * 300, "8", "S-ISRAEL-RAILWAYS")));
        actions.add(new NewWifiScanResultAction(new WifiScanResult(baseTimeUnixMs + second * 310)));
        actions.add(new NewWifiScanResultAction(new WifiScanResult(baseTimeUnixMs + second * 675, "2", "S-ISRAEL-RAILWAYS")));
        actions.add(new NewWifiScanResultAction(new WifiScanResult(baseTimeUnixMs + second * 690)));
        actions.add(new NewWifiScanResultAction(new WifiScanResult(baseTimeUnixMs + second * 1375, "3", "S-ISRAEL-RAILWAYS")));
        actions.add(new NewWifiScanResultAction(new WifiScanResult(baseTimeUnixMs + second * 1400)));

        return actions;
    }

    private List<Action> getFileActionHistory() {
        AssetManager assetManager = getApplicationContext().getAssets();
        try {
            InputStream inputStream = assetManager.open("action_history.json");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while(line != null) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }
            String jsonString = stringBuilder.toString();
            JSONObject json = new JSONObject(jsonString);
            return MainModel.historyFromJson(json);
        } catch (Exception exception) {
            Logger.log("Action history file failed to load.");
            return null;
        }
    }

    protected void onTestClick() {
        // We can load a recorded ride into the test.
        // To do it:
        // 1.copy the recorded json into action_history.json
        // 2. Use the following code and comment the currect actions:
        //List<Action> actions = getFileActionHistory();
        List<Action> actions = getHardCodedTestActions();

        // Save current state and replace with mock state
        final BssidMap prevBssidMap = MainModel.getInstance().getBssidMap();
        final WifiScanner prevWifiScanner = mBoundService.getWifiScanner();
        mBoundService.setWifiScanner(new MockWifiScanner(this, actions));

        // TODO: Save this state as well and replace with mock state.
        MainModel.getInstance().clearScannedItems();
        stopScanning();
        Logger.clearItems();
        Settings.setTestSettings();
        MockWifiScanner.mockWifiScanListener = new MockWifiScanner.MockWifiScanListener() {
            @Override
            public void onScanDone() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast("Test trip done!");
                        // Stop test trip and return to normal
                        stopScanning();
                        mBoundService.setWifiScanner(prevWifiScanner);
                        MainController.execute(new UpdateBssidMapAction(prevBssidMap));
                        Settings.setDefaultettings();
                    }
                });
            }
        };

        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startScanning();
            }
        }, 10);
    }
}
