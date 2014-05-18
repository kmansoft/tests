package org.kman.KitKatAlarmTest;

import java.util.Calendar;
import java.util.Set;

import org.kman.tests.utils.MyLog;
import org.kman.tests.utils.PackageUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver {

	private static final String TAG = "AlarmReceiver";

	private static final String ACTION_ALARM_TICK = "org.kman.KitKatAlarmTest.ALARM_TICK";

	/**
	 * Persistent data definitions
	 */
	private static final String SHARED_PREFS_NAME = "alarm";
	private static final String SHARED_PREFS_NEXT_TIME_KEY = "next";
	private static final String SHARED_PREFS_VERSION_CODE_KEY = "versionCode";
	private static final String SHARED_PREFS_METHOD = "method";

	/**
	 * Alarm period
	 */
	private static final long ALARM_PERIOD = 15 * 60 * 1000;

	/**
	 * Inexact alarm window
	 */
	private static final int ALARM_WINDOW = 30 * 1000;

	/**
	 * Alarm setting methods
	 */
	public static final int ALARM_METHOD_SETWINDOW = 0;
	public static final int ALARM_METHOD_SETEXACT = 1;
	public static final int ALARM_METHOD_SETALARM = 2;

	@Override
	public void onReceive(Context context, Intent intent) {
		MyLog.i(TAG, "onReceive: %s", intent);

		StringBuilder sbExtras = null;
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				sbExtras = new StringBuilder();
				final Set<String> keySet = extras.keySet();
				for (String key : keySet) {
					if (sbExtras.length() != 0) {
						sbExtras.append(", ");
					}
					sbExtras.append(key).append(" = ").append(extras.get(key));
				}
				if (sbExtras.length() == 0) {
					sbExtras.append("[no extras]");
				}
				MyLog.i(TAG, "onReceive extras: %s", sbExtras);
			}
		}

		final String action = intent.getAction();
		if (action != null) {
			if (action.equals(ACTION_ALARM_TICK)) {
				/*
				 * Our alarm went off
				 */
				setNextAlarmAlways(context, true);

				/*
				 * Start the service
				 */
				StartSyncService.submitAccountSync(context);

			} else if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				/*
				 * The user changed the time zone or time, make sure to reschedule the alarm
				 */
				setNextAlarmAlways(context, false);
			}
		}
	}

	public static void setNextAlarmWithCheck(Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

		final long currentTime = System.currentTimeMillis();
		final long schedule = prefs.getLong(SHARED_PREFS_NEXT_TIME_KEY, 0);
		final int versionCodeOld = prefs.getInt(SHARED_PREFS_VERSION_CODE_KEY, 0);
		final int versionCodeNew = PackageUtils.getVersionCode(context);

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

	public static void setNextAlarmAlways(Context context, boolean check) {
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

		final long currentTime = System.currentTimeMillis();
		final long scheduled = prefs.getLong(SHARED_PREFS_NEXT_TIME_KEY, 0);

		if (scheduled != 0 && currentTime < scheduled - 1000) {
			MyLog.i(TAG, "***** WRONG ALARM TIME ***** scheduled: %1$tF %1$tT now: %2$tF %2$tT", scheduled, currentTime);
		}

		setNextAlarm(context, currentTime, prefs);
	}

	private static void setNextAlarm(Context context, long currentTime, SharedPreferences prefs) {

		final long scheduled = computeNextAlarmTime(currentTime);
		final int versionCodeNew = PackageUtils.getVersionCode(context);

		final Intent intent = new Intent(AlarmReceiver.ACTION_ALARM_TICK);
		intent.setClass(context, AlarmReceiver.class);
		if (Build.VERSION.SDK_INT >= 16) {
			intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		}

		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.setWindow(AlarmManager.RTC_WAKEUP, scheduled, ALARM_WINDOW, pendingIntent);

		MyLog.i(TAG, "Set next alarm for %1$tF %1$tT for %2$s", scheduled, pendingIntent);

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
}
