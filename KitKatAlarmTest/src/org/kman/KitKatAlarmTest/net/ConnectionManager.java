package org.kman.KitKatAlarmTest.net;

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import org.kman.tests.utils.MyLog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class ConnectionManager implements Handler.Callback {
	private static final String TAG = "ConnectionManager";
	private static final int WHAT_CLOSE_CONNECTION = 0;
	private static final int CLOSE_DELAY = 25 * 60 * 1000; // 25 minutes

	public static ConnectionManager get(Context context) {
		synchronized (ConnectionManager.class) {
			if (gInstance == null) {
				gInstance = new ConnectionManager(context);
			}
			return gInstance;
		}
	}

	public static class Connection {
		public Socket mSocket;
	}

	public void postClose(Connection connection) {

		doCloseConnection(connection);

		// synchronized (this) {
		// mClosingConnections.add(connection);
		// final Message msg = mConnectionHandler.obtainMessage(WHAT_CLOSE_CONNECTION, connection);
		// mConnectionHandler.sendMessageDelayed(msg, CLOSE_DELAY);
		// }
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case WHAT_CLOSE_CONNECTION:
			doCloseConnection((Connection) msg.obj);
			break;
		default:
			return false;
		}
		return true;
	}

	private ConnectionManager(Context context) {
		mContext = context.getApplicationContext();
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		mClosingConnections = new LinkedList<ConnectionManager.Connection>();

		synchronized (ConnectionManager.class) {
			if (gConnectionThread == null) {
				gConnectionThread = new HandlerThread("ConnectionThread");
				gConnectionThread.start();
			}
		}

		Looper looper = gConnectionThread.getLooper();
		mConnectionHandler = new Handler(looper, this);

	}

	private void doCloseConnection(Connection connection) {
		MyLog.i(TAG, "Closing connection: %s", connection.mSocket);
		StreamUtil.closeSocket(connection.mSocket);
		synchronized (this) {
			mClosingConnections.remove(connection);
		}
	}

	private static ConnectionManager gInstance;
	private static HandlerThread gConnectionThread;

	private Context mContext;
	@SuppressWarnings("unused")
	private ConnectivityManager mConnectivityManager;
	private Handler mConnectionHandler;
	private List<Connection> mClosingConnections;
}
