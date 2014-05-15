package org.kman.tests.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class PackageUtils {

	private static final String TAG = "PackageUtils";

	public static int getVersionCode(Context context) {
		synchronized (PackageUtils.class) {
			if (gVersionCode == 0) {
				try {
					final String packageName = context.getPackageName();
					final PackageManager pm = context.getPackageManager();
					final PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
					gVersionCode = pi.versionCode;
				} catch (Exception x) {
					Log.w(TAG, "Error get package meta", x);
				}
			}
			return gVersionCode;
		}
	}

	private static int gVersionCode;
}
