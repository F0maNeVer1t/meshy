package ru.itis.meshy.android.hotspot;

import static ru.itis.meshy.android.util.UiUtils.showFragment;
import static ru.itis.meshy.api.android.AndroidNotificationManager.ACTION_STOP_HOTSPOT;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import ru.itis.meshy.R;
import ru.itis.meshy.android.activity.ActivityComponent;
import ru.itis.meshy.android.activity.MeshyActivity;
import ru.itis.meshy.android.fragment.BaseFragment.BaseFragmentListener;
import ru.itis.meshy.android.hotspot.HotspotState.HotspotError;
import ru.itis.meshy.android.hotspot.HotspotState.HotspotStarted;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import javax.annotation.Nullable;
import javax.inject.Inject;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class HotspotActivity extends MeshyActivity
		implements BaseFragmentListener {

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private HotspotViewModel viewModel;

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(HotspotViewModel.class);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment_container);

		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setDisplayHomeAsUpEnabled(true);
		}

		FragmentManager fm = getSupportFragmentManager();
		viewModel.getState().observe(this, hotspotState -> {
			if (hotspotState instanceof HotspotStarted) {
				HotspotStarted started = (HotspotStarted) hotspotState;
				String tag = HotspotFragment.TAG;
				if (started.wasNotYetConsumed()) {
					started.consume();
					showFragment(fm, new HotspotFragment(), tag);
				}
			} else if (hotspotState instanceof HotspotError) {
				HotspotError error = (HotspotError) hotspotState;
				showErrorFragment(error.getError());
			}
		});

		if (savedInstanceState == null) {
			// If there is no saved instance state, just start with the intro fragment.
			fm.beginTransaction()
					.replace(R.id.fragmentContainer, new HotspotIntroFragment(),
							HotspotIntroFragment.TAG)
					.commit();
		} else if (viewModel.getState().getValue() == null) {

			Fragment current = fm.findFragmentById(R.id.fragmentContainer);
			if (current instanceof HotspotIntroFragment) {
				// If the currently displayed fragment is the intro fragment,
				// there's nothing we need to do.
				return;
			}

			// Remove everything from the back stack.
			fm.popBackStackImmediate(null,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);

			// Start fresh with the intro fragment.
			fm.beginTransaction()
					.replace(R.id.fragmentContainer, new HotspotIntroFragment(),
							HotspotIntroFragment.TAG)
					.commit();
		}
	}

	private void showErrorFragment(String error) {
		FragmentManager fm = getSupportFragmentManager();
		String tag = HotspotErrorFragment.TAG;
		Fragment f = HotspotErrorFragment.newInstance(error);
		showFragment(fm, f, tag, false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (ACTION_STOP_HOTSPOT.equals(intent.getAction())) {
			// also closes hotspot
			supportFinishAfterTransition();
		}
	}

}
