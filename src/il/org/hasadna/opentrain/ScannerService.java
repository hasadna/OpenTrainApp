package il.org.hasadna.opentrain;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;

public final class ScannerService extends Service {
	public static final String MESSAGE_TOPIC = "il.org.hasadna.opentrain.serviceMessage";
	public static final String ACTION_CLOSE = "il.org.hasadna.opentrain.serviceMessage.close";

	private static final String LOGTAG = ScannerService.class.getName();
	private static final int NOTIFICATION_ID = 1;
	private static final int WAKE_TIMEOUT = 5 * 1000;

	private Scanner mScanner;
	private Reporter mReporter;
	private LooperThread mLooper;
	private PendingIntent mWakeIntent;
	private BroadcastReceiver mBatteryLowReceiver;
	private BroadcastReceiver mBatteryOkayReceiver;

	private BroadcastReceiver mCloseAppReceiver;
	
	private final ScannerServiceInterface.Stub mBinder = new ScannerServiceInterface.Stub() {
		@Override
		public boolean isScanning() throws RemoteException {
			Log.d(LOGTAG, "ScannerServiceInterface.Stub.isScanning:");
			return ((null==mScanner)?false:mScanner.isScanning());
		}

		@Override
		public void startScanning() throws RemoteException {
			Log.d(LOGTAG, "ScannerServiceInterface.Stub.startScanning:");
			ScannerService.this.startScanning();
		}

		@Override
		public void stopScanning() throws RemoteException {
			Log.d(LOGTAG, "ScannerServiceInterface.Stub.stopScanning:");
			ScannerService.this.stopScanning();
		}
	
		@Override
		public int getLocationCount() throws RemoteException {
			Log.d(LOGTAG, "ScannerServiceInterface.Stub.getLocationCount:");
			return ((null==mScanner)?0:mScanner.getLocationCount());
		}

		@Override
		public int getAPCount() throws RemoteException {
			Log.d(LOGTAG, "ScannerServiceInterface.Stub.getAPCount:");
			return ((null==mScanner)?0:mScanner.getAPCount());
		}
	};

	private final class LooperThread extends Thread {
		private Handler mHandler;

		@Override
		public void run() {
			Looper.prepare();
			mHandler = new Handler();
			Looper.loop();
		}

		void post(Runnable runnable) {
			if (mHandler != null) {
				mHandler.post(runnable);
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(LOGTAG, "onCreate");

		mBatteryLowReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(LOGTAG, "BatteryLowReceiver.onReceive: Battery low broadcast received");
				ScannerService.this.suspendScanningOnBatteryLow();
			}
		};
		registerReceiver(mBatteryLowReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));

		mBatteryOkayReceiver= new BroadcastReceiver(){
			@Override
			public void onReceive(Context context,Intent intent){
				Log.d(LOGTAG,"BatteryOkayReciever.onReceive: Battery back to normal broadcast received");
				ScannerService.this.resumeScanningOnBatteryOkay();
			}
		};
		registerReceiver(mBatteryOkayReceiver, new IntentFilter(Intent.ACTION_BATTERY_OKAY));

		mCloseAppReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				try {
					if (mBinder.isScanning()) {
						mBinder.stopScanning();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		};
		registerReceiver(mCloseAppReceiver, new IntentFilter(ACTION_CLOSE));

		mReporter = new Reporter(this);
		mScanner = new Scanner(this);
		mLooper = new LooperThread();
		mLooper.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(LOGTAG, "onDestroy");

		unregisterReceiver(mBatteryLowReceiver);
		mBatteryLowReceiver=null;
		
		unregisterReceiver(mBatteryOkayReceiver);
		mBatteryOkayReceiver = null;
		
		unregisterReceiver(mCloseAppReceiver);
		mCloseAppReceiver=null;


		mLooper.interrupt();
		mLooper = null;
		
		mScanner.stopScanning();
		mScanner = null;

		mReporter.shutdown();
		mReporter = null;

		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);
	}
	
	private void startScanning(){
		Log.d(LOGTAG, "startScanning:");

		if (mScanner.isScanning()) {
			return;
		}

		mLooper.post(new Runnable() {
			@Override
			public void run() {
				try {
					Log.d(LOGTAG, "startScanning: run");

					String title = getResources().getString(
							R.string.service_name);
					String text = getResources().getString(
							R.string.service_scanning);
					postNotification(title, text,
							Notification.FLAG_NO_CLEAR
									| Notification.FLAG_ONGOING_EVENT);

					mScanner.startScanning();
					mReporter.triggerUpload(); 

					// keep us awake.
					Context cxt = getApplicationContext();
					Calendar cal = Calendar.getInstance();
					Intent intent = new Intent(cxt, ScannerService.class);
					mWakeIntent = PendingIntent.getService(cxt, 0, intent,
							0);
					AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					alarm.setRepeating(AlarmManager.RTC_WAKEUP,
							cal.getTimeInMillis(), WAKE_TIMEOUT,
							mWakeIntent);
				} catch (Exception e) {
					Log.d(LOGTAG, "startScanning: Looper shut itself : " + e);
				}
			}
		});
	}
	
	private void stopScanning(){
		Log.d(LOGTAG, "stopScanning:");

		if (!mScanner.isScanning()) {
			return;
		}

		mLooper.post(new Runnable() {
			@Override
			public void run() {
				Log.d(LOGTAG, "stopScanning: run");

				AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				alarm.cancel(mWakeIntent);

				NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				nm.cancel(NOTIFICATION_ID);
				stopForeground(true);

				mScanner.stopScanning();

				mReporter.triggerUpload();
			}
		});
	}
	
	private void suspendScanningOnBatteryLow() {
		Log.d(LOGTAG, "suspendScanningOnBatteryLow:");

		if (!mScanner.isScanning()) {
			return;
		}
		mLooper.post(new Runnable() {
			@Override
			public void run() {
				Log.d(LOGTAG, "suspendScanningOnBatteryLow: run");

				mScanner.stopScanning();
				mReporter.triggerUpload();
				
				String title = getResources().getString(
						R.string.service_name);
				String text = getResources().getString(
						R.string.battery_low_message);
				String ticker = getResources().getString(
						R.string.battery_low_ticker);
				postNotification(title, text,ticker,
						Notification.FLAG_AUTO_CANCEL);
			
			}
		});
	}
		
	
	
	public void resumeScanningOnBatteryOkay() {
		Log.d(LOGTAG, "resumeScanningOnBatteryOkay:");

		if (mScanner.isScanning()) {
			return;
		}

		mLooper.post(new Runnable() {
			@Override
			public void run() {
				Log.d(LOGTAG, "resumeScanningOnBatteryOkay: run");
				mScanner.startScanning();
				mReporter.triggerUpload(); 
				
				String title = getResources().getString(
						R.string.service_name);
				String text = getResources().getString(
						R.string.service_scanning);
				String ticker = getResources().getString(
						R.string.battery_okay_ticker);
				postNotification(title, text,ticker,
						Notification.FLAG_AUTO_CANCEL);
			}
		});
		
	}

	private void postNotification(final String title, final String text, final int flags) {
		Log.d(LOGTAG, "postNotification:");
		postNotification( title, text,text,flags);
	}
	
	private void postNotification(final String title, final String text, final String ticker,
			final int flags) {
		Log.d(LOGTAG, "postNotification:");

		mLooper.post(new Runnable() {
			@Override
			public void run() {
				Log.d(LOGTAG, "postNotification: run");

				Context ctx = getApplicationContext();
				Intent notificationIntent = new Intent(ctx, MainActivity.class);
				notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_FROM_BACKGROUND);

				PendingIntent contentIntent = PendingIntent.getActivity(ctx,
						NOTIFICATION_ID, notificationIntent,
						PendingIntent.FLAG_CANCEL_CURRENT);

				int icon = R.drawable.ic_status_scanning;
				Notification n = buildNotification(ctx, icon, title, text, ticker,
						contentIntent, flags);
				startForeground(NOTIFICATION_ID, n);
			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// keep running!
		Log.d(LOGTAG, "onStartCommand:");
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(LOGTAG, "onBind");
		return mBinder;
	}

	// new version of notification creation.
	// replace deprecated version and add close action to it.
	private static Notification buildNotification(Context context, int icon,
			String contentTitle, String contentText, String tickerText,
			PendingIntent contentIntent, int flags) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context);
		builder.setContentTitle(contentTitle);
		builder.setContentText(contentText);
		builder.setContentIntent(contentIntent);
		builder.setTicker(tickerText);
		builder.setSmallIcon(icon);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
		Intent closeIntent = new Intent(ACTION_CLOSE);
		PendingIntent pendingCloseIntent = PendingIntent.getBroadcast(context,
				0, closeIntent, 0);
		builder.addAction(android.R.drawable.ic_menu_close_clear_cancel,
				context.getString(R.string.notification_close), pendingCloseIntent);

		Notification n = builder.build();
		n.flags |= flags;
		return n;
	}
}
