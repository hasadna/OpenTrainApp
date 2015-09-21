package com.opentrain.app.view;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opentrain.app.R;
import com.opentrain.app.adapter.StationsListAdapter;
import com.opentrain.app.model.MainModel;
import com.opentrain.app.model.Settings;
import com.opentrain.app.model.Station;
import com.opentrain.app.network.NetowrkManager;
import com.opentrain.app.service.ScannerService;
import com.opentrain.app.service.ServiceBroadcastReceiver;
import com.opentrain.app.testing.MockWifiScanner;
import com.opentrain.app.utils.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ScannerService mBoundService;
    public StationsListAdapter stationsListAdapter;
    private ServiceBroadcastReceiver mReceiver;

    Button button;
    ListView listView;
    ProgressBar progressBarScannig, progressBarSyncSever;

    private boolean mIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTrackingClick(v);
            }
        });
        progressBarScannig = (ProgressBar) findViewById(R.id.progressBarScannig);
        progressBarScannig.setVisibility(View.INVISIBLE);
        progressBarSyncSever = (ProgressBar) findViewById(R.id.progressBarSyncServer);
        progressBarSyncSever.setVisibility(View.INVISIBLE);
        listView = (ListView) findViewById(R.id.listView);
        listView.setEmptyView(findViewById(android.R.id.empty));

        // Add header to the listView
        View header = getLayoutInflater().inflate(R.layout.station_list_raw, null);
        listView.addHeaderView(header, null, false);

        stationsListAdapter = new StationsListAdapter(this);
        listView.setAdapter(stationsListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Station station = (Station) parent.getAdapter().getItem(position);
                onStationItemClick(station);
            }
        });

        mReceiver = new ServiceBroadcastReceiver(this);

        startService(getServiceIntent());
        doBindService();

    }

    protected Intent getServiceIntent() {
        return new Intent(this, ScannerService.class);
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((ScannerService.LocalBinder) service).getService();
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
        onScanResult();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_clear) {
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void onViewLogsClick() {
        startActivity(new Intent(this, LogActivity.class));
    }

    private void editServer() {
        onStationItemClick(null);
    }
    
    private void onStationItemClick(final Station station) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Edit Station:");

        View view = this.getLayoutInflater().inflate(R.layout.edit_station_name, null);


        final Spinner spinner = (Spinner) view.findViewById(R.id.stations_spinner);
        List<String> list = MainModel.getInstance().getStationList();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        final EditText stationRouters = (EditText) view.findViewById(R.id.editText_station_routers);
        stationRouters.setEnabled(station == null);
        stationRouters.setText(station != null ? station.getBSSIDs() : null);

        alert.setView(view);

        alert.setPositiveButton("Edit server", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String staionId = (String) spinner.getAdapter().getItem(spinner.getSelectedItemPosition());
                if (staionId.length() > 0) {
                    if (station == null) {
                        String bssids = stationRouters.getText().toString();

                        Set<String> bssidsSet = new HashSet<>();
                        bssidsSet.add(bssids);
                        Station newStation = new Station(bssidsSet, System.currentTimeMillis());
                        addMapToServer(newStation, staionId);
                    } else {
                        addMapToServer(station, staionId);
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

    public void onTrackingClick(View view) {
        if (mBoundService == null) {
            return;
        }
        if (mBoundService.isScanning()) {
            stopScanning();
        } else {
            startScanning();
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

    public void addMapToServer(Station station, String stationId) {
        onRequestStart();
        NetowrkManager.getInstance().addMappingToServer(station.getPostParam(stationId), new NetowrkManager.RequestListener() {
            @Override
            public void onResponse(Object response) {
                toast("Succes!");
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
        button.setText("Stop tracking");
    }

    public void onStopScanning() {
        button.setText("Start tracking");
        onStopScan();
    }

    public void onStopScan() {
        progressBarScannig.setVisibility(View.INVISIBLE);
    }

    public void onStartScan() {
        progressBarScannig.setVisibility(View.VISIBLE);
    }

    public void onScanResult() {
        //stationsListAdapter.setItems();
        stationsListAdapter.notifyDataSetChanged();
    }

    private void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    protected void onTestClick() {

        onRequestStart();
        NetowrkManager.getInstance().getTestTripFromServer(new NetowrkManager.RequestListener() {
            @Override
            public void onResponse(Object response) {
                onRequestDone();
                if (mBoundService == null) {
                    Logger.log("cant simulate trip. service is null!!");
                    toast("Cant run test. service is down");
                    return;
                }
                if (MainModel.getInstance().getMockResultsList().size() == 0) {
                    toast("Trip response is empty..!");
                } else {
                    toast("Test trip start");
                    startTestTrip();
                }
            }

            @Override
            public void onError() {
                toast("Fail to get test trip from server");
                onRequestDone();
            }
        });
    }

    private void startTestTrip() {

        HashMap<String, String> mockResult = new HashMap<>();
        mockResult.put("1", "Station 1");
        mockResult.put("2", "St2");
        mockResult.put("3", "Station3Name mane");
        mockResult.put("4", "Station 4 veryvery long name");
        mockResult.put("5", "Station 5");
        mockResult.put("6", "Station 6 long name");
        mockResult.put("7", "Station 7 long name");
        mockResult.put("8", "Station 8");

        // TODO: update the map inside the test (here) and then only use setBssidMap.
        MainModel.getInstance().updateMap(mockResult);

        MainModel.getInstance().clearScannedItems();
        stopScanning();
        mBoundService.setTestWifiScanner();
        Settings.setTestSettings();
        Logger.clearItems();

        MockWifiScanner.mockWifiScanListener = new MockWifiScanner.MockWifiScanListener() {
            @Override
            public void onScanDone() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast("Test trip done!");
                        stopTestTrip();
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
        }, 1000);
    }

    private void stopTestTrip() {
        stopScanning();
        mBoundService.setTrainWifiScanner();
        Settings.setDefaultettings();
    }
}
