package ru.itis.meshy.android.introduction;

import static ru.itis.meshy.android.conversation.ConversationActivity.CONTACT_ID;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.meshy.R;
import ru.itis.meshy.android.activity.ActivityComponent;
import ru.itis.meshy.android.activity.MeshyActivity;
import ru.itis.meshy.android.fragment.BaseFragment.BaseFragmentListener;

import javax.inject.Inject;

public class IntroductionActivity extends MeshyActivity
		implements BaseFragmentListener {

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private IntroductionViewModel viewModel;

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(IntroductionViewModel.class);
	}

	private static final String BUNDLE_CONTACT2 = "contact2";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		int contactId1 = intent.getIntExtra(CONTACT_ID, -1);
		if (contactId1 == -1)
			throw new IllegalStateException("No ContactId");
		ContactId firstContactId = new ContactId(contactId1);

		viewModel.setFirstContactId(firstContactId);

		setContentView(R.layout.activity_fragment_container);

		if (savedInstanceState == null) {
			showInitialFragment(new ContactChooserFragment());
		} else {
			int contactId2 = savedInstanceState.getInt(BUNDLE_CONTACT2);
			ContactId secondContactId = new ContactId(contactId2);
			viewModel.setSecondContactId(secondContactId);
		}

		viewModel.getSecondContactSelected().observeEvent(this, e -> {
			IntroductionMessageFragment fragment =
					new IntroductionMessageFragment();
			showNextFragment(fragment);
		});
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);

		ContactId secondContactId = viewModel.getSecondContactId();
		if (secondContactId != null) {
			outState.putInt(BUNDLE_CONTACT2, secondContactId.getInt());
		}
	}

}
