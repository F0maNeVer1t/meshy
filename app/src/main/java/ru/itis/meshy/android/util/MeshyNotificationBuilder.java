package ru.itis.meshy.android.util;

import static androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE;

import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import ru.itis.meshy.R;

public class MeshyNotificationBuilder extends NotificationCompat.Builder {

	private final Context context;

	public MeshyNotificationBuilder(Context context, String channelId) {
		super(context, channelId);
		this.context = context;
		// Auto-cancel does not fire the delete intent, see
		// https://issuetracker.google.com/issues/36961721
		setAutoCancel(true);

		setLights(ContextCompat.getColor(context, R.color.meshy_gold_400),
				750, 500);
		setVisibility(VISIBILITY_PRIVATE);
	}

	public MeshyNotificationBuilder setColorRes(@ColorRes int res) {
		setColor(ContextCompat.getColor(context, res));
		return this;
	}

	public MeshyNotificationBuilder setNotificationCategory(String category) {
		setCategory(category);
		return this;
	}

}
