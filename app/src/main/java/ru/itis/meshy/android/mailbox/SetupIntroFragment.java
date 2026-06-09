package ru.itis.meshy.android.mailbox;

import static ru.itis.meshy.android.AppModule.getAndroidComponent;
import static ru.itis.meshy.android.util.UiUtils.hideViewOnSmallScreen;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import ru.itis.meshy.R;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import javax.inject.Inject;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class SetupIntroFragment extends Fragment {

	static final String TAG = SetupIntroFragment.class.getName();

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private MailboxViewModel viewModel;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		FragmentActivity activity = requireActivity();
		getAndroidComponent(activity).inject(this);
		viewModel = new ViewModelProvider(activity, viewModelFactory)
				.get(MailboxViewModel.class);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_mailbox_setup_intro,
				container, false);
		Button button = v.findViewById(R.id.continueButton);
		button.setOnClickListener(view -> viewModel.showDownloadFragment());
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		requireActivity().setTitle(R.string.mailbox_setup_title);
		hideViewOnSmallScreen(requireView().findViewById(R.id.imageView));
	}

}
