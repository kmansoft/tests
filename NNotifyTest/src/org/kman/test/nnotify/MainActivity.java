package org.kman.test.nnotify;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	private static final String GROUP_KEY = "1";

	private static final int ID_SINGLE = 0x10000;
	private static final int ID_STACKED_PARENT = 0x100010;
	private static final int ID_STACKED_CHILD_1 = 0x100020;
	private static final int ID_STACKED_CHILD_2 = 0x100030;

	private static final int ACTION_ID_1 = 1;
	private static final int ACTION_ID_2 = 2;
	private static final int ACTION_ID_3 = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
		final int id = item.getItemId();
		switch (id) {
		case R.id.action_notfy_single:
			onActionNotifySingle();
			break;

		case R.id.action_notify_stacked:
			onActionNotifyStacked();
			break;

		default:
			return super.onOptionsItemSelected(item);

		}
		return true;
	}

	private void onActionNotifySingle() {
		final Context context = getApplicationContext();
		final NotificationManagerCompat nm = NotificationManagerCompat.from(context);

		cancelOldNotifications(nm);

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

		builder.setContentTitle("single title");
		builder.setContentText("single text");
		builder.setSubText("single subtext");
		builder.setSmallIcon(R.drawable.ic_launcher);

		builder.setCategory(NotificationCompat.CATEGORY_EMAIL);
		builder.setGroup(GROUP_KEY).setGroupSummary(true);

		builder.addAction(createAction(context, "one", ACTION_ID_1));
		builder.addAction(createAction(context, "two", ACTION_ID_2));
		builder.addAction(createAction(context, "three", ACTION_ID_3));

		final Notification notification = builder.build();
		nm.notify(ID_SINGLE, notification);
	}

	private void onActionNotifyStacked() {
		final Context context = getApplicationContext();
		final NotificationManagerCompat nm = NotificationManagerCompat.from(context);

		cancelOldNotifications(nm);

		// Group
		{
			final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

			builder.setContentTitle("stacked title");
			builder.setContentText("stacked text");
			builder.setSubText("stacked subtext");
			builder.setSmallIcon(R.drawable.ic_launcher);

			builder.setCategory(NotificationCompat.CATEGORY_EMAIL);
			builder.setGroup(GROUP_KEY).setGroupSummary(true);

			builder.addAction(createAction(context, "stacked one", ID_STACKED_PARENT + ACTION_ID_1));
			builder.addAction(createAction(context, "stacked two", ID_STACKED_PARENT + ACTION_ID_2));
			builder.addAction(createAction(context, "stacked three", ID_STACKED_PARENT + ACTION_ID_3));

			final Notification notification = builder.build();
			nm.notify(ID_STACKED_PARENT, notification);
		}

		// A child
		{
			final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

			builder.setContentTitle("child1 title");
			builder.setContentText("child1 text");
			builder.setSubText("child1 subtext");
			builder.setSmallIcon(R.drawable.ic_launcher);

			builder.setCategory(NotificationCompat.CATEGORY_EMAIL);
			builder.setGroup(GROUP_KEY);

			builder.addAction(createAction(context, "child1 one", ID_STACKED_CHILD_1 + ACTION_ID_1));
			builder.addAction(createAction(context, "child1 two", ID_STACKED_CHILD_1 + ACTION_ID_2));
			builder.addAction(createAction(context, "child1 three", ID_STACKED_CHILD_1 + ACTION_ID_3));

			final Notification notification = builder.build();
			nm.notify(ID_STACKED_CHILD_1, notification);
		}

		// A child
		{
			final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

			builder.setContentTitle("child2 title");
			builder.setContentText("child2 text");
			builder.setSubText("child2 subtext");
			builder.setSmallIcon(R.drawable.ic_launcher);

			builder.setCategory(NotificationCompat.CATEGORY_EMAIL);
			builder.setGroup(GROUP_KEY);

			builder.addAction(createAction(context, "child2 one", ID_STACKED_CHILD_2 + ACTION_ID_1));
			builder.addAction(createAction(context, "child2 two", ID_STACKED_CHILD_2 + ACTION_ID_2));
			builder.addAction(createAction(context, "child2 three", ID_STACKED_CHILD_2 + ACTION_ID_3));

			final Notification notification = builder.build();
			nm.notify(ID_STACKED_CHILD_2, notification);
		}
	}

	private NotificationCompat.Action createAction(Context context, String title, int requestCode) {
		final Intent intent = new Intent("my_dummy_action");
		final PendingIntent pending = PendingIntent.getBroadcast(context, requestCode, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		final NotificationCompat.Action action = new NotificationCompat.Action(android.R.drawable.ic_delete, title,
				pending);
		return action;
	}

	private void cancelOldNotifications(NotificationManagerCompat nm) {
		nm.cancel(ID_SINGLE);
		nm.cancel(ID_STACKED_PARENT);
		nm.cancel(ID_STACKED_CHILD_1);
		nm.cancel(ID_STACKED_CHILD_2);
	}
}
