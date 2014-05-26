package org.kman.KitKatAlarmTest;

import java.util.LinkedList;

import org.kman.tests.utils.MyLog;

import android.content.Context;

public class TaskExecutor implements Runnable {
	private static final String TAG = "TaskExecutor";

	public static TaskExecutor get(Context context) {
		synchronized (TaskExecutor.class) {
			if (gInstance == null) {
				gInstance = new TaskExecutor(context);
			}
			return gInstance;
		}
	}

	public void submit(Task task) {
		synchronized (this) {
			if (mThread == null) {
				mThread = new Thread(this, TAG);
				mThread.start();
			}
		}

		mLockManager.acquireSpecialFlag(LockManager.SPECIAL_FLAG_RUNNING_TASK);

		synchronized (this) {
			mTaskQueue.add(task);
			this.notifyAll();
		}
	}

	@Override
	public void run() {
		// DEBUG Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

		for (;;) {
			Task task;
			synchronized (this) {
				while (mTaskQueue.size() == 0) {
					try {
						this.wait();
					} catch (InterruptedException x) {
						MyLog.i(TAG, "Exception in worker thread: %s", x);
					}
				}

				task = mTaskQueue.removeFirst();
			}

			onTask(task);
		}
	}

	private void onTask(Task task) {
		MyLog.i(TAG, "Executing task %s", task);
		task.execute();

		synchronized (this) {
			if (mTaskQueue.size() == 0) {
				mLockManager.releaseSpecialFlag(LockManager.SPECIAL_FLAG_RUNNING_TASK);
			}
		}
	}

	private TaskExecutor(Context context) {
		mContext = context.getApplicationContext();
		mTaskQueue = new LinkedList<Task>();
		mLockManager = LockManager.get(mContext);
	}

	private static TaskExecutor gInstance;

	private Context mContext;
	private LinkedList<Task> mTaskQueue;
	private LockManager mLockManager;
	private Thread mThread;
}
