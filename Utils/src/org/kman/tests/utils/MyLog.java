package org.kman.tests.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;

/**
 * A simple utility for logging to logcat or /sdcard/log*.txt
 * 
 * Log file size is limited by {@link MyLog#MAX_FILE_SIZE}
 */

public abstract class MyLog {

	/**
	 * Maximum allowed log file size
	 */

	private static final int MAX_FILE_SIZE = 30 * 1024 * 1024;

	/**
	 * Log destination
	 */

	public static final int DEST_LOGCAT = 0;
	public static final int DEST_FILE = 1;

	/**
	 * Current configuration settings
	 */
	private static boolean gEnabled = true;
	private static int gDest = DEST_FILE;
	private static boolean gLogDbData = false;

	private static boolean gAutomaticFlush = false;

	/**
	 * Package info
	 */
	private static String gPackageName;
	private static int gPackageVersionCode;
	private static String gPackageVersionName;

	private static String LOG_FILE_NAME;
	private static String CRASH_FILE_NAME;

	/**
	 * Get the log file name
	 * 
	 * @return Log file name
	 */

	public static File getLogFileName() {
		return getLogFileName(LOG_FILE_NAME);
	}

	/**
	 * Get the log file name
	 * 
	 * @return Log file name
	 */

	public static File getLogFileName(String filename) {
		File fnExternalRoot = Environment.getExternalStorageDirectory();
		return new File(fnExternalRoot, filename);
	}

	/**
	 * Log a debug message
	 * 
	 * @param tag
	 *            Entry tag
	 * @param msg
	 *            Entry body
	 */

	public static void d(String tag, String msg) {
		if (gEnabled) {
			stat_msg(false, Log.DEBUG, tag, msg);
		}
	}

	/**
	 * Log an info message
	 * 
	 * @param tag
	 *            Entry tag
	 * @param msg
	 *            Entry body
	 */

	public static void i(String tag, String msg) {
		if (gEnabled) {
			stat_msg(false, Log.INFO, tag, msg);
		}
	}

	/**
	 * Checks if logging is enabled
	 * 
	 * @return Whether {@link #i(String, String)} will produce any output
	 */
	public static boolean isEnabled() {
		return gEnabled;
	}

	/**
	 * Checks if logging of database data is enabled
	 * 
	 * @return
	 */
	public static boolean isLogDbDataEnabled() {
		return gEnabled && gLogDbData;
	}

	/**
	 * Log a warning message
	 * 
	 * @param tag
	 *            Entry tag
	 * @param msg
	 *            Entry body
	 */

	public static void w(String tag, String msg, Throwable t) {
		if (gEnabled || BuildConfig.DEBUG) {
			String stackTrace = getStackTrace(t);
			stat_msg(false, Log.WARN, tag, msg + "\n" + stackTrace);
		}
	}

	/**
	 * Log an error message
	 * 
	 * @param tag
	 *            Message tag
	 * @param msg
	 *            Error message
	 * @param msg
	 *            Entry body
	 */

	public static void e(String tag, String msg, Throwable t) {
		if (gEnabled || BuildConfig.DEBUG) {
			String stackTrace = getStackTrace(t);
			stat_msg(false, Log.ERROR, tag, "***** ERROR: " + msg + "\n" + stackTrace);
		}
	}

	/**
	 * Log an info message
	 * 
	 * @param tag
	 *            Entry tag
	 * @param msg
	 *            Entry body
	 * @param args
	 *            Additional arguments for the message
	 */

	public static void i(String tag, String msg, Object... args) {
		if (gEnabled) {
			stat_msg(false, Log.INFO, tag, String.format(msg, args));
		}
	}

	public static void setPackageInfo(String packageName, int versionCode, String versionName) {
		synchronized (MyLog.class) {
			gPackageName = packageName;
			gPackageVersionCode = versionCode;
			gPackageVersionName = versionName;

			LOG_FILE_NAME = "log-".concat(packageName).concat(".txt");
			CRASH_FILE_NAME = "log-".concat(packageName).concat("-crash.txt");
		}
	}

	public static void setDebugSettings(boolean enabled) {
		synchronized (MyLog.class) {

			/*
			 * Set mode and features
			 */
			gEnabled = enabled;

			/*
			 * Flush and close the current file
			 */

			flushAndCloseLogger();

			setExceptionHandler();
		}
	}

	public static void setExceptionHandler() {

		synchronized (MyLog.class) {

			UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

			if (oldHandler == null || !(oldHandler instanceof EHandler)) {
				EHandler newHandler = new EHandler(oldHandler);
				Thread.setDefaultUncaughtExceptionHandler(newHandler);
			}
		}
	}

	public static void setLastData(String s) {
		synchronized (MyLog.class) {
			gLastData = s;
		}
	}

	private static String getLastData() {
		synchronized (MyLog.class) {
			return gLastData;
		}
	}

	private static String gLastData;

	/*
	 * Implementation
	 */

	static class EHandler implements UncaughtExceptionHandler {

		EHandler(UncaughtExceptionHandler oldHandler) {
			mOldHandler = oldHandler;
		}

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			/*
			 * Write to log file
			 */
			String stackTrace = getStackTrace(e);
			crash_msg(t, stackTrace);

			UncaughtExceptionHandler prevHandler = mOldHandler;
			if (prevHandler != null) {
				/*
				 * Call the default handler: this will notify the user
				 */
				mOldHandler.uncaughtException(t, e);
			}
		}

		private UncaughtExceptionHandler mOldHandler;
	}

	private static void stat_msg(boolean raw, int level, String tag, String msg) {
		MyLog logger = getLogger();
		logger.do_msg(raw, level, tag, msg);
	}

	private static void crash_msg(Thread thread, String msg) {
		if (gEnabled) {
			/*
			 * Write to main log
			 */
			MyLog logger = getLogger();
			logger.do_crash_msg(thread, msg);
		}

		/*
		 * Write to crash log
		 */
		MyLog_File fileLogger = new MyLog_File(CRASH_FILE_NAME);
		fileLogger.do_crash_msg(thread, msg);
		fileLogger.do_close();
	}

	private MyLog() {
	}

	abstract protected void do_msg(boolean raw, int level, String tag, String msg);

	abstract protected void do_crash_msg(Thread thread, String msg);

	protected void do_close() {
	}

	protected String getMemoryInfo() {
		return String.format("Native heap size: %d allocated / %d free, Runtime max %d",
				Debug.getNativeHeapAllocatedSize(), Debug.getNativeHeapFreeSize(), Runtime.getRuntime().maxMemory());
	}

	private static String getPackageInfo() {
		if (gPackageName != null && gPackageVersionName != null) {
			return String.format("%s %s (%d)", gPackageName, gPackageVersionName, gPackageVersionCode);
		}
		return "- none -";
	}

	private static String getProcessInfo() {
		int pid = android.os.Process.myPid();
		long time = android.os.Process.getElapsedCpuTime() / 1000;
		return String.format("pid %d, elapsed CPU %d seconds", pid, time);
	}

	private static String getBuildInfo() {
		return String.format("%s, %s %s, ver. %s, %s", Build.MANUFACTURER, Build.PRODUCT, Build.MODEL, Build.DISPLAY,
				Build.FINGERPRINT);
	}

	private static String getStackTrace(Throwable e) {
		Writer result = new StringWriter();
		PrintWriter writer = new PrintWriter(result);
		e.printStackTrace(writer);
		writer.append('\n');

		String lastData = getLastData();
		if (lastData != null) {
			writer.append("Last data:\n");
			writer.append(lastData);
			writer.append('\n');
		}

		Throwable cause = e.getCause();
		while (cause != null) {
			writer.append("Caused by:\n");
			cause.printStackTrace(writer);
			cause = cause.getCause();
		}
		String stackTrace = result.toString();
		writer.close();
		return stackTrace;
	}

	/*
	 * File based logging implementation
	 */

	private static class MyLog_File extends MyLog {

		MyLog_File() {
			this(LOG_FILE_NAME);
		}

		MyLog_File(String filename) {

			/*
			 * Determine log file name
			 */
			File fc = getLogFileName(filename);

			/*
			 * Open log file
			 */
			try {
				if (fc.exists() && fc.length() < MAX_FILE_SIZE) {
					mStream = new FileOutputStream(fc, true);
				} else {
					mStream = new FileOutputStream(fc);
				}
			} catch (IOException x) {
				android.util.Log.e("MyLog", "Error creating log file " + fc.getAbsolutePath(), x);
			}

			/*
			 * Write system information
			 */
			if (mStream != null) {
				mDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS Z");

				String d = mDateFormat.format(new Date());
				String s = String.format(
						"*\n* New log file session: %s\n* Package: %s\n* Process: %s\n* Build: %s\n*\n", d,
						getPackageInfo(), getProcessInfo(), getBuildInfo());

				try {
					mStream.write(stringToBytes(s));
				} catch (IOException x) {
					do_close();
				}
			}
		}

		@Override
		protected void do_close() {
			synchronized (this) {
				if (mStream != null) {
					try {
						mStream.close();
					} catch (IOException x) {
						// Ignore
					}
				}
				mStream = null;
			}

			super.do_close();
		}

		@Override
		protected void do_msg(boolean raw, int level, String tag, String msg) {
			synchronized (this) {
				if (mStream != null) {
					try {
						String d = mDateFormat.format(new Date());
						String s = String.format("%s\t%s\t%s\n", d, tag, msg);

						if (raw) {
							mStream.write(stringToBytes(s));
						} else {
							mStream.write(s.getBytes());
						}

						if (gAutomaticFlush) {
							mStream.flush();
						}

					} catch (IOException x) {
						do_close();
					}
				}
			}
		}

		@Override
		protected void do_crash_msg(Thread thread, String msg) {
			synchronized (this) {
				if (mStream != null) {
					try {
						long threadId = thread.getId();

						String d = mDateFormat.format(new Date());
						String m = getMemoryInfo();
						String s = String.format("%s\t[%d]\t<<CRASH>>\n%s\n%s\n", d, threadId, m, msg);

						mStream.write(stringToBytes(s));

						if (gAutomaticFlush && mStream != null) {
							mStream.flush();
						}
					} catch (IOException x) {
						do_close();
					}
				}
			}
		}

		private OutputStream mStream;
		private DateFormat mDateFormat;
	}

	/*
	 * Logcat based implementation
	 */

	private static class MyLog_Logcat extends MyLog {

		public MyLog_Logcat() {
		}

		@Override
		protected void do_msg(boolean raw, int level, String tag, String msg) {
			Log.println(level, tag, msg);
		}

		@Override
		protected void do_crash_msg(Thread thread, String msg) {
			long threadId = thread.getId();

			String t = getClass().getPackage().getName();
			String m = getMemoryInfo();
			String s = String.format("[%d]\t<<CRASH>>\n%s\n%s\n", threadId, m, msg);

			Log.e(t, s);
		}
	}

	private static MyLog getLogger() {
		synchronized (MyLog.class) {
			if (gLogger == null) {
				switch (gDest) {
				default:
				case DEST_FILE:
					gLogger = new MyLog_File();
					break;
				case DEST_LOGCAT:
					gLogger = new MyLog_Logcat();
					break;
				}
			}
			return gLogger;
		}
	}

	/**
	 * Flush and close the log file, do nothing if not open. Call from Activity.onStop.
	 */
	private static void flushAndCloseLogger() {
		synchronized (MyLog.class) {
			if (gLogger != null) {
				gLogger.do_close();
				gLogger = null;
			}
		}
	}

	private static MyLog gLogger;

	private static byte[] stringToBytes(String s) {
		if (s == null) {
			return null;
		}
		final int len = s.length();
		byte[] buf = new byte[len];
		for (int i = 0; i < len; ++i) {
			buf[i] = (byte) s.charAt(i);
		}
		return buf;
	}
}
