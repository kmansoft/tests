package org.kman.MobielRadioTest.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.net.Socket;

import org.kman.MobileRadioTest.util.MyLog;

public class StreamUtil {

	private static final String TAG = "StreamUtil";

	/**
	 * Safely closes a stream, eating IO exceptions
	 * 
	 * @param stream
	 *            The stream to close, may be null
	 */
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

	/**
	 * Safely closes a stream, eating IO exceptions
	 * 
	 * @param stream
	 *            The stream to close, may be null
	 */
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

	/**
	 * Safely flushes and closes a stream, eating IO exceptions
	 * 
	 * @param stream
	 *            The stream to close, may be null
	 */
	public static void flushAndCloseStream(OutputStream stream) {
		if (stream != null) {
			try {
				stream.flush();
			} catch (IOException x) {
				MyLog.i(TAG, "Stream flush error, ignoring");
			}

			try {
				stream.close();
			} catch (IOException x) {
				MyLog.i(TAG, "Stream close error, ignoring");
			}
		}
	}

	/**
	 * Safely closes a socket, eating IO exceptions
	 * 
	 * @param stream
	 *            The socket to close, may be null
	 */
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

	/**
	 * Safely closes a random access file, eating IO exceptions
	 * 
	 * @param raf
	 *            The random access file to close, may be null
	 */
	public static void closeRaf(RandomAccessFile raf) {
		if (raf != null) {
			try {
				raf.close();
			} catch (IOException x) {
				MyLog.i(TAG, "Raf close error, ignoring");
			}
		}
	}

	/**
	 * Safely sets a socket timeout. {@link Socket#setSoTimeout(int)}
	 * 
	 * @param socket
	 *            The socket
	 * @param timeout
	 *            Timeout value, in milliseconds, 0 for infinite.
	 */
	public static void setSocketTimeout(Socket socket, int timeout) {
		if (socket != null) {
			try {
				socket.setSoTimeout(timeout);
			} catch (IOException x) {
				MyLog.i(TAG, "Error setting socket timeout, ignoring");
			}
		}
	}

	/**
	 * Safely closes a reader, eating IO exceptions
	 * 
	 * @param reader
	 *            The reader to close, may be null
	 */
	public static void closeReader(Reader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException x) {
				MyLog.i(TAG, "Stream close error, ignoring");
			}
		}
	}

}
