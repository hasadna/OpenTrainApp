package il.org.hasadna.opentrain.client.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import il.org.hasadna.opentrain.R;
import il.org.hasadna.opentrain.service.DateTimeUtils;
import il.org.hasadna.opentrain.service.ScannerService;

/**
 * Created by android on 30/08/2014.
 */
public class MainFragment extends Fragment {

    private TextView textViewLastOnTrain, textViewLastreport, textViewReportsSent, textViewStationNameKey, textViewStationNameValue;
    private Button scanningBtn;
    private TextView status;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        textViewLastOnTrain = (TextView) view.findViewById(R.id.last_train);
        textViewLastreport = (TextView) view.findViewById(R.id.last_upload_time);
        textViewReportsSent = (TextView) view.findViewById(R.id.reports_sent);
        textViewStationNameKey = (TextView) view.findViewById(R.id.station_name_key);
        textViewStationNameValue = (TextView) view.findViewById(R.id.station_name_value);
        scanningBtn = (Button) view.findViewById(R.id.toggle_scanning);
        status = (TextView) view.findViewById(R.id.status_text);
        return view;
    }

    public void updateUI(ScannerService mConnectionRemote) {

        if (mConnectionRemote == null) {
            return;
        }
        if(status==null){
            return;
        }

        boolean scanning = mConnectionRemote.isScanning();
        long lastOnTrain = mConnectionRemote.lastOnTrain();
        long lastReport = mConnectionRemote.lastReport();
        int reportsSent = mConnectionRemote.reportsSent();
        int reportsPending = mConnectionRemote.reportsPending();
        String lastStationName = mConnectionRemote.lastStationName();
        boolean isStationIndication = mConnectionRemote.isStationIndication();

        String lastTrainIndicationTimeString = (lastOnTrain > 0) ? DateTimeUtils
                .formatTimeForLocale(lastOnTrain) : "--";
        String lastUploadTimeString = (lastReport > 0) ? DateTimeUtils
                .formatTimeForLocale(lastReport) : "--";
        String reportsSentString = String.valueOf(reportsSent);
        String reportsPendingString = String.valueOf(reportsPending);
        String lastStationNameString = lastStationName != null && lastStationName.length() > 0 ? lastStationName : "--";

        if (scanning) {
            status.setText(R.string.status_on);
            scanningBtn.setBackgroundResource(R.drawable.switch_on);
        } else {
            status.setText(R.string.status_off);
            scanningBtn.setBackgroundResource(R.drawable.switch_off);
        }

        textViewLastOnTrain.setText(lastTrainIndicationTimeString);
        textViewLastreport.setText(lastUploadTimeString);
        textViewReportsSent.setText(reportsSentString);
        textViewStationNameValue.setText(lastStationNameString);

        if (isStationIndication) {
            textViewStationNameKey.setText(R.string.station_name);
        } else {
            textViewStationNameKey.setText(R.string.last_station_name);
        }
    }

}
