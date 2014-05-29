### KitKatAlarmTest
**Demonstrates an issue with AlarmManager alarms being delivered too early on Samsung devices (tested on two) for targetApi=19**

The documentation guarantees that any of set(), setWindow(), setExact() will never deliver the alarm before
the scheduled time, only exactly on time or after.

I ran the test app on two Samsung devices with 4.4.2 that I have: a Note 3 N9005, and a Galaxy S4 i9505.

The issue happens on both of them. No matter if I use setWindow or setExact. It also survives reboots, cleaning Dalvik Cache, updating the firmware.

The thing that triggers the bug is connecting to a network server. Leaving a connection open between the alarms is not neessary for the issue to occur.

The relevant code is in [Task.java](KitKatAlarmTest/src/org/kman/KitKatAlarmTest/Task.java)

It builds with Eclipse and Gradle.

The issue can be easily seen in the app's log, highlighted in red:

AlarmReceiver ***** fired too early *****

Immediately above that is a dump of the alarm's extras, which includes 1) when the alarm was set and 2) for
what time it was scheduled.

[Screenshot from Galaxy S4](KitKatAlarmTest/screenshots/example-samsung-s4.png)

In my real app, I'm seeing same issue on both Samsungs, and do not see it on a Nexus 5 or an HTC One Max, both
also with 4.4.2.

