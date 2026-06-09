package ru.itis.meshy.android.settings;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.EXTRA_TEXT;
import static ru.itis.meshy.android.AppModule.getAndroidComponent;
import static ru.itis.meshy.android.util.UiUtils.launchActivityToOpenFile;
import static ru.itis.meshy.android.util.UiUtils.triggerFeedback;
import static ru.itis.meshy.android.util.UiUtils.tryToStartActivity;
import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

import ru.itis.meshy.R;
import ru.itis.meshy.android.mailbox.MailboxActivity;
import ru.itis.meshy.android.util.ActivityLaunchers.GetImageAdvanced;
import ru.itis.meshy.android.util.ActivityLaunchers.OpenImageDocumentAdvanced;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import javax.inject.Inject;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class SettingsFragment extends PreferenceFragmentCompat {

	public static final String SETTINGS_NAMESPACE = "android-ui";

	private static final String PREF_KEY_AVATAR = "pref_key_avatar";

	private static final String PREF_KEY_DEV = "pref_key_dev";

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private SettingsViewModel viewModel;
	private AvatarPreference prefAvatar;

	private final ActivityResultLauncher<String[]> docLauncher =
			registerForActivityResult(new OpenImageDocumentAdvanced(),
					this::onImageSelected);
	private final ActivityResultLauncher<String> contentLauncher =
			registerForActivityResult(new GetImageAdvanced(),
					this::onImageSelected);

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		getAndroidComponent(context).inject(this);
		viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
				.get(SettingsViewModel.class);
	}

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.settings);

		prefAvatar = requireNonNull(findPreference(PREF_KEY_AVATAR));
		if (viewModel.shouldEnableProfilePictures()) {
			prefAvatar.setOnPreferenceClickListener(preference -> {
				launchActivityToOpenFile(requireContext(),
						docLauncher, contentLauncher, "image/*");
				return true;
			});
		} else {
			prefAvatar.setVisible(false);
		}

        PreferenceGroup dev = requireNonNull(findPreference(PREF_KEY_DEV));
        dev.setVisible(false);
	}

	@Override
	public void onViewCreated(@NonNull View view,
			@Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		viewModel.getOwnIdentityInfo().observe(getViewLifecycleOwner(), us ->
				prefAvatar.setOwnIdentityInfo(us)
		);
	}

	@Override
	public void onStart() {
		super.onStart();
		requireActivity().setTitle(R.string.settings_button);
	}

	private void onImageSelected(@Nullable Uri uri) {
		if (uri == null) return;
		DialogFragment dialog = ConfirmAvatarDialogFragment.newInstance(uri);
		dialog.show(getParentFragmentManager(),
				ConfirmAvatarDialogFragment.TAG);
	}

}
