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

	/**
	 * Alarm period
	 */
	private static final long ALARM_PERIOD = 15 * 60 * 1000;

	/**
	 * Alarm method: use setWindow or if false, use setExact
	 */
	private static final boolean ALARM_USE_SETWINDOW = true;

	/**
	 * Inexact alarm window
	 */
	private static final long ALARM_WINDOW = 30 * 1000;

	/**
	 * Alarm setting methods
	 */
	public static final int ALARM_METHOD_SETWINDOW = 0;
	public static final int ALARM_METHOD_SETEXACT = 1;
	public static final int ALARM_METHOD_SETALARM = 2;

	// DEBUG
	public static final String EXTRA_SET_AT = "setAt";
	public static final String EXTRA_TARGET_TIME = "targetTime";
	public static final String EXTRA_WINDOW = "window";

	@Override
	public void onReceive(Context context, Intent intent) {
		MyLog.i(TAG, "onReceive: %s", intent);

		String action = null;
		if (intent != null) {
			action = intent.getAction();

			Bundle extras = intent.getExtras();
			if (extras != null) {
				final StringBuilder sbExtras = new StringBuilder();
				final Set<String> keySet = extras.keySet();
				for (String key : keySet) {
					if (sbExtras.length() != 0) {
						sbExtras.append(", ");
					}
					sbExtras.append(key).append(" = ").append(extras.get(key));
					if (key.equals(EXTRA_SET_AT) || key.equals(EXTRA_TARGET_TIME)
							|| key.equals("android.intent.extra.ALARM_TARGET_TIME")) {
						sbExtras.append(String.format(" %1$tF %1$tT.%1$tL", extras.getLong(key)));
					}
				}
				if (sbExtras.length() == 0) {
					sbExtras.append("[no extras]");
				}
				MyLog.i(TAG, "onReceive extras: %s", sbExtras);

				final long targetTime = extras.getLong(EXTRA_TARGET_TIME);
				final long now = System.currentTimeMillis();
				if (action != null && action.equals(ACTION_ALARM_TICK)) {
					/*
					 * This is our alarm action
					 */
					if (targetTime != 0 && now < targetTime - 1000) {
						MyLog.i(TAG, "onReceive ***** fired too early *****");

						// This seems fine, but in practice, on the Note 3, causes alarms to fire
						// more than once (!!!)
						//
						// final Intent newIntent = new Intent(ACTION_ALARM_TICK);
						// intent.setClass(context, AlarmReceiver.class);
						// if (Build.VERSION.SDK_INT >= 16) {
						// intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
						// }
						//
						// intent.putExtra(EXTRA_SET_AT, now);
						// intent.putExtra(EXTRA_TARGET_TIME, targetTime);
						//
						// if (ALARM_USE_SETWINDOW) {
						// intent.putExtra(EXTRA_WINDOW, ALARM_WINDOW);
						// }
						//
						// final PendingIntent newPendingIntent =
						// PendingIntent.getBroadcast(context, 0, newIntent,
						// PendingIntent.FLAG_UPDATE_CURRENT);
						//
						// AlarmManager am = (AlarmManager)
						// context.getSystemService(Context.ALARM_SERVICE);
						//
						// if (ALARM_USE_SETWINDOW) {
						// am.setWindow(AlarmManager.RTC_WAKEUP, targetTime, ALARM_WINDOW,
						// newPendingIntent);
						// MyLog.i(TAG, "onReceive set again: setWindow for %1$tF %1$tT for %2$s",
						// targetTime,
						// newPendingIntent);
						// } else {
						// am.setExact(AlarmManager.RTC_WAKEUP, targetTime, newPendingIntent);
						// MyLog.i(TAG, "onReceive set again: setExact for %1$tF %1$tT for %2$s",
						// targetTime,
						// newPendingIntent);
						// }
						//
						// return;
					}

					if (targetTime != 0 && now >= targetTime - 1000) {
						MyLog.i(TAG, "onReceive ##### fired on or after #####");
					}
				}
			}
		}

		if (action != null) {
			if (action.startsWith(ACTION_ALARM_TICK)) {
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

	public static void setNextAlarmAlways(Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		final long currentTime = System.currentTimeMillis();

		setNextAlarm(context, currentTime, prefs);
	}

	private static void setNextAlarm(Context context, long currentTime, SharedPreferences prefs) {

		/*
		 * This helped somewhat, but not quite
		 */

		// Try to cancel any existing pending intents
		// final Intent cancelIntent = new Intent(AlarmReceiver.ACTION_ALARM_TICK);
		// cancelIntent.setClass(context, AlarmReceiver.class);
		// if (Build.VERSION.SDK_INT >= 16) {
		// cancelIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		// }
		// final PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, 0,
		// cancelIntent,
		// PendingIntent.FLAG_NO_CREATE);
		// if (cancelPendingIntent != null) {
		// MyLog.i(TAG, "Canceling existing PendingIntent: %s", cancelPendingIntent);
		// cancelPendingIntent.cancel();
		// }

		final long scheduled = computeNextAlarmTime(currentTime);
		final int versionCodeNew = PackageUtils.getVersionCode(context);

		final Intent intent = new Intent(AlarmReceiver.ACTION_ALARM_TICK);
		intent.setClass(context, AlarmReceiver.class);
		if (Build.VERSION.SDK_INT >= 16) {
			intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		}

		intent.putExtra(EXTRA_SET_AT, currentTime);
		intent.putExtra(EXTRA_TARGET_TIME, scheduled);

		if (ALARM_USE_SETWINDOW) {
			intent.putExtra(EXTRA_WINDOW, ALARM_WINDOW);
		}

		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		if (ALARM_USE_SETWINDOW) {
			am.setWindow(AlarmManager.RTC_WAKEUP, scheduled, ALARM_WINDOW, pendingIntent);
			MyLog.i(TAG, "Set next alarm: setWindow for %1$tF %1$tT for %2$s", scheduled, pendingIntent);
		} else {
			am.setExact(AlarmManager.RTC_WAKEUP, scheduled, pendingIntent);
			MyLog.i(TAG, "Set next alarm: setExact for %1$tF %1$tT for %2$s", scheduled, pendingIntent);
		}

		SharedPreferences sharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putLong(SHARED_PREFS_NEXT_TIME_KEY, scheduled);
		editor.putInt(SHARED_PREFS_VERSION_CODE_KEY, versionCodeNew);
		editor.commit();
	}

	/*
	 * This did not help
	 */

	// private static Intent createBroadcastIntent(String action, long time) {
	// final Calendar cal = Calendar.getInstance();
	// cal.setTimeInMillis(time);
	// final String s = String.format("%s_%02d_%02d_%02d_%d", action, cal.get(Calendar.HOUR_OF_DAY),
	// cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), time);
	// return new Intent(s);
	// }

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
