package org.kman.KitKatAlarmTest.net;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.SSLSessionCache;

public class SSLSessionCacheCompat {

	private static SSLSessionCache gCache;

	@TargetApi(8)
	public static SSLSessionCache getSSLSessionCache(Context context) {
		synchronized (SSLSessionCacheCompat.class) {
			if (gCache == null) {
				gCache = new SSLSessionCache(context.getApplicationContext());
			}
		}
		return gCache;
	}
}
