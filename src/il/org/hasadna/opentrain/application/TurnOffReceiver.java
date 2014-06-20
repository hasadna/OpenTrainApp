package il.org.hasadna.opentrain.application;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import il.org.hasadna.opentrain.client.activity.MainActivity;
import il.org.hasadna.opentrain.service.ScannerService;


/**
 * Test low power in adb with am broadcast -a android.intent.action.BATTERY_LOW
 * Test cancel button in notification list by swiping down on the entry for the
 * stumbler, and [X] Stop Scanning will appear.
 */
public final class TurnOffReceiver extends BroadcastReceiver {
    private static final String LOGTAG = TurnOffReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOGTAG, "onReceive!");

        Intent serviceIntent = new Intent(context, ScannerService.class);
        IBinder binder = peekService(context, serviceIntent);
        if (binder != null) {
            // service is running, tell it to stop
            ScannerService.ScannerBinder serviceBinder = (ScannerService.ScannerBinder) binder;
            ScannerService service = serviceBinder.getService();
            service.stopScanning();
        }

        // In the case where the MainActivity is in the foreground, we need to tell it to update
        context.sendBroadcast(new Intent(MainActivity.ACTION_UPDATE_UI));
    }
}
