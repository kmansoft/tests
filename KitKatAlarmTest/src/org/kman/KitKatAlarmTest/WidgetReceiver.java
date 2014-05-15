package org.kman.KitKatAlarmTest;

import org.kman.tests.utils.MyLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class WidgetReceiver extends BroadcastReceiver {

	private static final String TAG = "WidgetReceiver";

	private static final String ACTION = "org.kman.KitKatAlarmTest.FOLDER_STATE_URI_CHANGE";

	public static void sendBroadcast(Context context) {
		final Intent intent = new Intent(ACTION);
		intent.setClass(context, WidgetReceiver.class);

		if (Build.VERSION.SDK_INT >= 16) {
			intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		}

		context.sendBroadcast(intent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		MyLog.i(TAG, "onReceive: %s", intent);
	}
}
