/***
  Copyright (c) 2012-2014 CommonsWare, LLC
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

import java.util.Calendar;
import java.util.Set;

import org.kman.tests.utils.MyLog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PollReceiver extends BroadcastReceiver {

	private static final String TAG = "PollReceiver";

	@Override
	public void onReceive(Context ctxt, Intent i) {
		MyLog.i(TAG, "onReceive: %s", i);

		if (i != null) {
			final Bundle extras = i.getExtras();
			if (extras != null) {
				final StringBuilder sbExtras = new StringBuilder();
				final Set<String> keySet = extras.keySet();
				for (String key : keySet) {
					if (sbExtras.length() != 0) {
						sbExtras.append(", ");
					}
					sbExtras.append(key).append(" = ").append(extras.get(key));
					if (key.equals("android.intent.extra.ALARM_TARGET_TIME")) {
						sbExtras.append(String.format(" %1$tF %1$tT.%1$tL", extras.getLong(key)));
					}
				}
				if (sbExtras.length() == 0) {
					sbExtras.append("[no extras]");
				}
				MyLog.i(TAG, "onReceive extras: %s", sbExtras);
			}
		}

		if (i.getAction() == null) {
			PollReceiver.scheduleAlarms(ctxt);
			WakefulIntentService.sendWakefulWork(ctxt, ScheduledService.class);
		} else {
			scheduleAlarms(ctxt);
		}
	}

	static void scheduleAlarms(Context ctxt) {
		AlarmManager mgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ctxt, PollReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);

		Calendar when = Calendar.getInstance();

		when.add(Calendar.MINUTE, 1);

		int unroundedMinutes = when.get(Calendar.MINUTE);
		int mod = unroundedMinutes % 15;

		when.add(Calendar.MINUTE, 15 - mod);
		when.set(Calendar.SECOND, 0);
		when.set(Calendar.MILLISECOND, 0);

		MyLog.i("PollReceiver", "Scheduling for " + when.toString());

		mgr.setWindow(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), 30000, pi);
	}
}
