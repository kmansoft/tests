package org.kman.KitKatAlarmTest.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncAccountAuthenticatorService extends Service {

	private SyncAccountAuthenticator mAuthenticator;

	@Override
	public void onCreate() {
		mAuthenticator = new SyncAccountAuthenticator(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mAuthenticator.getIBinder();
	}
}
