package ru.itis.messaging_engine.api.system;

import android.content.Intent;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface AlarmListener {

	void onAlarm(Intent intent);
}
