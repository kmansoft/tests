package org.kman.KitKatAlarmTest;

import org.kman.tests.utils.MyLog;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class Task {
	private static final String TAG = "Task";

	private static final int ITER_COUNT = 10;
	private static final int ITER_DELAY = 500;

	public Task(Context context, int startId) {
		mContext = context;
		mStartId = startId;
	}

	public void execute() {
		final Intent intent = new Intent(mContext, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		final PendingIntent pending = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final boolean isEnabled = connectivityManager.getBackgroundDataSetting();
		MyLog.i(TAG, "getBackgroundDataSetting = %b", isEnabled);

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
	}

	private Context mContext;
	private int mStartId;
}
