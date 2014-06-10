package org.kman.KitKatAlarmTest.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncAdapterService extends Service {

	private static final String TAG = "SyncAdapterService";

	private static SyncAdapter sSyncAdapter = null;
	private static final Object sSyncAdapterLock = new Object();

	@Override
	public void onCreate() {
		synchronized (sSyncAdapterLock) {
			if (sSyncAdapter == null) {
				sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return sSyncAdapter.getSyncAdapterBinder();
	}

}
