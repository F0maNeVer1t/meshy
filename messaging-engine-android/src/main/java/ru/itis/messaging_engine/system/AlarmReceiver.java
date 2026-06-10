package ru.itis.messaging_engine.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.itis.messaging_engine.MessagingEngineApplication;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		MessagingEngineApplication app =
				(MessagingEngineApplication) ctx.getApplicationContext();
		app.getBrambleAppComponent().alarmListener().onAlarm(intent);
	}
}
