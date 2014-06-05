package org.kman.KitKatAlarmTest;

import org.kman.tests.utils.MyLog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class SetAlarmService extends Service {

	private static String ACTION_SET_ALARM = "org.kman.KitKatAlarmTest.SET_ALARM";

	private static String TAG = "SetAlarmService";

	public static void submitSetNextAlarm(Context context) {
		final LockManager lm = LockManager.get(context);
		lm.acquireSpecialFlag(LockManager.SPECIAL_FLAG_SET_ALARM_SERVICE);

		final Intent intent = new Intent(ACTION_SET_ALARM);
		intent.setClass(context, SetAlarmService.class);
		if (context.startService(intent) == null) {
			lm.releaseSpecialFlag(LockManager.SPECIAL_FLAG_SET_ALARM_SERVICE);
		}
	}

	@Override
	public void onCreate() {
		MyLog.i(TAG, "onCreate");
		super.onCreate();
		mLockManager = LockManager.get(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		MyLog.i(TAG, "onStartCommand: %s", intent);
		AlarmReceiver.setNextAlarmAlways(this);
		mLockManager.releaseSpecialFlag(LockManager.SPECIAL_FLAG_SET_ALARM_SERVICE);
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private LockManager mLockManager;
}
