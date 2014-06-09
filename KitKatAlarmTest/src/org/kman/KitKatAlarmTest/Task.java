package org.kman.KitKatAlarmTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import org.kman.KitKatAlarmTest.net.StreamUtil;
import org.kman.tests.utils.MyLog;

import android.content.Context;

public class Task {
	private static final String TAG = "Task";

	private static final int CONNECT_TIMEOUT = 30 * 1000;
	private static final int DATA_TIMEOUT = 60 * 1000;

	private static final String SERVER = "imap.gmail.com";
	private static final int PORT = 993;

	public Task(Context context, int startId) {
		mContext = context;
	}

	public void execute() {
		// Networking test -- this is what causes alarms to fire too early
		try {
			testNetworking();
		} catch (Exception x) {
			MyLog.w(TAG, "Error in networking test", x);
		}
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
		final SSLSocketFactory socketFactory = getSSLSocketFactory();

		final long nTimeStart = System.currentTimeMillis();

		final InetSocketAddress sockAddr = new InetSocketAddress(SERVER, PORT);
		MyLog.i(TAG, "Trying: %s", sockAddr);

		socket = socketFactory.createSocket();
		try {
			socket.connect(sockAddr, CONNECT_TIMEOUT);
			MyLog.i(TAG, "Socket connection completed");
		} catch (IOException x) {
			StreamUtil.closeSocket(socket);
			socket = null;
			throw x;
		}

		if (socket == null || !socket.isConnected()) {
			throw new ConnectException("Could not connect to " + SERVER);
		}

		try {
			/*
			 * Get streams, this negotiates SSL
			 */
			socket.setSoTimeout(DATA_TIMEOUT);
			streamInput = socket.getInputStream();
			streamOutput = socket.getOutputStream();

			long nTimeConnect = System.currentTimeMillis() - nTimeStart;

			MyLog.i(TAG, "Connection to %s:%d completed: %s, time = %.2f sec", SERVER, PORT,
					socket.getRemoteSocketAddress(), nTimeConnect / 1000.0f);

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
			 * Write something
			 */
			// final byte[] w = "A bad http command\r\n".getBytes();
			// streamOutput.write(w);

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

		StreamUtil.closeSocket(socket);
		StreamUtil.closeStream(streamInput);
		StreamUtil.closeStream(streamOutput);
	}

	private SSLSocketFactory getSSLSocketFactory() {
		synchronized (Task.class) {
			if (gFactory == null) {
				gFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			}
			return gFactory;
		}
	}

	private static SSLSocketFactory gFactory;

	@SuppressWarnings("unused")
	private Context mContext;
}
