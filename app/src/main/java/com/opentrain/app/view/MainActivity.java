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
import java.util.List;

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

        stationsListAdapter = new StationsListAdapter(this);
        listView.setAdapter(stationsListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Station station = (Station) parent.getAdapter().getItem(position);
                onStationItemClick(station, false);
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
        Station station = new Station();
        onStationItemClick(station, true);
    }

    private void onStationItemClick(final Station station, final boolean enableEditBSSIDs) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Edit Station:");

        View view = this.getLayoutInflater().inflate(R.layout.edit_station_name, null);


        final Spinner spinner = (Spinner) view.findViewById(R.id.stations_spinner);
        List<Station> list = MainModel.getInstance().getStationList();

        ArrayAdapter<Station> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        final EditText stationRouters = (EditText) view.findViewById(R.id.editText_station_routers);
        stationRouters.setEnabled(enableEditBSSIDs);
        stationRouters.setText(station.getBSSIDs());

        alert.setView(view);

        alert.setPositiveButton("Edit server", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Station selectedStation = (Station) spinner.getAdapter().getItem(spinner.getSelectedItemPosition());
                String value = selectedStation.stationName;
                if (value.length() > 0) {
                    station.stationName = value;
                    if (enableEditBSSIDs) {
                        String bssids = stationRouters.getText().toString();
                        station.setUnMappedBSSIDs(bssids);
                    }
                    addMapToServer(station);
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

        TextView current = (TextView) view.findViewById(R.id.textView1);
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
        if (mBoundService != null) {
            mBoundService.clearItems();
        }
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

    public void addMapToServer(Station station) {
        onRequestStart();
        NetowrkManager.getInstance().addMappingToServer(station.getPostParam(), new NetowrkManager.RequestListener() {
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
        if (mBoundService != null) {
            stationsListAdapter.setItems(mBoundService.getScanningItems());
        }
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
        mockResult.put("2", "Station 2");
        mockResult.put("3", "Station 3");
        mockResult.put("4", "Station 4");
        MainModel.getInstance().updateMap(mockResult);

        mBoundService.clearItems();
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
