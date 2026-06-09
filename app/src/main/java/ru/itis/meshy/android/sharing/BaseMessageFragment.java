package ru.itis.meshy.android.sharing;

import static ru.itis.meshy.android.view.TextSendController.SendState.SENT;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.meshy.R;
import ru.itis.meshy.android.fragment.BaseFragment;
import ru.itis.meshy.android.view.LargeTextInputView;
import ru.itis.meshy.android.view.TextSendController;
import ru.itis.meshy.android.view.TextSendController.SendListener;
import ru.itis.meshy.android.view.TextSendController.SendState;
import ru.itis.meshy.api.attachment.AttachmentHeader;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.NotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import java.util.List;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class BaseMessageFragment extends BaseFragment
		implements SendListener {

	protected LargeTextInputView message;
	private TextSendController sendController;
	private MessageFragmentListener listener;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		listener = (MessageFragmentListener) context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		// inflate view
		View v = inflater.inflate(R.layout.fragment_message, container,
				false);
		message = v.findViewById(R.id.messageView);
		sendController = new TextSendController(message, this, true);
		message.setSendController(sendController);
		message.setMaxTextLength(listener.getMaximumTextLength());
		message.setButtonText(getString(getButtonText()));
		message.setHint(getHintText());

		return v;
	}

	protected void setTitle(int res) {
		listener.setTitle(res);
	}

	@StringRes
	protected abstract int getButtonText();

	@StringRes
	protected abstract int getHintText();

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (message.isKeyboardOpen()) message.hideSoftKeyboard();
			listener.onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public LiveData<SendState> onSendClick(@Nullable String text,
			List<AttachmentHeader> headers, long expectedAutoDeleteTimer) {
		// disable button to prevent accidental double actions
		sendController.setReady(false);
		message.hideSoftKeyboard();

		listener.onButtonClick(text);
		return new MutableLiveData<>(SENT);
	}

	@UiThread
	@NotNullByDefault
	public interface MessageFragmentListener {

		void onBackPressed();

		void setTitle(@StringRes int titleRes);

		void onButtonClick(@Nullable String text);

		int getMaximumTextLength();

	}

}
