package ru.itis.meshy.android.settings;

import static android.content.Intent.ACTION_VIEW;
import static ru.itis.meshy.android.util.UiUtils.tryToStartActivity;
import static java.util.logging.Logger.getLogger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.itis.meshy.BuildConfig;
import ru.itis.meshy.R;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import java.util.logging.Logger;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class AboutFragment extends Fragment {

	final static String TAG = AboutFragment.class.getName();
	private static final Logger LOG = getLogger(TAG);

	private TextView meshyVersion;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_about, container,
				false);
	}

	@Override
	public void onStart() {
		super.onStart();
		requireActivity().setTitle(R.string.about_title);
		meshyVersion = requireActivity().findViewById(R.id.MeshyVersion);
		meshyVersion.setText(
				getString(R.string.meshy_version, BuildConfig.VERSION_NAME));
	}

	private void goToUrl(String url) {
		Intent i = new Intent(ACTION_VIEW);
		i.setData(Uri.parse(url));
		tryToStartActivity(requireActivity(), i);
	}

}