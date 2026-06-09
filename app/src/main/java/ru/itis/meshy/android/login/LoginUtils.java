package ru.itis.meshy.android.login;

import static ru.itis.meshy.android.util.UiUtils.getDialogIcon;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.itis.meshy.R;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
class LoginUtils {

	static AlertDialog createKeyStrengthenerErrorDialog(Context ctx) {
		MaterialAlertDialogBuilder builder =
				new MaterialAlertDialogBuilder(ctx, R.style.MeshyDialogTheme);
		builder.setIcon(getDialogIcon(ctx, R.drawable.alerts_and_states_error));
		builder.setTitle(R.string.dialog_title_cannot_check_password);
		builder.setMessage(R.string.dialog_message_cannot_check_password);
		builder.setPositiveButton(R.string.ok, null);
		return builder.create();
	}
}
