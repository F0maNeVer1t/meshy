package ru.itis.meshy.android.util;

import static androidx.core.content.ContextCompat.getColor;

import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.snackbar.Snackbar;

import ru.itis.meshy.R;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public class MeshySnackbarBuilder {

	@ColorRes
	@Nullable
	private Integer backgroundResId = null;
	@StringRes
	private int actionResId;
	@Nullable
	private OnClickListener onClickListener;

	public Snackbar make(View view, CharSequence text, int duration) {
		Snackbar s = Snackbar.make(view, text, duration);
		if (backgroundResId != null) {
			s.setBackgroundTint(getColor(view.getContext(), backgroundResId));
			s.setTextColor(
					getColor(view.getContext(), R.color.md_theme_onSecondary));
		}
		if (onClickListener != null) {
			s.setActionTextColor(getColor(view.getContext(),
					R.color.meshy_button_text_positive));
			s.setAction(actionResId, onClickListener);
		}
		return s;
	}

	public Snackbar make(View view, @StringRes int resId, int duration) {
		return make(view, view.getResources().getText(resId), duration);
	}

	public MeshySnackbarBuilder setBackgroundColor(
			@ColorRes int backgroundResId) {
		this.backgroundResId = backgroundResId;
		return this;
	}

	public MeshySnackbarBuilder setAction(@StringRes int actionResId,
                                          OnClickListener onClickListener) {
		this.actionResId = actionResId;
		this.onClickListener = onClickListener;
		return this;
	}

}
