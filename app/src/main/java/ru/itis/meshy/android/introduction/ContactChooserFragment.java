package ru.itis.meshy.android.introduction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import ru.itis.meshy.R;
import ru.itis.meshy.android.activity.ActivityComponent;
import ru.itis.meshy.android.contact.ContactListAdapter;
import ru.itis.meshy.android.contact.ContactListItem;
import ru.itis.meshy.android.contact.OnContactClickListener;
import ru.itis.meshy.android.fragment.BaseFragment;
import ru.itis.meshy.android.view.MeshyRecyclerView;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import javax.inject.Inject;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ContactChooserFragment extends BaseFragment
		implements OnContactClickListener<ContactListItem> {

	private static final String TAG = ContactChooserFragment.class.getName();

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private IntroductionViewModel viewModel;
	private final ContactListAdapter adapter = new ContactListAdapter(this);
	private MeshyRecyclerView list;

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
				.get(IntroductionViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		// change toolbar text (relevant when navigating back to this fragment)
		requireActivity().setTitle(R.string.introduction_activity_title);

		View contentView = inflater.inflate(R.layout.list, container, false);

		list = contentView.findViewById(R.id.list);
		list.setLayoutManager(new LinearLayoutManager(getActivity()));
		list.setAdapter(adapter);
		list.setEmptyText(R.string.no_contacts);

		viewModel.getContactListItems().observe(getViewLifecycleOwner(),
				result -> result.onError(this::handleException)
						.onSuccess(adapter::submitList)
		);

		return contentView;
	}

	@Override
	public void onStart() {
		super.onStart();
		list.startPeriodicUpdate();
	}

	@Override
	public void onStop() {
		super.onStop();
		list.stopPeriodicUpdate();
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

	@Override
	public void onItemClick(View view, ContactListItem item) {
		viewModel.setSecondContactId(item.getContact().getId());
		viewModel.triggerContactSelected();
	}
}
