/***
  Copyright (c) 2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
 */

package com.commonsware.android.wakesvc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import org.kman.tests.utils.MyLog;

import android.content.Intent;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class ScheduledService extends WakefulIntentService {

	private static final String TAG = "ScheduledService";

	private static final int CONNECT_TIMEOUT = 30 * 1000;
	private static final int DATA_TIMEOUT = 60 * 1000;

	private static final String SERVER = "imap.gmail.com";
	private static final int PORT = 993;

	public ScheduledService() {
		super("ScheduledService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		try {
			testNetworking();
		} catch (Exception x) {
			MyLog.w(TAG, "Error in networking test", x);
		}

		Log.d(TAG, "I ran!");

	}

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

	static class StreamUtil {

		public static void closeSocket(Socket socket) {
			if (socket != null) {
				try {
					MyLog.i(TAG, "Closing socket %s", socket);
					socket.close();
				} catch (IOException x) {
					MyLog.i(TAG, "Error closing socket, ignoring");
				}
			}
		}

		public static void closeStream(InputStream stream) {
			if (stream != null) {
				try {
					MyLog.i(TAG, "Closing stream %s", stream);
					stream.close();
				} catch (IOException x) {
					MyLog.i(TAG, "Stream close error, ignoring");
				}
			}
		}

		public static void closeStream(OutputStream stream) {
			if (stream != null) {
				try {
					MyLog.i(TAG, "Closing stream %s", stream);
					stream.close();
				} catch (IOException x) {
					MyLog.i(TAG, "Stream close error, ignoring");
				}
			}
		}
	}

	private SSLSocketFactory getSSLSocketFactory() {
		synchronized (ScheduledService.class) {
			if (gFactory == null) {
				gFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			}
			return gFactory;
		}
	}

	private static SSLSocketFactory gFactory;
}
