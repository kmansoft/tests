package org.kman.test.charsettest;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTextView = (TextView) findViewById(R.id.text_main);

		final String s = Charsets.CHARSET_UTF_8;
		mTextView.setText(s);

		final String q = Charsets.NIO_CHARSET_UTF_8.displayName();
		mTextView.setText(q);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mTask == null) {

			final Test[] list = {
					new Test(new byte[] { -48, -94, -48, -75, -47, -127, -47, -126, -48, -66, -48, -78, -48, -66, -48,
							-75, 32, -47, -127, -48, -66, -48, -66, -48, -79, -47, -119, -48, -75, -48, -67, -48, -72,
							-48, -75 }, null, "Тестовое сообщение"),

					new Test(
							new byte[] { 82, 101, 58, 32, -48, -97, -47, -128, -48, -66, -48, -79, -48, -69, -48, -75,
									-48, -68, -48, -80, 32, -48, -66, -47, -126, -48, -66, -48, -79, -47, -128, -48,
									-80, -48, -74, -48, -75, -48, -67, -48, -72, -47, -113, 32, -47, -126, -48, -75,
									-48, -68, -47, -117 }, "utf-8", "Re: Проблема отображения темы"),

					new Test(new byte[] { 82, 101, 58, 32, -48, -97, -47, -128, -48, -66, -48, -79, -48, -69, -48, -75,
							-48, -68, -48, -80, 32, -48, -66, -47, -126, -48, -66, -48, -79, -47, -128, -48, -80, -48,
							-74, -48, -75, -48, -67, -48, -72, -47, -113, 32, -47, -126, -48, -75, -48, -68, -47, -117,
							32, -48, -65, -48, -72, -47, -127, -47, -116, -48, -68, -48, -80, 32, -48, -67, -48, -80,
							32, 65, 110, 100, 114, 111, 105, 100, 32, 54, 46, 48, 46, 49 }, null,
							"Re: Проблема отображения темы письма на Android 6.0.1"),

					new Test(new byte[] { 82, 101, 58, 32, -49, -16, -18, -31, -21, -27, -20, -32, 32, -18, -14, -18,
							-31, -16, -32, -26, -27, -19, -24, -1, 32, -14, -27, -20, -5, 32, -17, -24, -15, -4, -20,
							-32, 32, -19, -32, 32, 65, 110, 100, 114, 111, 105, 100, 32, 54, 46, 48, 46, 49 },
							"windows-1251", "Re: Проблема отображения темы письма на Android 6.0.1"),

					new Test(new byte[] { -48, -94, -48, -75, -47, -127, -47, -126, -48, -66, -48, -78, -48, -66, -48,
							-75, 32, -47, -127, -48, -66, -48, -66, -48, -79, -47, -119, -48, -75, -48, -67, -48, -72,
							-48, -75, 32 }, null, "Тестовое сообщение ")

			};

			mTask = new TestTask(this, list);
			mTask.execute((Void[]) null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	protected void onDestroy() {
		super.onDestroy();

		if (mTask != null) {
			mTask.setActivity(null);
		}
	}

	private void onTestDone(TestTask task, Test[] list) {
		if (mTask == task) {
			mTask = null;

			mTextView.setText(null);

			final StringBuilder sb = new StringBuilder();
			for (Test test : list) {
				if (sb.length() != 0) {
					sb.append("\n\n");
				}

				sb.append("cs: ").append(test.cs).append("\n");
				sb.append("expected: ").append(test.check).append("\n");
				if (test.check.equals(test.res)) {
					sb.append("OK");
				} else {
					sb.append("BAD: \"").append(test.res).append("\"");
				}
			}

			mTextView.setText(sb.toString());
		}
	}

	private static class Test {
		Test(byte[] b, String cs, String check) {
			this.b = b;
			this.cs = cs;
			this.check = check;
		}

		byte[] b;
		String cs;
		String check;
		String res;
	}

	private static class TestTask extends AsyncTask<Void, Void, Void> {

		TestTask(MainActivity activity, Test[] list) {
			mActivity = activity;
			mList = list;
		}

		void setActivity(MainActivity activity) {
			mActivity = activity;
		}

		@Override
		protected Void doInBackground(Void... params) {

			for (Test test : mList) {
				if (test.cs == null) {
					test.res = new String(test.b, Charsets.NIO_CHARSET_UTF_8);
				} else {
					try {
						test.res = new String(test.b, test.cs);
					} catch (UnsupportedEncodingException e) {
						test.res = null;
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mActivity != null) {
				mActivity.onTestDone(this, mList);
			}
		}

		private Test[] mList;
		private MainActivity mActivity;
	}

	private TextView mTextView;
	private TestTask mTask;
}
