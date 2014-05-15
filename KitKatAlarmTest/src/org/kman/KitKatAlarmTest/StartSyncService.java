package org.kman.KitKatAlarmTest;

import org.kman.tests.utils.MyLog;

import android.app.PendingIntent;
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
	 * Action to sync accounts in the background, applying all the usual logic
	 */
	private static final String ACTION_SYNC = "org.kman.KitKatAlarmTest.ACTION_SYNC";

	public static void submitAccountSync(Context context) {
		final LockManager lm = LockManager.get(context);
		lm.acquireSpecialFlag(LockManager.SPECIAL_FLAG_STARTING_SYNC);

		final Intent serviceIntent = new Intent(ACTION_SYNC);
		serviceIntent.setClass(context, StartSyncService.class);

		if (context.startService(serviceIntent) == null) {
			// Failure
			lm.releaseSpecialFlag(LockManager.SPECIAL_FLAG_STARTING_SYNC);
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
					final WorkItem work = new WorkItem(this, startId);
					final Thread thread = new Thread(work);
					thread.setName(work.toString());
					thread.start();

					final LockManager lm = LockManager.get(this);
					lm.releaseSpecialFlag(LockManager.SPECIAL_FLAG_STARTING_SYNC);
				}
			}
		}

		return START_NOT_STICKY;
	}

	static class WorkItem implements Runnable {

		private static final int ITER_COUNT = 10;
		private static final int ITER_DELAY = 500;

		WorkItem(Context context, int startId) {
			mContext = context.getApplicationContext();
			mLockManager = LockManager.get(mContext);
			mLockManager.acquireSpecialFlag(LockManager.SPECIAL_FLAG_RUNNING_SYNC);
			mStartId = startId;
		}

		@Override
		public void run() {

			final Intent intent = new Intent(mContext, MainActivity.class);
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);

			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			final PendingIntent pending = PendingIntent.getActivity(mContext, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			for (int i = 0; i < ITER_COUNT; ++i) {
				final String msg = String.format("Running %d/%d", i + 1, ITER_COUNT);
				final KeepAliveService.Info info = new KeepAliveService.Info(msg);
				KeepAliveService.Facade.start(mContext, info, pending);

				WidgetReceiver.sendBroadcast(mContext);

				if (i != ITER_COUNT - 1) {
					try {
						Thread.sleep(ITER_DELAY);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			}

			TouchWiz.sendTotalUnreadCount(mContext, mStartId);

			KeepAliveService.Facade.stop(mContext);
			mLockManager.releaseSpecialFlag(LockManager.SPECIAL_FLAG_RUNNING_SYNC);
		}

		private Context mContext;
		private LockManager mLockManager;
		private int mStartId;
	}
}
