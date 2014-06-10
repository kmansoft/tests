package org.kman.KitKatAlarmTest;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kman.KitKatAlarmTest.net.StreamUtil;
import org.kman.KitKatAlarmTest.sync.SyncContentProvider;
import org.kman.tests.utils.MyLog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "MainActivity";

	private static final String ACCOUNT_TYPE = "org.kman.KitKatAlarmTest";

	private static final int MAX_LOG_TEXT = 30 * 1024;
	private static final int MAX_NEWLINE_SEARCH = 1024;

	private static final Pattern TIME_PATTERN = Pattern.compile("\\d{2}:\\d{2}:\\d{2}.\\d{3}");
	private static final Pattern BAD_PATTERN = Pattern.compile("\\*{5}[a-z ]+\\*{5}");
	private static final Pattern GOOD_PATTERN = Pattern.compile("\\#{5}[a-z ]+\\#{5}");

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

		AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
		addAccount(accountManager, "test_account_1");
		addAccount(accountManager, "test_account_2");

		onLogRefresh();
	}

	private void addAccount(AccountManager accountManager, String accountName) {
		Account newAccount = new Account(accountName, ACCOUNT_TYPE);
		boolean newAccountAdded = accountManager.addAccountExplicitly(newAccount, null, null);

		ContentResolver.setIsSyncable(newAccount, SyncContentProvider.AUTHORITY, 1);
		ContentResolver.setSyncAutomatically(newAccount, SyncContentProvider.AUTHORITY, true);
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

			final Matcher timeMatcher = TIME_PATTERN.matcher(s);
			while (timeMatcher.find()) {
				final int start = timeMatcher.start();
				final int end = timeMatcher.end();
				ssb.setSpan(new TextAppearanceSpan(null, Typeface.BOLD, -1, null, null), start, end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}

			final Resources res = getResources();
			final ColorStateList badColor = res.getColorStateList(R.color.error_color);
			final Matcher badMatcher = BAD_PATTERN.matcher(s);
			while (badMatcher.find()) {
				final int start = badMatcher.start();
				final int end = badMatcher.end();
				ssb.setSpan(new TextAppearanceSpan(null, Typeface.BOLD, -1, badColor, null), start, end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}

			final ColorStateList goodColor = res.getColorStateList(R.color.again_color);
			final Matcher goodMatcher = GOOD_PATTERN.matcher(s);
			while (goodMatcher.find()) {
				final int start = goodMatcher.start();
				final int end = goodMatcher.end();
				ssb.setSpan(new TextAppearanceSpan(null, Typeface.BOLD, -1, goodColor, null), start, end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}

			mLogText.setText(ssb);
			mLogScroll.post(new Runnable() {
				@Override
				public void run() {
					mLogScroll.scrollTo(0, mLogText.getMeasuredHeight());
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

	private ScrollView mLogScroll;
	private TextView mLogText;

	private Button mRunSync;
	private Button mLogRefresh;
	private Button mLogReset;
}
