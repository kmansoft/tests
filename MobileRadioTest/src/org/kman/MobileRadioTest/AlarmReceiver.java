package org.kman.MobileRadioTest;

import java.util.Calendar;

import org.kman.MobileRadioTest.util.MyLog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	private static final String TAG = "AlarmReceiver";

	private static final String ACTION_ALARM_TICK = "org.kman.MobileRadioTest.ALARM_TICK";

	/**
	 * Persistent data definitions
	 */
	private static final String SHARED_PREFS_NAME = "alarm";
	private static final String SHARED_PREFS_NEXT_TIME_KEY = "next";
	private static final String SHARED_PREFS_VERSION_CODE_KEY = "versionCode";

	/**
	 * Alarm period
	 */
	private static final long ALARM_PERIOD = 15 * 60 * 1000;

	/**
	 * Alarm window
	 */
	private static final long ALARM_WINDOW = 15 * 1000;

	@Override
	public void onReceive(Context context, Intent intent) {
		MyLog.i(TAG, "onReceive: %s", intent);

		String action = intent.getAction();

		if (action != null) {
			if (action.equals(ACTION_ALARM_TICK)) {
				/*
				 * Our alarm went off
				 */
				setNextAlarmAlways(context);

				/*
				 * Start the service
				 */
				StartSyncService.submitAccountSync(context);

			} else if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				/*
				 * The user changed the time zone or time, make sure to reschedule the alarm
				 */
				setNextAlarmAlways(context);
			} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
				/*
				 * Boot completed
				 */
				setNextAlarmAlways(context);
			}
		}
	}

	public static void setNextAlarmWithCheck(Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

		final long currentTime = System.currentTimeMillis();
		final long schedule = prefs.getLong(SHARED_PREFS_NEXT_TIME_KEY, 0);
		final int versionCodeOld = prefs.getInt(SHARED_PREFS_VERSION_CODE_KEY, 0);
		final int versionCodeNew = getVersionCode(context);

		if (currentTime > schedule) {
			// The old alarm expired, refresh
			setNextAlarm(context, currentTime, prefs);
		} else if (versionCodeNew != versionCodeOld) {
			// The package has been updated, refresh
			MyLog.i(TAG, "New package version code %d, refreshing the alarm", versionCodeNew);
			setNextAlarm(context, currentTime, prefs);
		} else {
			// None of the above, keep existing alarm
			MyLog.i(TAG, "Kept existing alarm for %1$tF %1$tT", schedule);
		}
	}

	public static void setNextAlarmAlways(Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		final long currentTime = System.currentTimeMillis();

		setNextAlarm(context, currentTime, prefs);
	}

	private static void setNextAlarm(Context context, long currentTime, SharedPreferences prefs) {

		final long scheduled = computeNextAlarmTime(currentTime);
		final int versionCodeNew = getVersionCode(context);

		final Intent intent = new Intent(AlarmReceiver.ACTION_ALARM_TICK);
		intent.setClass(context, AlarmReceiver.class);
		if (Build.VERSION.SDK_INT >= 16) {
			intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		}

		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		am.setWindow(AlarmManager.RTC_WAKEUP, scheduled, ALARM_WINDOW, pendingIntent);

		SharedPreferences sharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putLong(SHARED_PREFS_NEXT_TIME_KEY, scheduled);
		editor.putInt(SHARED_PREFS_VERSION_CODE_KEY, versionCodeNew);
		editor.commit();
	}

	private static long computeNextAlarmTime(long currentTime) {
		final long referenceTime = computeReferenceTime(currentTime);
		final int periods = (int) ((currentTime - referenceTime) / ALARM_PERIOD);
		final long next = referenceTime + periods * ALARM_PERIOD + ALARM_PERIOD;
		return next;
	}

	private static long computeReferenceTime(long currentTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentTime);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		long referenceTime = cal.getTimeInMillis();
		if (referenceTime > currentTime) {
			referenceTime -= 24 * 60 * 60 * 1000;
		}

		return referenceTime;
	}

	private static int getVersionCode(Context context) {
		try {
			final String packageName = context.getPackageName();
			final PackageManager pm = context.getPackageManager();
			final PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
			return pi.versionCode;
		} catch (Exception x) {
			Log.w(TAG, "Error get package meta", x);
		}
		return 0;
	}
}
