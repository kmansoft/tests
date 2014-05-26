package org.kman.KitKatAlarmTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import org.kman.KitKatAlarmTest.net.AddressResolution;
import org.kman.KitKatAlarmTest.net.ConnectionManager;
import org.kman.KitKatAlarmTest.net.SSLSocketFactoryMaker;
import org.kman.KitKatAlarmTest.net.StreamUtil;
import org.kman.tests.utils.MyLog;

import android.content.Context;

public class Task {
	private static final String TAG = "Task";

	private static final int ITER_COUNT = 10;
	private static final int ITER_DELAY = 500;

	private static final String SERVER = "imap.gmail.com";
	private static final int PORT = 993;

	// private static/* final */boolean VERBOSE_LOG = false;

	public Task(Context context, int startId) {
		mContext = context;
		mStartId = startId;
	}

	public void execute() {
		// Not sure if this is needed

		// DEBUG
		// ConnectivityManager connectivityManager = (ConnectivityManager) mContext
		// .getSystemService(Context.CONNECTIVITY_SERVICE);
		// final boolean isEnabled = connectivityManager.getBackgroundDataSetting();
		// if (VERBOSE_LOG) MyLog.i(TAG, "getBackgroundDataSetting = %b", isEnabled);

		// Networking test -- this is what causes alarms to fire too early
		try {
			testNetworking();
		} catch (Exception x) {
			MyLog.w(TAG, "Error in networking test", x);
		}

		// Shot in the dark test -- did not cause alarm issue, not sure if needed
		testShotInTheDark();
	}

	/**
	 * Running this code cause AlarmManager alarms to fire early.
	 * 
	 * The documentation is very clear and explicit that even for for targetApi >= 19, alarms are
	 * _never ever under any circumstance_ delivered earlier than the scheduled time.
	 * 
	 * This applies to all of: setAlarm, setWindow, setExact.
	 * 
	 * On two Samsung devices, I'm seeing alarms being delivered early.
	 * 
	 * How much early?
	 * 
	 * This app, Galaxy Note 3 N9005 - from 15 seconds
	 * 
	 * This app, Galaxy S4 i9505 - 1-10 minutes
	 * 
	 * My real app, both of these devices - up to 3-10 minutes
	 * 
	 * All apps set alarms every 15 minutes.
	 * 
	 * These cases can be seen in the app's log as:
	 * 
	 * AlarmReceiver ***** fired too early *****
	 * 
	 * and are highlighted in red.
	 * 
	 * Immediately above that is a dump of the alarm's extras, which includes 1) when the alarm was
	 * set and 2) for what time it was scheduled.
	 * 
	 * No such issue on Nexus 5 and HTC One Max, both also with 4.4.2
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	private void testNetworking() throws IOException {
		Socket socket = null;
		InputStream streamInput = null;
		OutputStream streamOutput = null;

		/*
		 * Connect at socket level
		 */
		final InetAddress[] addrList = AddressResolution.resolveServerName(SERVER);
		final SSLSocketFactory socketFactory = SSLSocketFactoryMaker.getStrictFactory(mContext);

		final long nTimeStart = System.currentTimeMillis();
		final int addrListSize = addrList.length;
		for (int i = 0; i < addrListSize; ++i) {
			final InetSocketAddress sockAddr = new InetSocketAddress(addrList[i], PORT);
			MyLog.i(TAG, "Trying: %s", sockAddr);
			socket = socketFactory.createSocket();
			try {
				socket.connect(sockAddr, SSLSocketFactoryMaker.CONNECT_TIMEOUT);
				MyLog.i(TAG, "Socket connection completed");
				break; // Success
			} catch (IOException x) {
				StreamUtil.closeSocket(socket);
				socket = null;

				if (i == addrListSize - 1) {
					throw x;
				}
			}
		}

		if (socket == null || !socket.isConnected()) {
			throw new ConnectException("Could not connect to " + SERVER);
		}

		try {
			/*
			 * Get streams, this negotiates SSL
			 */
			socket.setSoTimeout(SSLSocketFactoryMaker.DATA_TIMEOUT);
			streamInput = socket.getInputStream();
			streamOutput = socket.getOutputStream();

			long nTimeConnect = System.currentTimeMillis() - nTimeStart;

			MyLog.i(TAG, "Connection to %s:%d completed: %s, time = %.2f sec", SERVER, PORT,
					socket.getRemoteSocketAddress(), nTimeConnect / 1000.0f);

			int nSendBufSize = socket.getSendBufferSize();
			int nRecvBufSize = socket.getReceiveBufferSize();

			MyLog.i(TAG, "Buffer sizes: %d send, %d receive", nSendBufSize, nRecvBufSize);

		} catch (RuntimeException x) {
			StreamUtil.closeSocket(socket);
			StreamUtil.closeStream(streamInput);
			StreamUtil.closeStream(streamOutput);
			throw new IOException(x);
		} catch (IOException x) {
			StreamUtil.closeSocket(socket);
			StreamUtil.closeStream(streamInput);
			StreamUtil.closeStream(streamOutput);
			throw x;
		}

		try {
			/*
			 * Read something
			 */
			final byte[] b = new byte[8192];
			final int r = streamInput.read(b);
			final String s = new String(b, 0, 0, r);
			MyLog.i(TAG, "Read: %s", s);
		} catch (Exception x) {
			StreamUtil.closeSocket(socket);
			StreamUtil.closeStream(streamInput);
			StreamUtil.closeStream(streamOutput);
			throw new IOException(x);
		}

		final ConnectionManager connectionManager = ConnectionManager.get(mContext);
		final ConnectionManager.Connection connection = new ConnectionManager.Connection();
		connection.mSocket = socket;
		connectionManager.postClose(connection);
	}

	private void testShotInTheDark() {
		// final Intent intent = new Intent(mContext, MainActivity.class);
		// intent.setAction(Intent.ACTION_MAIN);
		// intent.addCategory(Intent.CATEGORY_LAUNCHER);
		//
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		for (int i = 0; i < ITER_COUNT; ++i) {
			// DEBUG
			// final String msg = String.format("Running %d/%d", i + 1, ITER_COUNT);
			// final KeepAliveService.Info info = new KeepAliveService.Info(msg);
			//
			// final PendingIntent pending = PendingIntent.getActivity(mContext, 0, intent,
			// PendingIntent.FLAG_UPDATE_CURRENT);
			// KeepAliveService.Facade.start(mContext, info, pending);

			if (i != ITER_COUNT - 1) {
				try {
					Thread.sleep(ITER_DELAY);
				} catch (InterruptedException e) {
					// Ignore
				}
			}

			// DEBUG WidgetReceiver.sendBroadcast(mContext);
		}

		// DEBUG TouchWiz.sendTotalUnreadCount(mContext, mStartId);

		// DEBUG KeepAliveService.Facade.stop(mContext);
	}

	private Context mContext;
	private int mStartId;
}
