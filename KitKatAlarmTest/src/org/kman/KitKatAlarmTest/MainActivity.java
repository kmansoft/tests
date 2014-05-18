package org.kman.KitKatAlarmTest;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.kman.tests.utils.MyLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "MainActivity";

	private static final int MAX_LOG_TEXT = 30 * 1024;
	private static final int MAX_NEWLINE_SEARCH = 1024;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.i(TAG, "onCreate");

		setContentView(R.layout.activity_main);
		mLogText = (EditText) findViewById(R.id.log_text);
		mLogRefresh = (Button) findViewById(R.id.log_refresh);
		mLogRefresh.setOnClickListener(this);
		mLogReset = (Button) findViewById(R.id.log_reset);
		mLogReset.setOnClickListener(this);

		AlarmReceiver.setNextAlarmWithCheck(this);

		onLogRefresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.log_refresh:
			onLogRefresh();
			break;
		case R.id.log_reset:
			onLogReset();
			break;
		}
	}

	@SuppressWarnings("deprecation")
	private void onLogRefresh() {
		RandomAccessFile raf = null;
		final File f = MyLog.getLogFileName();
		try {
			final int len = (int) f.length();
			raf = new RandomAccessFile(f, "r");

			int readOffset = 0;
			int readLen = len;
			if (len > MAX_LOG_TEXT) {
				readOffset = len - MAX_LOG_TEXT;
				raf.seek(readOffset);
				readLen = MAX_LOG_TEXT;
			}

			final byte[] b = new byte[readLen];
			final int readCount = raf.read(b);

			int textOffset = 0;
			if (readOffset > 0) {
				while (textOffset < readCount && textOffset < MAX_NEWLINE_SEARCH) {
					++textOffset;
					if (b[textOffset - 1] == '\n') {
						break;
					}
				}
			}

			final String s = new String(b, 0, textOffset, readCount - textOffset);
			mLogText.setText(s);
			mLogText.setSelection(s.length());

		} catch (Exception x) {
			mLogText.setText(x.toString());
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException x) {
					// Ignore
				}
			}
		}
	}

	private void onLogReset() {
		final File f = MyLog.getLogFileName();
		MyLog.setDebugSettings(true);
		f.delete();
		MyLog.i(TAG, "The log has been reset");
		onLogRefresh();
		TouchWiz.sendTotalUnreadCount(this, 0);
	}

	private EditText mLogText;
	private Button mLogRefresh;
	private Button mLogReset;
}
