package org.kman.KitKatAlarmTest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KeepAliveService extends Service {

	/*
	 * Disabled
	 * 
	 * private static final String TAG = "KeepAliveService";
	 * 
	 * private static final int NOTIFICATION_ID = 0x00000002; private static final String
	 * EXTRA_PENDING_INTENT = "pendingIntent";
	 * 
	 * public static class Info { private static final String EXTRA_MESSAGE = "message";
	 * 
	 * public Info(CharSequence message) { mMessage = message; }
	 * 
	 * public Info(Context context, int resId, String arg) { mMessage = context.getString(resId,
	 * arg); }
	 * 
	 * CharSequence mMessage;
	 * 
	 * void pack(Intent intent) { intent.putExtra(EXTRA_MESSAGE, mMessage); }
	 * 
	 * static Info unpack(Intent intent) { CharSequence message =
	 * intent.getStringExtra(EXTRA_MESSAGE); if (!TextUtils.isEmpty(message)) { final Info info =
	 * new Info(message); return info; } return null; } }
	 * 
	 * public static class Facade {
	 * 
	 * public static void start(Context context, Info info, PendingIntent pendingIntent) {
	 * 
	 * // MyLog.i(TAG, "Facade:start %s", info.mMessage);
	 * 
	 * checkStatics(context);
	 * 
	 * // Start foreground service Intent intent = new Intent(context, KeepAliveService.class);
	 * info.pack(intent); intent.putExtra(EXTRA_PENDING_INTENT, pendingIntent);
	 * context.startService(intent); }
	 * 
	 * public static void stop(Context context) { checkStatics(context);
	 * 
	 * Intent intent = new Intent(context, KeepAliveService.class); context.stopService(intent);
	 * 
	 * // MyLog.i(TAG, "gNotificationManager.cancel"); gNotificationManager.cancel(NOTIFICATION_ID);
	 * }
	 * 
	 * private static void checkStatics(Context context) { synchronized (Facade.class) { if
	 * (gSharedPrefs == null) { Context appContext = context.getApplicationContext();
	 * 
	 * gSharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext);
	 * gNotificationManager = (NotificationManager) appContext
	 * .getSystemService(Context.NOTIFICATION_SERVICE); } } }
	 * 
	 * private static SharedPreferences gSharedPrefs; private static NotificationManager
	 * gNotificationManager; }
	 * 
	 * @Override public void onCreate() { super.onCreate(); }
	 * 
	 * @SuppressWarnings("deprecation")
	 * 
	 * @Override public int onStartCommand(Intent intent, int flags, int startId) { if (intent !=
	 * null) { Info info = Info.unpack(intent); PendingIntent pendingIntent =
	 * intent.getParcelableExtra(EXTRA_PENDING_INTENT);
	 * 
	 * if (info != null && pendingIntent != null) { // Show a notification // MyLog.i(TAG,
	 * "onStartCommand msg = %s", info.mMessage);
	 * 
	 * final long when = System.currentTimeMillis(); Notification notification = null;
	 * 
	 * final HcNotificationBuilder builder = HcNotificationBuilder.create(this,
	 * R.drawable.ic_launcher, null, when); if (builder != null) {
	 * builder.setLatestEventInfo(getString(R.string.app_name), info.mMessage, pendingIntent);
	 * builder.setOngoing(true); notification = builder.getNotification(); }
	 * 
	 * if (notification == null) { notification = new Notification(R.drawable.ic_launcher, null,
	 * when); notification.flags |= Notification.FLAG_ONGOING_EVENT;
	 * notification.setLatestEventInfo(this, getString(R.string.app_name), info.mMessage,
	 * pendingIntent); }
	 * 
	 * startForeground(NOTIFICATION_ID, notification); } else { // No message, remove the
	 * notification // MyLog.i(TAG, "onStartCommand, no msg, calling stopForeground");
	 * stopForeground(true); } } else { // No intent, we got restarted after getting kicked out of
	 * memory MyLog.i(TAG, "onStartCommand with null intent"); }
	 * 
	 * return START_STICKY; }
	 * 
	 * @Override public void onDestroy() { super.onDestroy(); stopForeground(true); }
	 */

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
