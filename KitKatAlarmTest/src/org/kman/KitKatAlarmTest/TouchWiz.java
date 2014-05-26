package org.kman.KitKatAlarmTest;

import android.content.Context;
import android.content.Intent;

public class TouchWiz {

	// private static final String TAG = "TouchWiz";

	// private static final String SEC_PACKAGE_NAME = "com.sec.android.app.launcher";

	private static final String SEC_ACTION = "android.intent.action.BADGE_COUNT_UPDATE";

	public static void sendTotalUnreadCount(Context context, int count) {
		try {

			Intent intent = new Intent(SEC_ACTION);
			intent.putExtra("badge_count", count);
			intent.putExtra("badge_count_package_name", context.getPackageName());
			intent.putExtra("badge_count_class_name", "org.kman.KitKatAlarmTest.MainActivity");
			context.sendBroadcast(intent);
		} catch (Exception x) {
			// Ignore
		}
	}
}
