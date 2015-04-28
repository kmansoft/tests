package org.kman.MobileRadioTest;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kman.MobielRadioTest.net.StreamUtil;
import org.kman.MobileRadioTest.util.MyLog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "MainActivity";

	private static final int MAX_LOG_TEXT = 30 * 1024;
	private static final int MAX_NEWLINE_SEARCH = 1024;

	private static final Pattern BOLD_PATTERN = Pattern.compile("Task\\s+testNetworking\\s+(begin|end)");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.i(TAG, "onCreate");

		setContentView(R.layout.activity_main);
		mLogScroll = (ScrollView) findViewById(R.id.log_scroll);
		mLogText = (TextView) findViewById(R.id.log_text);

		mRunSync = (Button) findViewById(R.id.run_sync);
		mRunSync.setOnClickListener(this);
		mLogRefresh = (Button) findViewById(R.id.log_refresh);
		mLogRefresh.setOnClickListener(this);
		mLogReset = (Button) findViewById(R.id.log_reset);
		mLogReset.setOnClickListener(this);

		AlarmReceiver.setNextAlarmWithCheck(this);

		onLogRefresh();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.run_sync:
			onRunSync();
			break;
		case R.id.log_refresh:
			onLogRefresh();
			break;
		case R.id.log_reset:
			onLogReset();
			break;
		}
	}

	private void onRunSync() {
		final Intent intent = new Intent(StartSyncService.ACTION_SYNC);
		intent.setClass(this, StartSyncService.class);
		startService(intent);
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
			final SpannableStringBuilder ssb = new SpannableStringBuilder(s);

			final Matcher matcher = BOLD_PATTERN.matcher(s);
			while (matcher.find()) {
				final int start = matcher.start();
				final int end = matcher.end();
				ssb.setSpan(new TextAppearanceSpan(null, Typeface.BOLD, -1, null, null), start, end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}

			mLogText.setText(ssb);
			mLogScroll.post(new Runnable() {
				@Override
				public void run() {
					scrollToBottom();
				}
			});
		} catch (Exception x) {
			mLogText.setText(x.toString());
		} finally {
			StreamUtil.closeRaf(raf);
		}
	}

	private void onLogReset() {
		final File f = MyLog.getLogFileName();
		MyLog.setDebugSettings(true);
		f.delete();
		MyLog.i(TAG, "The log has been reset");
		onLogRefresh();
	}

	private void scrollToBottom() {
		final Layout layout = mLogText.getLayout();
		if (layout != null) {
			final int lineCount = layout.getLineCount();
			if (lineCount > 0) {
				final int bottom = layout.getLineBottom(lineCount - 1);
				final int offset = bottom - mLogScroll.getHeight();
				mLogScroll.smoothScrollTo(0, offset);
			}
		}
	}

	private ScrollView mLogScroll;
	private TextView mLogText;

	private Button mRunSync;
	private Button mLogRefresh;
	private Button mLogReset;
}
