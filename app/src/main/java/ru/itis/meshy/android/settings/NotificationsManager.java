package ru.itis.meshy.android.settings;

import static android.widget.Toast.LENGTH_SHORT;
import static ru.itis.messaging_engine.util.LogUtils.logDuration;
import static ru.itis.messaging_engine.util.LogUtils.logException;
import static ru.itis.messaging_engine.util.LogUtils.now;
import static ru.itis.meshy.android.settings.SettingsFragment.SETTINGS_NAMESPACE;
import static ru.itis.meshy.api.android.AndroidNotificationManager.PREF_NOTIFY_PRIVATE;
import static ru.itis.meshy.api.android.AndroidNotificationManager.PREF_NOTIFY_RINGTONE_NAME;
import static ru.itis.meshy.api.android.AndroidNotificationManager.PREF_NOTIFY_RINGTONE_URI;
import static ru.itis.meshy.api.android.AndroidNotificationManager.PREF_NOTIFY_SOUND;
import static ru.itis.meshy.api.android.AndroidNotificationManager.PREF_NOTIFY_VIBRATION;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.settings.Settings;
import ru.itis.messaging_engine.api.settings.SettingsManager;
import ru.itis.meshy.R;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
class NotificationsManager {

	private final static Logger LOG =
			getLogger(NotificationsManager.class.getName());

	private final Context ctx;
	private final SettingsManager settingsManager;
	private final Executor dbExecutor;

	private final MutableLiveData<Boolean> notifyPrivateMessages =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> notifyVibration =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> notifySound =
			new MutableLiveData<>();

	private volatile String ringtoneName, ringtoneUri;

	NotificationsManager(Context ctx,
			SettingsManager settingsManager,
			Executor dbExecutor) {
		this.ctx = ctx;
		this.settingsManager = settingsManager;
		this.dbExecutor = dbExecutor;
	}

	void updateSettings(Settings settings) {
		notifyPrivateMessages.postValue(settings.getBoolean(
				PREF_NOTIFY_PRIVATE, true));
		notifyVibration.postValue(settings.getBoolean(
				PREF_NOTIFY_VIBRATION, true));
		ringtoneName = settings.get(PREF_NOTIFY_RINGTONE_NAME);
		ringtoneUri = settings.get(PREF_NOTIFY_RINGTONE_URI);
		notifySound.postValue(settings.getBoolean(PREF_NOTIFY_SOUND, true));
	}

	void onRingtoneSet(@Nullable Uri uri) {
		Settings s = new Settings();
		if (uri == null) {
			// The user chose silence
			s.putBoolean(PREF_NOTIFY_SOUND, false);
			s.put(PREF_NOTIFY_RINGTONE_NAME, "");
			s.put(PREF_NOTIFY_RINGTONE_URI, "");
		} else if (RingtoneManager.isDefault(uri)) {
			// The user chose the default
			s.putBoolean(PREF_NOTIFY_SOUND, true);
			s.put(PREF_NOTIFY_RINGTONE_NAME, "");
			s.put(PREF_NOTIFY_RINGTONE_URI, "");
		} else {
			// The user chose a ringtone other than the default
			Ringtone r = RingtoneManager.getRingtone(ctx, uri);
			if (r == null || "file".equals(uri.getScheme())) {
				Toast.makeText(ctx, R.string.cannot_load_ringtone, LENGTH_SHORT)
						.show();
			} else {
				String name = r.getTitle(ctx);
				s.putBoolean(PREF_NOTIFY_SOUND, true);
				s.put(PREF_NOTIFY_RINGTONE_NAME, name);
				s.put(PREF_NOTIFY_RINGTONE_URI, uri.toString());
			}
		}
		dbExecutor.execute(() -> {
			try {
				long start = now();
				settingsManager.mergeSettings(s, SETTINGS_NAMESPACE);
				logDuration(LOG, "Merging notification settings", start);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	LiveData<Boolean> getNotifyPrivateMessages() {
		return notifyPrivateMessages;
	}

	LiveData<Boolean> getNotifyVibration() {
		return notifyVibration;
	}

	@NonNull
	LiveData<Boolean> getNotifySound() {
		return notifySound;
	}

	String getRingtoneName() {
		return ringtoneName;
	}

	String getRingtoneUri() {
		return ringtoneUri;
	}
}
