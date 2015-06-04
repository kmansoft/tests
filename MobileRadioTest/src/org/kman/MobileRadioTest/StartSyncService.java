package org.kman.MobileRadioTest;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import org.kman.MobileRadioTest.util.MyLog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class StartSyncService extends Service {

	/**
	 * Log tag
	 */
	private static final String TAG = "StartSyncService";

	/**
	 * Thread count
	 */
	private static final int THREAD_COUNT = 5;

	/**
	 * Action to sync accounts in the background, applying all the usual logic
	 */
	public static final String ACTION_SYNC = "org.kman.MobileRadioTest.ACTION_SYNC";

	public static void submitAccountSync(Context context) {
		final LockManager lm = LockManager.get(context);
		lm.acquireSpecialFlag(LockManager.LOCK_FLAG_STARTING_SYNC);

		final Intent serviceIntent = new Intent(ACTION_SYNC);
		serviceIntent.setClass(context, StartSyncService.class);

		if (context.startService(serviceIntent) == null) {
			// Failure
			lm.releaseSpecialFlag(LockManager.LOCK_FLAG_STARTING_SYNC);
		}
	}

	@Override
	public void onCreate() {
		MyLog.i(TAG, "onCreate");
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		 * Do something useful based on the action
		 */
		if (intent != null) {
			String action = intent.getAction();
			if (action != null) {
				if (action.equals(ACTION_SYNC)) {
					/*
					 * Sync request
					 */
					final CountDownLatch latch = new CountDownLatch(1);
					for (int i = 0; i < THREAD_COUNT; ++i) {
						final Task task = new Task(this, latch, LockManager.LOCK_FLAG_RUNNING_TASK << i);
						final Thread thread = new Thread(task);
						thread.setName(String.format(Locale.US, "Task-%d", i));
						thread.start();
					}
					latch.countDown();

					final LockManager lm = LockManager.get(this);
					lm.releaseSpecialFlag(LockManager.LOCK_FLAG_STARTING_SYNC);
				}
			}
		}

		return START_NOT_STICKY;
	}
}
