package org.kman.MobileRadioTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.SSLSocketFactory;

import org.kman.MobileRadioTest.net.StreamUtil;
import org.kman.MobileRadioTest.util.MyLog;

import android.content.Context;

public class Task implements Runnable {
	private static final String TAG = "Task";

	private static final int CONNECT_TIMEOUT = 30 * 1000;
	private static final int DATA_TIMEOUT = 60 * 1000;

	private static final String SERVER = "imap.gmail.com";
	private static final int PORT = 993;

	private static final int SLEEP_DURATION = 3;

	private static/* final */boolean TEST_WITH_NETWORKING = true;
	private static/* final */boolean LEAVE_CONNECTIONS_OPEN = true;

	public Task(Context context, CountDownLatch latch, int lockFlag) {
		mContext = context.getApplicationContext();
		mLatch = latch;
		mLockManager = LockManager.get(mContext);
		mLockFlag = lockFlag;
	}

	@Override
	public void run() {
		try {
			mLatch.await();
		} catch (Exception x) {
			MyLog.w(TAG, "Error in networking test", x);
			return;
		}

		mLockManager.acquireSpecialFlag(mLockFlag);
		try {
			if (TEST_WITH_NETWORKING) {
				testNetworking();
			} else {
				testNoNetworking();
			}

			MyLog.i(TAG, "Sleeping for %d sec", SLEEP_DURATION);
			try {
				Thread.sleep(SLEEP_DURATION * 1000);
			} catch (InterruptedException x) {
			}
			MyLog.i(TAG, "Done sleeping");

		} catch (Exception x) {
			MyLog.w(TAG, "Error in networking test", x);
		} finally {
			mLockManager.releaseSpecialFlag(mLockFlag);
		}
	}

	private void testNoNetworking() {
		MyLog.i(TAG, "No networking");
	}

	private void testNetworking() throws IOException {
		Socket socket = null;
		InputStream streamInput = null;
		OutputStream streamOutput = null;

		try {
			/*
			 * Connect at socket level
			 */
			final SSLSocketFactory socketFactory = getSSLSocketFactory();

			final long nTimeStart = System.currentTimeMillis();

			final InetSocketAddress sockAddr = new InetSocketAddress(SERVER, PORT);
			MyLog.i(TAG, "Connecting to: %s", sockAddr);

			socket = socketFactory.createSocket();
			try {
				socket.connect(sockAddr, CONNECT_TIMEOUT);
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
				throw new IOException(x);
			} catch (IOException x) {
				throw x;
			}

			try {
				/*
				 * Read IMAP greeting, send CAPABILITY and read response. Then close connection.
				 */
				final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(streamOutput,
						StandardCharsets.US_ASCII), 1024);
				final BufferedReader reader = new BufferedReader(new InputStreamReader(streamInput,
						StandardCharsets.US_ASCII), 8192);

				// Read greeting
				readImapResponse(reader, null);

				// Send "CAPABILITY" and read response
				sendImapCommand(writer, "k1", "CAPABILITY");
				readImapResponse(reader, "k1");

				/*
				 * Gracefully close connection
				 */
				sendImapCommand(writer, "k2", "LOGOUT");
				readImapResponse(reader, "k2");

			} catch (Exception x) {
				throw new IOException(x);
			}

		} finally {
			if (LEAVE_CONNECTIONS_OPEN) {
				MyLog.i(TAG, "Leaving socket %s open", socket);
			} else {
				StreamUtil.closeSocket(socket);
				StreamUtil.closeStream(streamInput);
				StreamUtil.closeStream(streamOutput);
			}

			/*
			 * Log that we're done
			 */
			MyLog.i(TAG, "testNetworking end");
		}
	}

	private void sendImapCommand(BufferedWriter w, String tag, String command) throws IOException {
		MyLog.i(TAG, "Send: %s %s", tag, command);
		final String s = tag + " " + command + "\r\n";
		w.write(s);
		w.flush();
	}

	private void readImapResponse(BufferedReader r, String tag) throws IOException {
		String s;
		while ((s = r.readLine()) != null) {
			MyLog.i(TAG, "Read: %s", s);
			if (tag == null || s.startsWith(tag)) {
				break;
			}
		}
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

	private Context mContext;
	private CountDownLatch mLatch;
	private LockManager mLockManager;
	private int mLockFlag;
}
