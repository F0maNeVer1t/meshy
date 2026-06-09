package ru.itis.messaging_engine.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.itis.messaging_engine.BrambleApplication;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		BrambleApplication app =
				(BrambleApplication) ctx.getApplicationContext();
		app.getBrambleAppComponent().alarmListener().onAlarm(intent);
	}
}
