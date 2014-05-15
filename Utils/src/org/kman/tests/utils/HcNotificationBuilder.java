package org.kman.tests.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;

public abstract class HcNotificationBuilder {

	public static final int MAX_INBOX_STYLE_ITEMS = 5;

	public static HcNotificationBuilder create(Context context, int smallIconRes, CharSequence ticker, long when) {
		if (Build.VERSION.SDK_INT >= 16) {
			/*
			 * API 16, Android 4.1: "big text" style, we call it largePreview
			 */
			return new HcNotificationBuilder_api16(context, smallIconRes, ticker, when);
		} else if (Build.VERSION.SDK_INT >= 14) {
			/*
			 * API 14, Android 4.0: progress indication
			 */
			return new HcNotificationBuilder_api14(context, smallIconRes, ticker, when);
		} else if (Build.VERSION.SDK_INT >= 11) {
			/*
			 * API 11, Android 3.0: large bitmap, separate count in the lower right
			 */
			return new HcNotificationBuilder_api11(context, smallIconRes, ticker, when);
		}

		return null;
	}

	public static final boolean hasListSupport() {
		return Build.VERSION.SDK_INT >= 16;
	}

	public static final boolean hasActionSupport() {
		return Build.VERSION.SDK_INT >= 16;
	}

	public static class Action {
		public int icon;
		public CharSequence title;
		public PendingIntent intent;
	}

	public abstract void setLatestEventInfo(CharSequence title, CharSequence message, PendingIntent pendingIntent);

	public abstract void setSubTitle(CharSequence subtitle);

	public abstract void setOngoing(boolean ongoing);

	public abstract void setProgress(int max, int progress);

	public static final int PRIORITY_MIN = -2;

	public abstract void setPriority(int priority);

	public abstract void setNumber(int number);

	public abstract void setSound(Uri uri);

	public abstract void setDefaults(int flags);

	public abstract void setLights(int argb, int onMs, int offMs);

	public abstract void setVibrate(long[] pattern);

	public abstract void setOnlyAlertOnce(boolean once);

	public abstract void setDeleteIntent(PendingIntent deleteIntent);

	public abstract void setLargeIcon(Bitmap bitmap);

	public abstract void setLargePreview(String subject, String largePreview);

	public abstract void setList(CharSequence[] list, CharSequence more);

	public abstract void setActions(Action[] list);

	public abstract Notification getNotification();
}

@TargetApi(11)
class HcNotificationBuilder_api11 extends HcNotificationBuilder {

	/* package */HcNotificationBuilder_api11(Context context, int smallIconRes, CharSequence ticker, long when) {
		mBuilder = new Notification.Builder(context).setSmallIcon(smallIconRes).setTicker(ticker).setWhen(when);
	}

	@Override
	public void setLatestEventInfo(CharSequence title, CharSequence message, PendingIntent pendingIntent) {
		mBuilder.setContentTitle(title).setContentText(message).setContentIntent(pendingIntent).setAutoCancel(true);
	}

	@Override
	public void setSubTitle(CharSequence subtitle) {
	}

	@Override
	public void setOngoing(boolean ongoing) {
		mBuilder.setOngoing(ongoing);
	}

	@Override
	public void setProgress(int max, int progress) {
	}

	@Override
	public void setPriority(int priority) {
	}

	@Override
	public void setNumber(int number) {
		mBuilder.setNumber(number);
	}

	@Override
	public void setSound(Uri uri) {
		mBuilder.setSound(uri);
	}

	@Override
	public void setDefaults(int flags) {
		mBuilder.setDefaults(flags);
	}

	@Override
	public void setLights(int argb, int onMs, int offMs) {
		mBuilder.setLights(argb, onMs, offMs);
	}

	@Override
	public void setVibrate(long[] pattern) {
		mBuilder.setVibrate(pattern);
	}

	@Override
	public void setOnlyAlertOnce(boolean once) {
		mBuilder.setOnlyAlertOnce(once);
	}

	@Override
	public void setDeleteIntent(PendingIntent deleteIntent) {
		mBuilder.setDeleteIntent(deleteIntent);
	}

	@Override
	public void setLargeIcon(Bitmap bitmap) {
		mBuilder.setLargeIcon(bitmap);
	}

	@Override
	public void setLargePreview(String subject, String largePreview) {
	}

	@Override
	public void setList(CharSequence[] list, CharSequence more) {
	}

	@Override
	public void setActions(Action[] list) {
	}

	@SuppressWarnings("deprecation")
	@Override
	public Notification getNotification() {
		return mBuilder.getNotification();
	}

	protected Notification.Builder mBuilder;
}

@TargetApi(14)
class HcNotificationBuilder_api14 extends HcNotificationBuilder_api11 {
	/* package */HcNotificationBuilder_api14(Context context, int smallIconRes, CharSequence ticker, long when) {
		super(context, smallIconRes, ticker, when);
	}

	@Override
	public void setProgress(int max, int progress) {
		mBuilder.setProgress(max, progress, false);
	}
}

@TargetApi(16)
class HcNotificationBuilder_api16 extends HcNotificationBuilder_api14 {

	private static final int SUBJECT_LIMIT = 30;
	private static final int PREVIEW_LIMIT = 150;

	/* package */HcNotificationBuilder_api16(Context context, int smallIconRes, CharSequence ticker, long when) {
		super(context, smallIconRes, ticker, when);
	}

	@Override
	public void setSubTitle(CharSequence subtitle) {
		mBuilder.setSubText(subtitle);
	}

	@Override
	public void setPriority(int priority) {
		mBuilder.setPriority(priority);
	}

	@Override
	public void setLargePreview(String subject, String largePreview) {
		if (!TextUtils.isEmpty(largePreview)) {
			if (mBigTextStyle == null) {
				mBuilder.setPriority(Notification.PRIORITY_HIGH);
				mBigTextStyle = new Notification.BigTextStyle(mBuilder);
			}

			CharSequence clipped = largePreview;
			if (largePreview.length() > PREVIEW_LIMIT) {
				clipped = largePreview.substring(0, PREVIEW_LIMIT).concat("…");
			}

			if (!TextUtils.isEmpty(subject)) {
				SpannableStringBuilder ssb = new SpannableStringBuilder();

				if (subject.length() > SUBJECT_LIMIT) {
					ssb.append(subject.substring(0, SUBJECT_LIMIT).concat("…"));
				} else {
					ssb.append(subject);
				}

				int len = ssb.length();
				ssb.append("\n");
				ssb.append(clipped);
				ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				clipped = ssb;
			}

			mBigTextStyle.bigText(clipped);
		}
	}

	@Override
	public void setList(CharSequence[] list, CharSequence more) {
		if (list != null && list.length != 0) {
			if (mInboxStyle == null) {
				mBuilder.setPriority(Notification.PRIORITY_HIGH);
				mInboxStyle = new Notification.InboxStyle(mBuilder);
			}

			for (CharSequence l : list) {
				mInboxStyle.addLine(l);
			}
			if (more != null) {
				mInboxStyle.setSummaryText(more);
			}
		}
	}

	@Override
	public void setActions(Action[] list) {
		for (Action action : list) {
			mBuilder.addAction(action.icon, action.title, action.intent);
		}
	}

	@Override
	public Notification getNotification() {
		if (mBigTextStyle != null) {
			return mBigTextStyle.build();
		}
		if (mInboxStyle != null) {
			return mInboxStyle.build();
		}
		return super.getNotification();
	}

	private Notification.BigTextStyle mBigTextStyle;
	private Notification.InboxStyle mInboxStyle;
}
