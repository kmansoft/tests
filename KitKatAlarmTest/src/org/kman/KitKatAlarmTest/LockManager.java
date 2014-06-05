package org.kman.KitKatAlarmTest;

import org.kman.tests.utils.MyLog;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;

public class LockManager {

	private static final String TAG = "LockManager";
	private static final String WAKE_LOCK_TAG = "KitKatAlarmTest WakeLock";

	public static LockManager get(Context context) {
		synchronized (LockManager.class) {
			if (gInstance == null) {
				gInstance = new LockManager(context);
			}
			return gInstance;
		}
	}

	public static final int SPECIAL_FLAG_STARTING_SYNC = 0x0001;
	public static final int SPECIAL_FLAG_RUNNING_TASK = 0x0002;
	public static final int SPECIAL_FLAG_SET_ALARM_SERVICE = 0x0004;

	public void acquireSpecialFlag(int flag) {
		synchronized (this) {
			mWakeLockFlags |= flag;
			MyLog.i(TAG, "Acquired special flag 0x%08x, result 0x%08x", flag, mWakeLockFlags);
			acquireWakeLockLocked();
		}
	}

	public void releaseSpecialFlag(int flag) {
		synchronized (this) {
			mWakeLockFlags &= ~flag;
			MyLog.i(TAG, "Released special flag 0x%08x, result 0x%08x", flag, mWakeLockFlags);
			if (mWakeLockFlags == 0) {
				releaseWakeLockLocked();
			}
		}
	}

	private void acquireWakeLockLocked() {
		if (mWakeLock.isHeld()) {
			MyLog.i(TAG, "... Wake lock already held");
		} else {
			MyLog.i(TAG, "Acquiring the Wake lock");
			mWakeLock.acquire();
			mWakeLockStartTime = SystemClock.elapsedRealtime();
		}
	}

	private boolean releaseWakeLockLocked() {
		if (mWakeLock.isHeld()) {
			MyLog.i(TAG, "Releasing the Wake lock, time held = %.2f sec",
					(SystemClock.elapsedRealtime() - mWakeLockStartTime) / 1000.0f);
			mWakeLock.release();
			mWakeLockStartTime = 0;
			return true;
		} else {
			MyLog.i(TAG, "Wake lock is not held");
			return false;
		}
	}

	private LockManager(Context context) {
		mContext = context.getApplicationContext();

		mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
		mWakeLock.setReferenceCounted(false);
	}

	private static LockManager gInstance;

	private Context mContext;

	private PowerManager mPowerManager;
	private PowerManager.WakeLock mWakeLock;
	private long mWakeLockStartTime;

	private int mWakeLockFlags;
}
