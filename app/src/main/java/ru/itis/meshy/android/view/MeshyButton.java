package ru.itis.meshy.android.view;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static androidx.transition.TransitionManager.beginDelayedTransition;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.google.android.material.button.MaterialButton;

import ru.itis.meshy.R;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public class MeshyButton extends FrameLayout {

	private final Button button;
	private final ProgressBar progressBar;

	public MeshyButton(Context context) {
		this(context, null);
	}

	public MeshyButton(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MeshyButton(Context context, @Nullable AttributeSet attrs,
                       int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.meshy_button, this, true);

		TypedArray attributes =
				context.obtainStyledAttributes(attrs, R.styleable.MeshyButton);
		String text = attributes.getString(R.styleable.MeshyButton_text);
		int style = attributes
				.getResourceId(R.styleable.MeshyButton_buttonStyle, 0);
		attributes.recycle();

		ContextThemeWrapper wrapper = new ContextThemeWrapper(context, style);
		button = new MaterialButton(wrapper, null, style);
		button.setText(text);
		addView(button);
		progressBar = findViewById(R.id.meshy_button_progress_bar);
	}

	@Override
	public void setOnClickListener(@Nullable OnClickListener l) {
		if (l == null) button.setOnClickListener(null);
		else {
			button.setOnClickListener(v -> {
				beginDelayedTransition(this);
				progressBar.setVisibility(VISIBLE);
				button.setVisibility(INVISIBLE);
				l.onClick(this);
			});
		}
	}

	public void reset() {
		beginDelayedTransition(this);
		progressBar.setVisibility(INVISIBLE);
		button.setVisibility(VISIBLE);
	}

}
