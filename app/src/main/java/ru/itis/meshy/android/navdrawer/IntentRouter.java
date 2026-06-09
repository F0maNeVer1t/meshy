package ru.itis.meshy.android.navdrawer;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.EXTRA_TEXT;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static ru.itis.messaging_engine.api.contact.HandshakeLinkConstants.LINK_REGEX;

import android.content.Context;
import android.content.Intent;

import ru.itis.meshy.android.activity.MeshyActivity;
import ru.itis.meshy.android.contact.add.remote.AddContactActivity;

class IntentRouter {

	static void handleExternalIntent(Context ctx, Intent i) {
		String action = i.getAction();
		// add remote contact with clicked meshy:// link
		if (ACTION_VIEW.equals(action) && "meshy".equals(i.getScheme())) {
			redirect(ctx, i, AddContactActivity.class);
		}
		// add remote contact with shared meshy:// link
		else if (ACTION_SEND.equals(action) &&
				"text/plain".equals(i.getType()) &&
				i.getStringExtra(EXTRA_TEXT) != null &&
				LINK_REGEX.matcher(i.getStringExtra(EXTRA_TEXT)).find()) {
			redirect(ctx, i, AddContactActivity.class);
		}
	}

	private static void redirect(Context ctx, Intent i,
			Class<? extends MeshyActivity> activityClass) {
		i.setClass(ctx, activityClass);
		i.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
		ctx.startActivity(i);
	}

}
